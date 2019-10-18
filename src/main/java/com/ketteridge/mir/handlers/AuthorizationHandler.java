package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Slf4j
public class AuthorizationHandler implements Handler {

    static final String AUTHORIZATION = "Authorization";

    @Override
    public void handle(Context ctx) throws Exception {
        String auth = ctx.getRequest().getHeaders().get(AUTHORIZATION);
        if (auth != null) {
            MDC.put(AUTHORIZATION, auth);
        }
    }

}
