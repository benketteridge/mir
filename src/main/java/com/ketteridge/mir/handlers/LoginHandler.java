package com.ketteridge.mir.handlers;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;

@Slf4j
public class LoginHandler extends ExtendedHandler {

    @Override
    public boolean requiresAuthorization() {
        return false;
    }

    @Override
    public boolean supports(Context ctx) {
        return super.supports(ctx) && ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("login");
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.render("ok");
    }

}
