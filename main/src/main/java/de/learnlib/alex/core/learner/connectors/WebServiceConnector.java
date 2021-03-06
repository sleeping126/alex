/*
 * Copyright 2016 TU Dortmund
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.learnlib.alex.core.learner.connectors;

import de.learnlib.alex.core.learner.BaseUrlManager;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

/**
 * A Wrapper/ Facade around a @{link WebTarget}.
 */
public class WebServiceConnector implements Connector {

    /** The target behind the connector. */
    private WebTarget target;

    /** A managed base url to use. */
    private BaseUrlManager baseUrl;

    /** Internal field to determine if the target called at least once (-> other fields have a value). */
    private boolean init;

    /** The response HTTP status of the last call done by the connection. */
    private int status;

    /** The response HTTP headers of the last call done by the connection.  */
    private MultivaluedMap<String, Object> headers;

    /** The response body of the last call done by the connection. */
    private String body;

    /** The cookies from th last call done by the connection. */
    private Map<String, NewCookie> cookies;

    /**
     * Constructor which sets the WebTarget to use.
     *
     * @param baseUrl
     *         The base url used by the connector. All other paths will treated as suffix to this.
     */
    public WebServiceConnector(String baseUrl) {
        this.baseUrl = new BaseUrlManager(baseUrl);

        this.target = ClientBuilder.newClient().target(baseUrl);
    }

    /**
     * Constructor for testing purpose which sets the WebTarget to use.
     *
     * @param target
     *         The WebTarget the connection will use.
     * @param baseUrl
     *         The base URL used by the connector. All other paths will treated as suffix to this.
     * @param resetUrl
     *         The url to reset the SUL. This URL is relative to the base URL.
     */
     public WebServiceConnector(WebTarget target, String baseUrl, String resetUrl) {
        this.baseUrl = new BaseUrlManager(baseUrl);

        this.target = target;
        reset(resetUrl);
    }

    @Override
    public void reset() {
        // nothing to do here
    }

    @Override
    public void dispose() {
        // nothing to do here
    }

    /**
     * Get the response status of the last request.
     * You have to do at least on request ({@link #get(String, Map, Set)}|
     * {@link #post(String, Map, Set, String)}|
     * {@link #put(String, Map, Set, String)}|
     * {@link #delete(String, Map, Set)}).
     *
     * @return The last status received by the connections.
     * @throws java.lang.IllegalStateException
     *         If no request was done before the method call.
     */
    public int getStatus() throws IllegalStateException {
        if (!init) {
            throw new IllegalStateException();
        }
        return status;
    }

    /**
     * Get the response HTTP header of the last request.
     * You have to do at least on request ({@link #get(String, Map, Set)}|
     * {@link #post(String, Map, Set, String)}|
     * {@link #put(String, Map, Set, String)}|
     * {@link #delete(String, Map, Set)}).
     *
     * @return The last HTTP header received by the connections.
     * @throws java.lang.IllegalStateException
     *         If no request was done before the method call.
     */
    public MultivaluedMap<String, Object> getHeaders() throws IllegalStateException {
        if (!init) {
            throw  new IllegalStateException();
        }
        return headers;
    }

    /**
     * Get the response body of the last request.
     * You have to do at least on request ({@link #get(String, Map, Set)}|
     * {@link #post(String, Map, Set, String)}|
     * {@link #put(String, Map, Set, String)}|
     * {@link #delete(String, Map, Set)}).
     *
     * @return The last body received by the connections.
     * @throws java.lang.IllegalStateException
     *         If no request was done before the method call.
     */
    public String getBody() throws IllegalStateException {
        if (!init) {
            throw  new IllegalStateException();
        }
        return body;
    }

    /**
     * Get the cookies.
     * @return The cookies.
     * @throws IllegalStateException
     *          If no request was done before the method call.
     */
    public Map<String, NewCookie> getCookies() throws  IllegalStateException {
        if (!init) {
            throw new IllegalStateException();
        }
        return cookies;
    }

