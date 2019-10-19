package com.ketteridge.mir.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.Registry;

@Slf4j
/**
 * handler to extract the Authorization header, and put the key into the Registry for use further down the chain.
 */
public class AuthorizationHandler implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        String auth = ctx.getRequest().getHeaders().get("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String trimmed = auth.replace("Bearer ", "");
            ctx.next(Registry.single(new Authorization(trimmed)));
        }
        else ctx.next();
    }

    @Getter
    @AllArgsConstructor
    static class Authorization {
        private String auth;
    }
}
