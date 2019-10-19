package com.ketteridge.mir.handlers;

import com.ketteridge.mir.domain.Authorization;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class TransactionHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        return ctx.getRequest().getMethod().isGet() && ctx.getRequest().getPath().equals("transactions");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        // we must be authenticated to have got this far:
        String transactionListAsJson;
        try (Jedis jedis = getPool(ctx).getResource()) {
            // return the transactions against the hash
            transactionListAsJson = jedis.get("transactions:" + getAuth(ctx));
        }

        ctx.getResponse().status(200).send(transactionListAsJson);
    }

}
