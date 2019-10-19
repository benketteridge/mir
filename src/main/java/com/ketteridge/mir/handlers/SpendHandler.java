package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ketteridge.mir.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ratpack.jackson.Jackson.fromJson;

@Slf4j
public class SpendHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        return super.supports(ctx) && ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("spend");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        JedisPool pool = ctx.get(JedisPool.class);

        // we must be authenticated to have got this far:
        String theHash = ctx.get(AuthorizationHandler.Authorization.class).getAuth();

        ctx.parse(fromJson(Transaction.class)).then(transaction -> {
            try (Jedis jedis = pool.getResource()) {
                // return the transactions against the hash
                String currentTransactions = jedis.get("transactions:"+ theHash);
                List<Transaction> transactions = mapper.readValue(currentTransactions, new TypeReference<List<Transaction>>(){});

                // hmm what do we do about mis-matched currencies? (error, or handle exchange rates, or separate balances?)
                // or outspending the balance?
                transactions.add(transaction.spend());

                String newTransactionsList = mapper.writeValueAsString(transactions);
                jedis.set("transactions:"+ theHash, newTransactionsList);
            }

            ctx.getResponse().status(201).send("ok");
        });
    }
}
