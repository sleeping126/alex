package de.learnlib.alex.core.learner;

import de.learnlib.alex.core.learner.connectors.WebServiceConnector;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WebServiceConnectorTest {

    private static final String FAKE_URL = "http://example.com/";
    private static final String RESET_URL = "/";
    private static final int OK_STATUS = 200;
    private static final String FAKE_MESSAGE = "{}";

    @Test
    public void shouldGetASiteAndRememberTheResponse() {
        Invocation.Builder builder = mock(Invocation.Builder.class);
        WebTarget target = createWebTarget(builder);
        WebServiceConnector connector = new WebServiceConnector(target, FAKE_URL, RESET_URL);
        MultivaluedMap<String, Object> requestHeaders = mock(MultivaluedHashMap.class);
        Set<Cookie> cookies = new HashSet<>();

        connector.get("/", requestHeaders, cookies);

        verify(builder, atLeast(1)).get();
        assertEquals(OK_STATUS, connector.getStatus());
        assertEquals(null, connector.getHeaders());
        assertEquals("{}", connector.getBody());
    }

    @Test
    public void shouldPostToASiteAndRememberTheResponse() {
        Invocation.Builder builder = mock(Invocation.Builder.class);
        WebTarget target = createWebTarget(builder);
        WebServiceConnector connector = new WebServiceConnector(target, FAKE_URL, RESET_URL);
        MultivaluedMap<String, Object> requestHeaders = mock(MultivaluedHashMap.class);
        Set<Cookie> cookies = new HashSet<>();

        connector.post("/", requestHeaders, cookies, FAKE_MESSAGE);

        verify(builder).post(Entity.json(FAKE_MESSAGE));
        assertEquals(OK_STATUS, connector.getStatus());
        assertEquals(null, connector.getHeaders());
        assertEquals(FAKE_MESSAGE, connector.getBody());
    }

    @Test
    public void shouldPutToASiteAndRememberTheResponse() {
        Invocation.Builder builder = mock(Invocation.Builder.class);
        WebTarget target = createWebTarget(builder);
        WebServiceConnector connector = new WebServiceConnector(target, FAKE_URL, RESET_URL);
        MultivaluedMap<String, Object> requestHeaders = mock(MultivaluedHashMap.class);
        Set<Cookie> cookies = new HashSet<>();

        connector.put("/", requestHeaders, cookies, FAKE_MESSAGE);

        verify(builder).put(Entity.json(FAKE_MESSAGE));
        assertEquals(OK_STATUS, connector.getStatus());
        assertEquals(null, connector.getHeaders());
        assertEquals(FAKE_MESSAGE, connector.getBody());
    }


    @Test
    public void shouldDeleteToASiteAndRememberTheResponse() {
        Invocation.Builder builder = mock(Invocation.Builder.class);
        WebTarget target = createWebTarget(builder);
        WebServiceConnector connector = new WebServiceConnector(target, FAKE_URL, RESET_URL);
        MultivaluedMap<String, Object> requestHeaders = mock(MultivaluedHashMap.class);
        Set<Cookie> cookies = new HashSet<>();

        connector.delete("/", requestHeaders, cookies);

        verify(builder).delete();
        assertEquals(OK_STATUS, connector.getStatus());
        assertEquals(null, connector.getHeaders());
        assertEquals(FAKE_MESSAGE, connector.getBody());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenThereIsNoStatus() {
        WebServiceConnector connector = new WebServiceConnector(FAKE_URL);

        connector.getStatus();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenThereIsNoHead() {
        WebServiceConnector connector = new WebServiceConnector(FAKE_URL);

        connector.getHeaders();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenThereIsNoBody() {
        WebServiceConnector connector = new WebServiceConnector(FAKE_URL);

        connector.getBody();
    }

    private WebTarget createWebTarget(Invocation.Builder builder) {
        Response response = createResponse();
        Invocation.Builder headerBuilder = mock(Invocation.Builder.class);
        WebTarget subTarget = mock(WebTarget.class);
        WebTarget target = mock(WebTarget.class);


        given(builder.get()).willReturn(response);
        given(builder.post(Entity.json(FAKE_MESSAGE))).willReturn(response);
        given(builder.put(Entity.json(FAKE_MESSAGE))).willReturn(response);
        given(builder.delete()).willReturn(response);

        given(headerBuilder.headers(any(MultivaluedMap.class))).willReturn(builder);
        given(subTarget.request()).willReturn(headerBuilder);
        given(target.path(FAKE_URL)).willReturn(subTarget);
        given(target.path("/")).willReturn(subTarget);

        return target;
    }

    private Response createResponse() {
        Response response = mock(Response.class);
        given(response.getStatus()).willReturn(OK_STATUS);
        given(response.getHeaders()).willReturn(null);
        given(response.readEntity(String.class)).willReturn("{}");

        return response;
    }

}
