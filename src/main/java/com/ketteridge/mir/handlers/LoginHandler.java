package com.ketteridge.mir.handlers;

import com.ketteridge.mir.domain.Transaction;
import ratpack.handling.Context;
import redis.clients.jedis.Jedis;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handler for /login API.
 * The simplest implementation of an account is just a list of transactions.
 * We store the transaction list as a JSON string against the authorization hash, and this means returning the list is trivial.
 * We simplify the 'balance' API by initialising the 'balance' key to go along with the transactions.
 * As the only way to change the balance is by using the 'spend' API, we can update the balance record any time we add transactions,
 * rather than calculating it when calling the 'balance' API.
 */
public class LoginHandler extends ExtendedHandler {

    @Override
    public boolean requiresAuthorization() {
        return false;
    }

    @Override
    public boolean supports(Context ctx) {
        // check that this is POST /login
        return ctx.getRequest().getMethod().isPost() && ctx.getRequest().getPath().equals("login");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        // the hash is effectively a random value. If we had more user information, we could reduce the predictability
        // of the hash by increasing the range of data items used in the seeding approach.
        // And the longer and more randomised the source string, the less likelihood of encountering collisions.
        String theHash = newHash(String.format("new%shash%sstring",
                LocalDateTime.now().toString(), new Random().nextInt(1024)));

        try (Jedis jedis = getPool(ctx).getResource()) {
            List<Transaction> transactions = Collections.singletonList(
                    new Transaction(LocalDateTime.now().toString(), "initial setup", "100", "USD"));
            // Turn the list of one transaction into the JSON form:
            String transactionListAsJson = mapper.writeValueAsString(transactions);

            // store the new list and balance
            jedis.set("transactions:" + theHash, transactionListAsJson);
            jedis.set("balance:" + theHash, "USD 100");
        }

        // return 201 Created, with the hash as the response body
        ctx.getResponse().status(201).send(theHash);
    }

    private String newHash(String string) throws NoSuchAlgorithmException {
        // SHA-256 is a simple way to generate a hash from a string
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(string.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }
}
