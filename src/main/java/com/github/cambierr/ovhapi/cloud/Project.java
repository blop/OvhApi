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
package com.github.cambierr.ovhapi.cloud;

import com.github.cambierr.ovhapi.auth.Credential;
import com.github.cambierr.ovhapi.common.Method;
import com.github.cambierr.ovhapi.common.OvhApi;
import com.github.cambierr.ovhapi.common.RequestBuilder;
import com.github.cambierr.ovhapi.common.SafeResponse;
import com.github.cambierr.ovhapi.exception.PartialObjectException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;

/**
 *
 * @author cambierr
 */
public class Project {

    private long creationDate;
    private String status;
    private String description;
    private final String id;

    private boolean partial = false;
    private final Credential credentials;

    private Project(Credential _credentials, String _id, String _status, long _creationDate, boolean _unleash, String _description) {
        this.creationDate = _creationDate;
        this.status = _status;
        this.description = _description;
        this.id = _id;
        this.credentials = _credentials;
    }

    private Project(Credential _credentials, String _id) {
        credentials = _credentials;
        id = _id;
        partial = true;
    }

    /**
     * Checks if this Project is partially loaded or not
     *
     * @return true if partially loaded, or false
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * Updates a project object
     *
     * @return the Observable updated Project object
     */
    public Observable<Project> update() {
        return byId(credentials, id)
                .map((Project t1) -> {
                    this.creationDate = t1.creationDate;
                    this.description = t1.description;
                    this.status = t1.status;
                    this.partial = false;
                    return this;
                });
    }

    /**
     * Completes a partial Project object
     *
     * @return the Observable completed Project object
     */
    public Observable<Project> complete() {
        if (!partial) {
            return Observable.just(this);
        }
        return update();
    }

    /**
     * Returns the credentials of this project
     *
     * @return the credentials of this project
     */
    public Credential getCredentials() {
        return credentials;
    }

    /**
     * Returns the creation date of this project
     *
     * @return the creation date (timestamp) of this project
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public long getCreationDate() {
        if (partial) {
            throw new PartialObjectException();
        }
        return creationDate;
    }

    /**
     * Returns the status of this project
     *
     * @return the status of this project
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getStatus() {
        if (partial) {
            throw new PartialObjectException();
        }
        return status;
    }

    /**
     * Returns the description of this project
     *
     * @return the description of this project
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getDescription() {
        if (partial) {
            throw new PartialObjectException();
        }
        return description;
    }

    /**
     * Returns the id of this project
     *
     * @return the id of this project
     */
    public String getId() {
        return id;
    }

    /**
     * Loads a Project by its id
     *
     * @param _credentials The credentials to use to load the project
     * @param _id the Project id
     *
     * @return an observable project object
     */
    public static Observable<Project> byId(Credential _credentials, String _id) {
        return new RequestBuilder("/cloud/project/" + _id, Method.GET, _credentials)
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject project) -> new Project(_credentials,
                        project.getString("project_id"),
                        project.getString("status"),
                        OvhApi.dateToTime(project.getString("creationDate")),
                        project.getBoolean("unleash"),
                        project.getString("description")
                ));
    }

    /**
     * Lists all projects availables
     *
     * @param _credentials The credentials to use to load the project
     *
     * @return Zero to several observable Project objects
     */
    public static Observable<Project> list(Credential _credentials) {
        return new RequestBuilder("/cloud/project", Method.GET, _credentials)
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray resp) -> Observable
                        .range(0, resp.length())
                        .map((Integer t) -> new Project(_credentials, resp.getString(t)))
                );
    }

    /**
     * Updates this project's decription
     *
     * @param _description the new project description
     *
     * @return an obsevable Project matching the update request
     */
    public Observable<Project> setDescription(String _description) {
        return new RequestBuilder("/cloud/project/" + this.id, Method.PUT, credentials)
                .body(new JSONObject().put("description", _description).toString())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> {
                    this.description = _description;
                    return this;
                });
    }

    /**
     * Returns the consumption of this project
     *
     * @param _from The start of the consumption window to fetch
     * @param _to The end of the consumption window to fetch
     *
     * @return an observable Consumption object
     */
    public Observable<Consumption> getConsumption(long _from, long _to) {
        String args;
        try {
            args = "?from=" + URLEncoder.encode(OvhApi.timeToDate(_from), "UTF-8") + "&to=" + URLEncoder.encode(OvhApi.timeToDate(_to), "UTF-8");
        } catch (Exception ex) {
            return Observable.error(ex);
        }
        return new RequestBuilder("/cloud/project/" + this.id + "/consumption" + args, Method.GET, credentials)
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject output) -> new Consumption(output));
    }

    public class Consumption {

        private final List<Service> services;
        private final Cost total;

        private Consumption(JSONObject _json) {
            services = new ArrayList<>();

            for (String key : _json.getJSONObject("current").keySet()) {
                if (!key.equals("total")) {
                    services.add(new Service(key, _json.getJSONObject("current").getJSONObject(key)));
                }
            }
            total = new Cost(_json.getJSONObject("current").getJSONObject("total").getString("currencyCode"),
                    _json.getJSONObject("current").getJSONObject("total").getString("text"),
                    _json.getJSONObject("current").getJSONObject("total").getDouble("value"));
        }

        public List<Service> getServices() {
            return services;
        }

        public Cost getTotal() {
            return total;
        }

        public class Service {

            private final String name;
            private final Cost total;

            private Service(String _key, JSONObject _json) {
                name = _key;
                total = new Cost(_json.getJSONObject("total").getString("currencyCode"),
                        _json.getJSONObject("total").getString("text"),
                        _json.getJSONObject("total").getDouble("value"));
            }

            public Cost getTotal() {
                return total;
            }

            public String getName() {
                return name;
            }

            public class Item {

                public final JSONObject details;

                private Item(JSONObject _json) {
                    details = _json;
                }

            }

        }

        public class Cost {

            public final String currencyCode;
            public final String text;
            public final double value;

            private Cost(String _currencyCode, String _text, double _value) {
                currencyCode = _currencyCode;
                text = _text;
                value = _value;
            }

        }
    }

}
