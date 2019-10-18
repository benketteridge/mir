package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Slf4j
public abstract class ExtendedHandler implements Handler {

    public boolean requiresAuthorization() {
        return true;
    }

    // won't be able to match any handler that requires authentication unless the auth header is provided
    public boolean supports(Context ctx) {
        String auth = MDC.get(AuthorizationHandler.AUTHORIZATION);
        boolean requires = requiresAuthorization();
        log.trace("requires auth: {}, auth header: {}", requires, auth);
        return (auth != null || !requires);
    }

}
