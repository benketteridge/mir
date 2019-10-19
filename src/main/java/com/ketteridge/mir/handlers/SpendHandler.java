package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ketteridge.mir.domain.Authorization;
import com.ketteridge.mir.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.List;

import static ratpack.jackson.Jackson.fromJson;

@Slf4j
public class SpendHandler extends ExtendedHandler {

    private static final int MAX_TRIES = 5;

    @Override
    public boolean supports(Context ctx) {
        return ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("spend");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        // we must be authenticated to have got this far:
        ctx.parse(fromJson(Transaction.class)).then(transaction -> {
            try (Jedis jedis = getPool(ctx).getResource()) {
                int failed = 0;
                do {

                    // return the transactions against the hash
                    String key1 = "transactions:" + getAuth(ctx);
                    String key2 = "balance:" + getAuth(ctx);

                    // implementing optimistic locking, with five tries, and 1 second back-off
                    jedis.watch(key1, key2);

                    String currentTransactions = jedis.get(key1);
                    List<Transaction> transactions = mapper.readValue(currentTransactions, new TypeReference<List<Transaction>>() {
                    });

                    // hmm what do we do about mis-matched currencies? (error, or handle exchange rates, or separate balances?)
                    // or outspending the balance?
                    transactions.add(transaction.spend());

                    BigDecimal summation = transactions.stream().map(Transaction::getAmountBD).reduce(BigDecimal.ZERO, BigDecimal::add);

                    String newTransactionsList = mapper.writeValueAsString(transactions);

                    redis.clients.jedis.Transaction multi = jedis.multi();

                    multi.set(key1, newTransactionsList);
                    multi.set(key2, transactions.get(0).getCurrency() + " " + summation.toPlainString());

                    if (multi.exec().isEmpty()) {
                        failed++;
                        // don't hammer the server
                        Thread.sleep(1000);
                    }
                    else failed = -1;
                }
                while (failed > 0 && failed < MAX_TRIES);

                if (failed < 0) {
                    ctx.getResponse().status(201).send("ok");
                }
                else {
                    ctx.getResponse().status(500).send("unable to complete request");
                }
            }
        });
    }
}
