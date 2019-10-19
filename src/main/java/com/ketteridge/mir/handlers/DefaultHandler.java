package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;

@Slf4j
public class DefaultHandler extends ExtendedHandler {

    @Override
    public boolean requiresAuthorization() { return false; }

    @Override
    public boolean supports(Context ctx) {
        return true;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.notFound();
    }
}
