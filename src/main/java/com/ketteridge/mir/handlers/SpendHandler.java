package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ketteridge.mir.domain.Transaction;
import lombok.RequiredArgsConstructor;
import ratpack.func.Action;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

import static ratpack.jackson.Jackson.fromJson;

/**
 * Handler for /spend API.
 * if the request
 */
public class SpendHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        // check that this is POST /spend
        return ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("spend");
    }

    @Override
    public void handle(Context ctx) {

        try (Jedis jedis = getPool(ctx).getResource()) {
            String auth = getAuth(ctx);
            if (jedis.get("balance:" + auth) == null) {
                sendUnauthorized(ctx);
            }
            else {
                // we must be authenticated to have got this far:
                ctx.parse(fromJson(Transaction.class))
                        // if the JSON parsing fails:
                        .onError(handler -> {
                            ctx.getResponse().status(400).send("bad request");
                        })
                        // if the JSON parsing succeeds, handle the spending
                        .then(new TransactionAction(auth, ctx, jedis));
            }
        }

    }

    private static final int MAX_TRIES = 5;
    private static final TypeReference<List<Transaction>> LIST_TRANSACTIONS = new TypeReference<List<Transaction>>() {
    };

    @RequiredArgsConstructor
    private static class TransactionAction implements Action<Transaction> {
        private final String authKey;
        private final Context ctx;
        private final Jedis jedis;

        @Override
        public void execute(Transaction transaction) throws Exception {
            // start the retry-loop
            int failed = 0;
            do {
                String key1 = "transactions:" + authKey;
                String key2 = "balance:" + authKey;

                // implementing optimistic locking, with five tries, and 1 second back-off,
                // start by marking the keys we're interested in
                jedis.watch(key1, key2);

                // return the transactions against the hash
                String currentTransactions = jedis.get(key1);
                // and parse the existing list
                // TODO: error handling?
                List<Transaction> transactions = mapper.readValue(jedis.get(key1), LIST_TRANSACTIONS);

                // TODO: what do we do about mis-matched currencies? (error, or handle exchange rates, or separate balances?)
                // TODO: how to handle outspending the balance?
                transactions.add(transaction.spend());

                // generate the new value for the transactions key
                String newTransactionsList = mapper.writeValueAsString(transactions);

                // sum all the existing transactions (assuming they're all in the same currency)
                BigDecimal summation = transactions.stream().map(Transaction::getAmountBD).reduce(BigDecimal.ZERO, BigDecimal::add);
                String newBalance = transactions.get(0).getCurrency() + " " + summation.toPlainString();

                // open the transaction
                redis.clients.jedis.Transaction multi = jedis.multi();
                // set the new values
                multi.set(key1, newTransactionsList);
                multi.set(key2, newBalance);

                // if the resulting value set is empty, the optimistic lock failed
                if (multi.exec().isEmpty()) {
                    failed++;
                    // we won't DOS the redis server, but we will only wait five seconds in total.
                    Thread.sleep(1000);
                }
                // hurrah... exit the loop
                else failed = -1;
            }
            while (failed > 0 && failed < MAX_TRIES);

            if (failed < 0) {
                // success
                ctx.getResponse().status(201).send("ok");
            } else {
                // failed, despite the retry, this account is really being hammered!
                // might be a red flag to be alerted elsewhere
                ctx.getResponse().status(500).send("unable to complete request");
            }
        }
    }
}
