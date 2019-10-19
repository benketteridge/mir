package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ketteridge.mir.domain.Authorization;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.NotInRegistryException;
import redis.clients.jedis.JedisPool;

@Slf4j
public abstract class ExtendedHandler implements Handler {

    static ObjectMapper mapper = new ObjectMapper();

    public boolean requiresAuthorization() {
        return true;
    }

    public abstract boolean supports(Context ctx);

    // won't be able to access any handler that requires authentication unless the auth header is provided
    public void preHandle(Context ctx) {
        if (requiresAuthorization()) {
            try {
                Authorization auth = ctx.get(Authorization.class);
                log.trace("requires auth, auth header: {}", auth.getAuth());
            } catch (NotInRegistryException nire) {
                ctx.getResponse().status(401).send("Unauthorized");
            }
        }
    }

    JedisPool getPool(Context ctx) {
        return ctx.get(JedisPool.class);
    }

    String getAuth(Context ctx) {
        try {
            return ctx.get(Authorization.class).getAuth();
        }
        catch (NotInRegistryException nire) {
            return "";
        }
    }

}
