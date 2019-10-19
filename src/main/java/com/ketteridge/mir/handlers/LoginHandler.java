package com.ketteridge.mir.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ketteridge.mir.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class LoginHandler extends ExtendedHandler {

    @Override
    public boolean requiresAuthorization() {
        return false;
    }

    @Override
    public boolean supports(Context ctx) {
        return ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("login");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        String theHash = newHash(String.format("new%shash", LocalDateTime.now().toString()));

        // very simple implementation of an account is just a list of transactions.
        // we store that list as a JSON string against the authorization hash, and
        // this means returning the list is trivial, and as the only way to change
        // the balance is by using the spend API, we can create a balance record
        // any time we spend

        try (Jedis jedis = getPool(ctx).getResource()) {
            List<Transaction> transactions = Collections.singletonList(new Transaction(LocalDateTime.now().toString(), "initial setup", "100", "USD"));
            String transactionListAsJson = mapper.writeValueAsString(transactions);

            jedis.set("transactions:"+ theHash, transactionListAsJson);
        }

        ctx.getResponse().status(201).send(theHash);
    }

    private String newHash(String string) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(string.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }
}
