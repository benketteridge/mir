package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;

@Slf4j
public class TransactionHandler implements ExtendedHandler {

    @Override
    public boolean supports(Context ctx) {
        return ctx.getRequest().getMethod().isGet() && ctx.getRequest().getPath().equals("transactions");
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.render("ok");
    }

}
