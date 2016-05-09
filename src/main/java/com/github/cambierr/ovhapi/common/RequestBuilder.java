/*
 * The MIT License
 *
 * Copyright 2015 cambierr.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.cambierr.ovhapi.common;

import com.github.cambierr.ovhapi.auth.Credential;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import java.util.concurrent.Future;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 *
 * @author cambierr
 */
public class RequestBuilder {

    private final String path;
    private final Method method;
    private String body;
    private static final String userAgent = "Java " + System.getProperty("java.version") + " | com.github.cambierr.ovhapi wrapper (https://github.com/cambierr/OvhApi)";
    private final Credential credentials;
    private final String applicationKey;

    /**
     * Creates a RequestBuilder for CredentialRequest
     *
     * @param _path the path of the request
     * @param _method the method of the request
     * @param _applicatioKey the OVH API application key
     */
    public RequestBuilder(String _path, Method _method, String _applicatioKey) {
        path = _path;
        method = _method;
        credentials = null;
        applicationKey = _applicatioKey;
    }

    /**
     * Creates a RequestBuilder for API calls
     *
     * @param _path the path of the request
     * @param _method the method of the request
     * @param _credentials the Credentials to be used in the request
     */
    public RequestBuilder(String _path, Method _method, Credential _credentials) {
        path = _path;
        method = _method;
        credentials = _credentials;
        applicationKey = null;
    }

    /**
     * Appends a body to the request (only for PUT and POST requests)
     *
     * @param _body the body to be appended
     *
     * @return the updated RequestBuilder
     *
     * @throws IllegalArgumentException if called on a request that doesn't
     * supports bodys
     */
    public RequestBuilder body(String _body) {
        if (!this.method.equals(Method.POST) && !this.method.equals(Method.PUT)) {
            throw new IllegalArgumentException("Body should only be appended to POST or PUT request");
        }
        body = _body;
        return this;
    }

    /**
     * Builds and execute the request, returning a response
     *
     * @return an observable Resposne object
     */
    public Observable<SafeResponse> build() {
        return Observable
                .create((Subscriber<? super Future<HttpResponse<String>>> arg0) -> {
                    arg0.onStart();
                    try {
                        String completePath = "https://" + OvhApi.API_ENDPOINT + "/" + OvhApi.API_VERSION + path;
                        HttpRequest req = null;
                        switch (method) {
                            case GET:
                                req = Unirest.get(completePath);
                                break;
                            case DELETE:
                                req = Unirest.delete(completePath);
                                break;
                            case POST:
                                req = ((body == null) ? Unirest.post(completePath) : Unirest.post(completePath).header("Content-type", "application/json").body(body)).getHttpRequest();
                                break;
                            case PUT:
                                req = ((body == null) ? Unirest.put(completePath) : Unirest.put(completePath).header("Content-type", "application/json").body(body)).getHttpRequest();
                                break;
                            default:
                                throw new RuntimeException("wrong method received");
                        }
                        if (credentials != null) {
                            credentials.sign(req, method, body);
                        } else if (applicationKey != null) {
                            req.header("X-Ovh-Application", applicationKey);
                        }
                        req.header("User-Agent", userAgent);
                        arg0.onNext(req.asStringAsync());
                        arg0.onCompleted();
                    } catch (Exception ex) {
                        arg0.onError(ex);
                    }
                })
                .flatMap((Future<HttpResponse<String>> arg0) -> Observable
                        .from(arg0, Schedulers.io())
                        .map((HttpResponse<String> t) -> new SafeResponse(t))
                );

    }

}
