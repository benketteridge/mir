package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ketteridge.mir.domain.Authorization;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.NotInRegistryException;
import redis.clients.jedis.JedisPool;

/**
 * Super class for the API handlers
 * Provides a simple authorisation check, a shared ObjectMapper, and some convenience methods.
 */
@Slf4j
public abstract class ExtendedHandler implements Handler {

    static ObjectMapper mapper = new ObjectMapper();

    /**
     * subclasses implement this to indicate to the {@link Router}
     * that they can handle a given {@link Context}
     */
    public abstract boolean supports(Context ctx);

    /**
     * subclasses default to requiring authorization
     */
    public boolean requiresAuthorization() {
        return true;
    }

    // won't be able to access any handler that requires authentication unless the auth header is provided
    boolean failedAuthorization(Authorization auth) {
        return requiresAuthorization() && (auth == null || auth.getAuth().equals(""));
    }

    JedisPool getPool(Context ctx) {
        return ctx.get(JedisPool.class);
    }

    String getAuth(Context ctx) {
        try {
            String auth = ctx.get(Authorization.class).getAuth();
            log.trace("auth header: {}", auth);
            return auth;
        } catch (NotInRegistryException nire) {
            return "";
        }
    }

    void sendUnauthorized(Context ctx) {
        ctx.getResponse().status(401).send("Unauthorized");
    }
}
