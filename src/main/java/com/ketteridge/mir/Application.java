package com.ketteridge.mir;

import com.ketteridge.mir.handlers.Router;
import ratpack.handling.RequestLogger;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Application to implement the simple payment API
 */
public class Application {

    public static void main(String[] args) throws Exception {
        // create a pooled connection to the localhost installation of Redis.
        // (could be parameterised on the command line with a different host, and more parameters, if required)
        // ... ooh bad man, hard coding a specific IP & port!
        JedisPool pool = new JedisPool(new JedisPoolConfig(), "35.189.235.73", 6379);

        // as this main method will finish after initialising the RatpackServer, but we want to be good Redis users
        // and release our resources at the end of the run, we'll add a shutdown hook.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // defensive to the end
            if (pool != null) pool.close();
        }));

        // start the server, with the JedisPool in the registry for further use.
        RatpackServer.start(server -> server
                .registry(Registry.of(spec -> spec.add(pool)))
                .handlers(chain -> chain
                        // log every request:
                        .all(RequestLogger.ncsa())
                        // apply authorization rules and chose the right context handler
                        .all(new Router())));

        // can't close the JedisPool here because we've only just launched the background Netty server, not finished
        // running the application
    }

}