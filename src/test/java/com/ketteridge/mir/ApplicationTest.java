package com.ketteridge.mir;

import com.ketteridge.mir.handlers.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ratpack.http.client.ReceivedResponse;
import ratpack.impose.ImpositionsSpec;
import ratpack.impose.UserRegistryImposition;
import ratpack.registry.Registry;
import ratpack.test.MainClassApplicationUnderTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ApplicationTest {

    //    private JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    private JedisPool pool = mock(JedisPool.class);
    private Jedis jedis = mock(Jedis.class);

    private MainClassApplicationUnderTest appUnderTest = new MainClassApplicationUnderTest(Application.class) {
        @Override
        protected void addImpositions(final ImpositionsSpec impositions) {
            // inject the mock pool
            impositions.add(UserRegistryImposition.of(Registry.single(JedisPool.class, pool)));
        }
    };

    @Before
    public void init() {
        when(pool.getResource()).thenReturn(jedis);
    }

    @Test
    public void givenUnsupportedUrls_get404Response() {
        // there's no welcome page right now
        assertEquals(404, appUnderTest.getHttpClient().get("/").getStatusCode());
        assertEquals(404, appUnderTest.getHttpClient().get("/newuser").getStatusCode());
        assertEquals(404, appUnderTest.getHttpClient().get("/../cgi-bin").getStatusCode());
    }

    @Test
    public void givenLoginUrl_getAuthorizationText() {
        ReceivedResponse post = appUnderTest.getHttpClient().post("/login");
        // record created:
        assertEquals(201, post.getStatusCode());
        String text = post.getBody().getText();
        assertNotNull(text);
        // and the body is the key
        assertTrue(text.matches("^[A-Z0-9]{20,}$"));
    }

    @Test
    public void givenPostSpendUrl_butUnauthorized_get401Unauthorized() {
        ReceivedResponse post = appUnderTest.getHttpClient().post("/spend");
        assertEquals(401, post.getStatusCode());
        assertEquals("Unauthorized", post.getBody().getText());
    }

    @Test
    public void givenGetTransactionsUrl_butUnauthorized_get401Unauthorized() {
        ReceivedResponse get = appUnderTest.getHttpClient().get("/transactions");
        assertEquals(401, get.getStatusCode());
        assertEquals("Unauthorized", get.getBody().getText());
    }

    @Test
    public void givenGetBalanceUrl_butUnauthorized_get401Unauthorized() {
        ReceivedResponse get = appUnderTest.getHttpClient().get("/balance");
        assertEquals(401, get.getStatusCode());
        assertEquals("Unauthorized", get.getBody().getText());
    }

    @Test
    public void givenGetLoginUrl_wrongHttpMethod_get404Response() {
        ReceivedResponse get = appUnderTest.getHttpClient().get("/login");
        assertEquals(404, get.getStatusCode());
    }

    @Test
    public void givenGetSpendUrl_wrongHttpMethod_get404Response() {
        ReceivedResponse get = appUnderTest.getHttpClient().get("/spend");
        assertEquals(404, get.getStatusCode());
    }

    @Test
    public void givenPostTransactionsUrl_wrongHttpMethod_get404Response() {
        ReceivedResponse post = appUnderTest.getHttpClient().post("/transactions");
        assertEquals(404, post.getStatusCode());
    }

    @Test
    public void givenPostBalanceUrl_wrongHttpMethod_get404Response() {
        ReceivedResponse post = appUnderTest.getHttpClient().post("/balance");
        assertEquals(404, post.getStatusCode());
    }

    @Test
    public void givenPostSpendUrl_Authorized_getOk() {
        when(jedis.get(eq("balance:" + AUTH_KEY))).thenReturn("USD 100");
        when(jedis.get(eq("transactions:" + AUTH_KEY))).thenReturn(EXISTING_TRANSACTIONS_LIST);
        Transaction multi = mock(Transaction.class);
        when(jedis.multi()).thenReturn(multi);
        when(multi.exec()).thenReturn(Collections.singletonList(new Object()));

        ReceivedResponse post = appUnderTest.getHttpClient().requestSpec(r ->
                r.body(b -> b.type("application/json").text(GOOD_TRANSACTION))
                        .getHeaders().set(Router.AUTHORIZATION, AUTH_HEADER_VALUE)
        ).post("/spend");
        assertEquals(201, post.getStatusCode());
        assertEquals("ok", post.getBody().getText());
    }

    @Test
    public void givenPostSpendUrl_Authorized_badTransaction_get400BadRequest() {
        when(jedis.get(eq("balance:" + AUTH_KEY))).thenReturn("USD 100");
        when(jedis.get(eq("transactions:" + AUTH_KEY))).thenReturn(EXISTING_TRANSACTIONS_LIST);
        Transaction multi = mock(Transaction.class);
        when(jedis.multi()).thenReturn(multi);
        when(multi.exec()).thenReturn(Collections.singletonList(new Object()));

        ReceivedResponse post = appUnderTest.getHttpClient().requestSpec(r ->
                r.body(b -> b.type("application/json").text("{\"nonsense\":\"data\"}"))
                        .getHeaders().set(Router.AUTHORIZATION, AUTH_HEADER_VALUE)
        ).post("/spend");
        assertEquals(400, post.getStatusCode());
        assertEquals("bad request", post.getBody().getText());
    }

    @Test
    public void givenPostSpendUrl_Authorized_getOptimisticLockFail() {
        when(jedis.get(eq("balance:" + AUTH_KEY))).thenReturn("USD 100");
        when(jedis.get(eq("transactions:" + AUTH_KEY))).thenReturn(EXISTING_TRANSACTIONS_LIST);
        Transaction multi = mock(Transaction.class);
        when(jedis.multi()).thenReturn(multi);
        when(multi.exec()).thenReturn(Collections.emptyList());

        ReceivedResponse post = appUnderTest.getHttpClient().requestSpec(r ->
                r.body(b -> b.type("application/json").text(GOOD_TRANSACTION))
                        .getHeaders().set(Router.AUTHORIZATION, AUTH_HEADER_VALUE)
        ).post("/spend");
        assertEquals(500, post.getStatusCode());
        assertEquals("unable to complete request", post.getBody().getText());
    }

    @Test
    public void givenGetTransactionsUrl_Authorized_getOk() {
        when(jedis.get(eq("transactions:" + AUTH_KEY))).thenReturn(EXISTING_TRANSACTIONS_LIST);

        ReceivedResponse get = appUnderTest.getHttpClient().requestSpec(r ->
                r.getHeaders().set(Router.AUTHORIZATION, AUTH_HEADER_VALUE)
        ).get("/transactions");
        assertEquals(200, get.getStatusCode());
        assertTrue(get.getBody().getText().matches("^\\[\\{.*}]$"));
    }

    @Test
    public void givenGetBalanceUrl_Authorized_getOk() {
        when(jedis.get(eq("balance:" + AUTH_KEY))).thenReturn("USD 100");
        ReceivedResponse get = appUnderTest.getHttpClient().requestSpec(r ->
                r.getHeaders().set(Router.AUTHORIZATION, AUTH_HEADER_VALUE)
        ).get("/balance");
        assertEquals(200, get.getStatusCode());
        assertEquals("USD 100", get.getBody().getText());
    }

    @Test
    public void givenGetBalanceUrl_UnknownKey_get401Unauthorised() {
        ReceivedResponse get = appUnderTest.getHttpClient().requestSpec(r ->
                r.getHeaders().set(Router.AUTHORIZATION, BAD_AUTH_HEADER_VALUE)
        ).get("/balance");
        assertEquals(401, get.getStatusCode());
    }

    @Test
    public void givenGetTransactionsUrl_UnknownKey_get401Unauthorised() {
        ReceivedResponse get = appUnderTest.getHttpClient().requestSpec(r ->
                r.getHeaders().set(Router.AUTHORIZATION, BAD_AUTH_HEADER_VALUE)
        ).get("/transactions");
        assertEquals(401, get.getStatusCode());
    }

    @Test
    public void givenPostSpendUrl_UnknownKey_get401Unauthorised() {
        ReceivedResponse send = appUnderTest.getHttpClient().requestSpec(r ->
                r.getHeaders().set(Router.AUTHORIZATION, BAD_AUTH_HEADER_VALUE)
        ).post("/spend");
        assertEquals(401, send.getStatusCode());
    }

    @After
    public void shutdown() {
        appUnderTest.close();
    }

    // factored out long/complex strings for clarity in tests above
    private static final String AUTH_KEY = "good-auth-key";
    private static final String BAD_AUTH_KEY = "bad-auth-key";
    private static final String AUTH_HEADER_VALUE = "Bearer " + AUTH_KEY;
    private static final String BAD_AUTH_HEADER_VALUE = "Bearer " + BAD_AUTH_KEY;
    private static final String GOOD_TRANSACTION = "{\"date\":\"2019-10-19T12:57:40.000\",\"description\":\"test transaction\",\"amount\":\"25\",\"currency\":\"USD\"}";
    private static final String EXISTING_TRANSACTIONS_LIST = "[{\"date\":\"2019-10-19T13:57:40.109\",\"description\":\"initial setup\",\"amount\":\"100\",\"currency\":\"USD\"}]";

}