    /**
     * Do a HTTP GET request.
     *
     * @param path
     *         The path to send the request to.
     * @param requestHeaders
     *         The headers to send with the request.
     * @param requestCookies
     *         The cookies to send with the request.
     */
    public void get(String path, Map<String, String> requestHeaders, Set<Cookie> requestCookies) {
        Response response = getRequestObject(path, requestHeaders, requestCookies).get();
        rememberResponseComponents(response);
    }

    /**
     * Do a HTTP POST request.
     *
     * @param path
     *         The path to send the request to.
     * @param requestHeaders
     *         The headers to send with the request.
     * @param requestCookies
     *         The cookies to send with the request.
     * @param jsonData
     *         The data to send with the request.
     */
    public void post(String path, Map<String, String> requestHeaders, Set<Cookie> requestCookies,
                     String jsonData) {
        Response response = getRequestObject(path, requestHeaders, requestCookies).post(Entity.json(jsonData));
        rememberResponseComponents(response);
    }

    /**
     * Do a HTTP PUT request.
     *
     * @param path
     *         The path to send the request to.
     * @param requestHeaders
     *         The headers to send with the request.
     * @param requestCookies
     *         The cookies to send with the request.
     * @param jsonData
     *         The data to send with the request.
     */
    public void put(String path, Map<String, String> requestHeaders, Set<Cookie> requestCookies,
                    String jsonData) {
        Response response = getRequestObject(path, requestHeaders, requestCookies).put(Entity.json(jsonData));
        rememberResponseComponents(response);
    }

    /**
     * Do a HTTP DELETE request.
     *
     * @param path
     *         The path to send the request to.
     * @param requestHeaders
     *         The headers to send with the request.
     * @param requestCookies
     *         The cookies to send with the request.
     */
    public void delete(String path, Map<String, String> requestHeaders, Set<Cookie> requestCookies) {
        Response response = getRequestObject(path, requestHeaders, requestCookies).delete();
        rememberResponseComponents(response);
    }

    /**
     * Reset the connector and the SUL.
     *
     * @param resetUrl
     *         The url (based on the base url) to reset the SUL.
     */
    public void reset(String resetUrl) {
        target.path(resetUrl).request().get();
        this.init = false;
    }

    /**
     * Multi setter for all the fields based on the response.
     *
     * @param response
     *         The response all other fields will be based on.
     */
    private void rememberResponseComponents(Response response) {
        status = response.getStatus();
        headers = response.getHeaders();
        body = response.readEntity(String.class);
        cookies = response.getCookies();
        init = true;
    }

    /**
     * Get the base url of the API to call. All requests will be based on this!
     * @see BaseUrlManager#getBaseUrl()

     * @return The base url for all the requests.
     */
    public String getBaseUrl() {
        return baseUrl.getBaseUrl();
    }

    /**
     * Creates a request object that is passed to further REST actions.
     *
     * @param path The URI that is called.
     * @param requestHeaders The HTTP headers of the request.
     * @param requestCookies The cookies of the request.
     * @return The request object.
     */
    private Invocation.Builder getRequestObject(String path, Map<String, String> requestHeaders,
                                                Set<Cookie> requestCookies) {
        String[] splitedPath = path.split("\\?");

        WebTarget tmpTarget = target.path(splitedPath[0]);

        if (splitedPath.length > 1) {
            for (String queryParam : splitedPath[1].split("&")) {
                String[] queryParamPair = queryParam.split("\\=");

                if (queryParamPair.length == 2) {
                    tmpTarget = tmpTarget.queryParam(queryParamPair[0], queryParamPair[1]);
                }
            }
        }
        Invocation.Builder builder = tmpTarget.request();
        requestHeaders.forEach(builder::header);
        requestCookies.forEach(builder::cookie);

        return builder;
    }
}
