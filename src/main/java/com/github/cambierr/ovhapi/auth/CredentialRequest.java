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
package com.github.cambierr.ovhapi.auth;

import com.github.cambierr.ovhapi.common.Method;
import com.github.cambierr.ovhapi.common.RequestBuilder;
import com.github.cambierr.ovhapi.common.SafeResponse;
import com.github.cambierr.ovhapi.exception.TokenNotLinkedException;
import org.json.JSONObject;
import rx.Observable;

/**
 *
 * @author cambierr
 */
public class CredentialRequest {

    private final String applicationKey;
    private final String applicationSecret;
    private final String consumerKey;
    private final String validationUrl;
    private boolean linked;

    /**
     * Creates a credential request to get a CK
     *
     * @param _applicationKey the OVH API application key
     * @param _applicationSecret the OVH API application secret
     * @param _rules the list of rules to be granted by the CK
     * @param _redirection the redirection url for after-connection
     *
     * @return an Observable CredentialRequest
     */
    public static Observable<CredentialRequest> build(String _applicationKey, String _applicationSecret, AccessRules _rules, String _redirection) {
        return new RequestBuilder("/auth/credential", Method.POST, _applicationKey)
                .body(new JSONObject().put("redirection", _redirection).put("accessRules", _rules.toJson()).toString())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject token) -> {
                    return new CredentialRequest(_applicationKey, _applicationSecret, token.getString("consumerKey"), token.getString("validationUrl"));
                });
    }

    private CredentialRequest(String _applicationKey, String _applicationSecret, String _consumerKey, String _validationUrl) {
        applicationKey = _applicationKey;
        applicationSecret = _applicationSecret;
        consumerKey = _consumerKey;
        linked = false;
        validationUrl = _validationUrl;
    }

    /**
     * Returns the validation url associated with this request
     *
     * @return the validation url
     */
    public String getValidationUrl() {
        return validationUrl;
    }

    /**
     * Checks if this request (and its CK) has been linked to an user account
     *
     * <p>
     * <strong>Currently, this method isn't implemented yet and will always
     * return true.</strong></p>
     *
     * @return true if linked, or false
     */
    public boolean isLinked() {
        if (linked) {
            return true;
        }
        /**
         * @pending: waiting for ovh
         */
        linked = true;

        return linked;
    }

    /**
     * Returns the Credential object matching this request if CK has been
     * linked, or throws an exception
     *
     * @return the credential object matching this request
     *
     * @throws TokenNotLinkedException if this request (its CK) hasn't been
     * linked to an user
     */
    public Credential getCredential() throws TokenNotLinkedException {
        if (!isLinked()) {
            throw new TokenNotLinkedException();
        }
        return new Credential(applicationKey, applicationSecret, consumerKey);
    }
}
