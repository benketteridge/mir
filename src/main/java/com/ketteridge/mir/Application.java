package com.ketteridge.mir;

import com.ketteridge.mir.handlers.AuthorizationHandler;
import ratpack.handling.RequestLogger;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Application {

    public static void main(String[] args) throws Exception {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // defensive to the end
            if (pool != null) pool.close();
        }));

        RatpackServer.start(server -> server
                .registry(Registry.of(spec -> spec.add(pool)))
                .handlers(chain -> chain
                        .all(RequestLogger.ncsa())
                        .all(new AuthorizationHandler())
                        .all(new Router())));

        // can't close the JedisPool here because we've only just launched the background Netty server, not finished
        // running the application
    }

}