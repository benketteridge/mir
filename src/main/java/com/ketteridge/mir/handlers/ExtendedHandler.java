package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ketteridge.mir.handlers.AuthorizationHandler.Authorization;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.NotInRegistryException;

@Slf4j
public abstract class ExtendedHandler implements Handler {

    static ObjectMapper mapper = new ObjectMapper();

    public boolean requiresAuthorization() {
        return true;
    }

    public boolean supports(Context ctx) {
//        String auth = MDC.get(AuthorizationHandler.AUTHORIZATION);
//        boolean requires = requiresAuthorization();
//        log.trace("requires auth: {}, auth header: {}", requires, auth);
//        return (auth != null || !requires);
        return true;
    }

    // won't be able to access any handler that requires authentication unless the auth header is provided
    public void preHandle(Context ctx) {
        if (requiresAuthorization()) {
            try {
                Authorization auth = ctx.get(Authorization.class);
                log.trace("requires auth, auth header: {}", auth.getAuth());
            }
            catch (NotInRegistryException nire){
                ctx.getResponse().status(401).send("Unauthorized");
            }
        }
    }
}
