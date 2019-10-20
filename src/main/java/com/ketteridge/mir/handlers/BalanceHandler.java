package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;

/**
 * Handler for /balance API.
 * Retrieves the balance key from the Redis store and returns it to the requester.
 */
@Slf4j
public class BalanceHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        // check that this is GET /balance
        return ctx.getRequest().getMethod().isGet() && ctx.getRequest().getPath().equals("balance");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        // we must be authenticated to have got this far:
        String balance;
        try (Jedis jedis = getPool(ctx).getResource()) {
            // return the transactions against the hash
            balance = jedis.get("balance:"+ getAuth(ctx));

            // keeping the connection open for as short a period as possible
        }

        if (balance == null || balance.length() == 0) {
            sendUnauthorized(ctx);
        }
        else {
            ctx.getResponse().status(200).send(balance);
        }
    }

}
