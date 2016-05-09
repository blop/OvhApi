/*
 * The MIT License
 *
 * Copyright 2016 cambierr.
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

import com.github.cambierr.ovhapi.exception.RequestException;
import com.mashape.unirest.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;

/**
 *
 * @author cambierr
 */
public class SafeResponse {

    private final String data;
    private final String statusText;
    private final int status;

    protected SafeResponse(HttpResponse<String> _from) {
        data = _from.getBody();
        statusText = _from.getStatusText();
        status = _from.getStatus();
    }

    public String getStatusText() {
        return statusText;
    }

    public String getBody() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public <T extends Object> Observable<T> validateResponse(Class<T> _model) {
        return Observable.create((Subscriber<? super T> arg0) -> {
            try {
                if (getStatus() < 200 || getStatus() >= 300) {
                    throw new RequestException(getStatus(), getStatusText(), (data == null) ? null : data);
                }
                if (_model == null) {
                    arg0.onNext(null);
                } else if (_model.equals(String.class)) {
                    arg0.onNext((T) getBody());
                } else if (_model.equals(JSONObject.class)) {
                    arg0.onNext((T) new JSONObject(getBody()));
                } else if (_model.equals(JSONArray.class)) {
                    arg0.onNext((T) new JSONArray(getBody()));
                } else {
                    throw new IllegalArgumentException("model class unknown");
                }
                arg0.onCompleted();
            } catch (RequestException | IllegalArgumentException | JSONException ex) {
                if (ex instanceof JSONException) {
                    arg0.onError(new RequestException(getStatus(), getStatusText(), (data == null) ? null : data));
                } else {
                    arg0.onError(ex);
                }
            }
        });
    }

}
