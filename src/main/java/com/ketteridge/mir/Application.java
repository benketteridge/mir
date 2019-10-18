package com.ketteridge.mir;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.RequestLogger;
import ratpack.server.RatpackServer;

@Slf4j
public class Application {

    public static void main(String[] args) throws Exception {
        RatpackServer.start(server -> server.handlers(chain -> chain
                .all(RequestLogger.ncsa())
                .all(new Router())));
    }
}