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

import com.github.cambierr.ovhapi.common.Method;
import com.github.cambierr.ovhapi.common.RequestBuilder;
import com.github.cambierr.ovhapi.common.SafeResponse;
import com.github.cambierr.ovhapi.exception.PartialObjectException;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;

/**
 *
 * @author cambierr
 */
public class SshKey {

    private final String id;
    private final Region region;
    private String name;
    private String pubKey;
    private String fingerPrint;

    private boolean partial = false;
    private final Project project;

    protected SshKey(Project _project, String _id, Region _region, String _name, String _pubKey, String _fingerPrint) {
        project = _project;
        id = _id;
        region = _region;
        name = _name;
        pubKey = _pubKey;
        fingerPrint = _fingerPrint;
    }

    /**
     * For internal use only
     *
     * @param _project the project to request from
     * @param _id the key id
     *
     * @return a SshKey object
     */
    protected static SshKey byIdPartial(Project _project, String _id) {
        SshKey temp = new SshKey(_project, _id, null, null, null, null);
        temp.partial = true;
        return temp;
    }

    /**
     * Lists all SSH keys within the provided project in the specified region
     * (or null if not provided)
     *
     * @param _project the project to list keys from
     * @param _region the region to list keys from
     *
     * @return Zero to several observable SshKey objects
     */
    public static Observable<SshKey> list(Project _project, Region _region) {
        String args = "";
        if (_region != null) {
            args += "region=" + _region.getName() + "&";
        }

        return new RequestBuilder("/cloud/project/" + _project.getId() + "/sshkey?" + args, Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray keys) -> Observable
                        .range(0, keys.length())
                        .map((Integer t2) -> {
                            JSONObject key = keys.getJSONObject(t2);
                            return new SshKey(_project, key.getString("id"), Region.byName(_project, key.getJSONArray("regions").getString(0)), key.getString("name"), key.getString("publicKey"), null);
                        })
                );
    }

    /**
     * Loads a SSH key by its id
     *
     * @param _project the project to load the key from
     * @param _id the key id
     *
     * @return an observable SshKey object
     */
    public static Observable<SshKey> byId(Project _project, String _id) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/sshkey/" + _id, Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject key) -> {
                    return new SshKey(_project,
                            key.getString("id"),
                            Region.byName(_project, key.getJSONArray("regions").getString(0)),
                            key.getString("name"),
                            key.getString("publicKey"),
                            key.getString("fingerPrint"));
                });
    }

    /**
     * Returns this key's id
     *
     * @return this key's id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns this key's region
     *
     * @return this key's region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Returns this key's name
     *
     * @return this key's name
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getName() {
        if (partial) {
            throw new PartialObjectException();
        }
        return name;
    }

    /**
     * Returns this key's public key
     *
     * @return this key's public key
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getPubKey() {
        if (partial) {
            throw new PartialObjectException();
        }
        return pubKey;
    }

    /**
     * Returns this key's fingerprint
     *
     * @return this key's fingerprint
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getFingerPrint() {
        if (fingerPrint == null) {
            throw new PartialObjectException();
        }
        return fingerPrint;
    }

    /**
     * Checks if this Flavor is partially loaded or not
     *
     * @return true if partially loaded, or false
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * Completes a partial SshKey object
     *
     * @return the Observable completed SshKey object
     */
    public Observable<SshKey> complete() {
        if (!partial) {
            return Observable.just(this);
        }

        return byId(project, id)
                .map((SshKey t1) -> {
                    this.fingerPrint = t1.fingerPrint;
                    this.name = t1.name;
                    this.pubKey = t1.pubKey;
                    this.partial = false;
                    return this;
                });
    }

    /**
     * Creates a SshKey
     *
     * @param _project The project to create the key in
     * @param _region The region ti ceate the key in
     * @param _name The name of the new key
     * @param _key The public key part
     *
     * @return an observable SshKeymatching the creation request
     */
    public static Observable<SshKey> create(Project _project, Region _region, String _name, String _key) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/sshkey", Method.POST, _project.getCredentials())
                .body(new JSONObject()
                        .put("name", _name)
                        .put("region", (_region == null) ? null : _region.getName())
                        .put("publicKey", _key)
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject key) -> new SshKey(_project,
                        key.getString("id"),
                        _region,
                        _name,
                        key.getString("publicKey"),
                        key.getString("fingerPrint"))
                );
    }

    /**
     * Deletes this SshKeyobject
     *
     * @return the observable deleted SshKey object
     */
    public Observable<SshKey> delete() {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/sshkey/" + id, Method.DELETE, project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> this);
    }

}
