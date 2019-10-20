package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;

/**
 * Handler for /transactions API.
 * Retrieves the transactions list from the Redis store and returns it to the requester.
 */
@Slf4j
public class TransactionHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        // check that this is GET /transactions
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

        if (transactionListAsJson == null) {
            sendUnauthorized(ctx);
        }
        else {
            // and tell the client
            ctx.getResponse().contentType("application/json").status(200).send(transactionListAsJson);
        }
    }

}
