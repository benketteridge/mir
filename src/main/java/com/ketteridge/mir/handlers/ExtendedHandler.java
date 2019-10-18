package com.ketteridge.mir.handlers;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public interface ExtendedHandler extends Handler {

    boolean supports(Context ctx);

}
