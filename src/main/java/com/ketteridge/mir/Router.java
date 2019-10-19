package com.ketteridge.mir;

import com.ketteridge.mir.handlers.*;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.RequestLogger;

public class Router implements Handler {
    private ExtendedHandler loginHandler = new LoginHandler();
    private ExtendedHandler balanceHandler = new BalanceHandler();
    private ExtendedHandler transactionHandler = new TransactionHandler();
    private ExtendedHandler spendHandler = new SpendHandler();
    private ExtendedHandler defaultHandler = new DefaultHandler();

    private ExtendedHandler[] handlers = new ExtendedHandler[]{
            loginHandler, balanceHandler, transactionHandler, spendHandler, defaultHandler
    };

    public void handle(Context ctx) {
        for (ExtendedHandler h: handlers) {
            if (h.supports(ctx)) {
                h.preHandle(ctx); // check Authorization, if required
                ctx.insert(h);
                break;
            }
        }
    }
}