package com.ketteridge.mir.handlers;

import com.ketteridge.mir.domain.Authorization;
import com.ketteridge.mir.handlers.*;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.Registry;

/**
 * Simple router logic, not dissimilar to Spring concept of the DispatcherServlet.
 */
public class Router implements Handler {

    public static final String AUTHORIZATION = "Authorization";

    // the list of handlers to be checked in order.
    // the order doesn't really matter as the Contexts handled are disjoint.
    private ExtendedHandler[] handlers = new ExtendedHandler[]{
            new LoginHandler(), new BalanceHandler(), new TransactionHandler(), new SpendHandler()
    };

    public void handle(Context ctx) {

        Authorization authorization = null;
        String auth = ctx.getRequest().getHeaders().get(AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            authorization = new Authorization(auth.replace("Bearer ", ""));
        }

        // don't like return statement scattered through methods, so using a status flag
        boolean handled = false;
        for (ExtendedHandler h: handlers) {
            // check that the handler is interested in the Context, otherwise skip it
            if (!h.supports(ctx)) continue;

            // check for the Authorization header, if required by the selected handler
            if (h.failedAuthorization(authorization)) {
                // abort the request handling
                h.sendUnauthorized(ctx);
                handled = true;
            }
            else {
                // insert the selected handler into the chain, with authorization, if we have it
                if (authorization != null)
                    ctx.insert(Registry.single(authorization), h);
                else ctx.insert(h);

                handled = true;
            }
        }

        // fallback to the default (404 Not found)
        if (!handled) ctx.notFound();
    }
}