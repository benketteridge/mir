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

@Slf4j
public class BalanceHandler extends ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
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

        ctx.getResponse().status(200).send(balance);
    }

}
