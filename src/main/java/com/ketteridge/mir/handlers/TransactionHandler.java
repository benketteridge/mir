package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ketteridge.mir.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TransactionHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        return super.supports(ctx) && ctx.getRequest().getMethod().isGet() && ctx.getRequest().getPath().equals("transactions");
    }

    @Override
    public void handle(Context ctx) throws Exception {
        JedisPool pool = ctx.get(JedisPool.class);

        // we must be authenticated to have got this far:
        String theHash = ctx.get(AuthorizationHandler.Authorization.class).getAuth();

        String transactionListAsJson;
        try (Jedis jedis = pool.getResource()) {
            // return the transactions against the hash
            transactionListAsJson = jedis.get("transactions:"+ theHash);
        }

        ctx.getResponse().status(200).send(transactionListAsJson);
    }

}
