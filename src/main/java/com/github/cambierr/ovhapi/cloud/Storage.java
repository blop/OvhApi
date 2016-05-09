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
public class Storage {

    private final Region region;
    private String name;
    private long storedBytes;
    private final String id;
    private long storedObjects;
    private String staticUrl;
    private boolean isPublic;

    private boolean partial = false;
    private final Project project;

    private Storage(Project _project, String _id, String _name, Region _region, long _storedBytes, long _storedObjects, String _staticUrl, boolean _isPublic) {
        region = _region;
        name = _name;
        storedBytes = _storedBytes;
        storedObjects = _storedObjects;
        id = _id;
        project = _project;
        staticUrl = _staticUrl;
        isPublic = _isPublic;
    }

    private Storage(Project _project, String _id, String _name, Region _region, long _storedBytes, long _storedObjects) {
        region = _region;
        name = _name;
        storedBytes = _storedBytes;
        storedObjects = _storedObjects;
        id = _id;
        project = _project;
        staticUrl = null;
        isPublic = false;
        partial = true;
    }

    /**
     * Lists all storage containers availables in a project
     *
     * @param _project The project to list storage containers of
     *
     * @return Zero to several observable Storage objects
     */
    public static Observable<Storage> list(Project _project) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/storage", Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray containers) -> Observable
                        .range(0, containers.length())
                        .map((Integer t2) -> {
                            JSONObject container = containers.getJSONObject(t2);
                            return new Storage(_project,
                                    container.getString("id"),
                                    container.getString("name"),
                                    Region.byName(_project, container.getString("region")),
                                    container.getLong("storedBytes"),
                                    container.getLong("storedObjects"));
                        })
                );
    }

    /**
     * Loads a storage container by its id
     *
     * @param _project the project to load the storage container from
     * @param _id the storage container id
     *
     * @return an observable Storage object
     */
    public static Observable<Storage> byId(Project _project, String _id) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/storage/" + _id, Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject container) -> new Storage(_project,
                        _id,
                        container.getString("name"),
                        Region.byName(_project, container.getString("region")),
                        container.getLong("storedBytes"),
                        container.getLong("storedObjects"),
                        container.getString("staticUrl"),
                        container.getBoolean("public"))
                );
    }

    /**
     * Returns the region of this storage container
     *
     * @return the region of this storage container
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Returns the name of this storage container
     *
     * @return the name of this storage container
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the size of this storage container
     *
     * @return the size of this storage container
     */
    public long getStoredBytes() {
        return storedBytes;
    }

    /**
     * Returns the id of this storage container
     *
     * @return the id of this storage container
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the object count of this storage container
     *
     * @return the object count of this storage container
     */
    public long getStoredObjects() {
        return storedObjects;
    }

    /**
     * Returns the static url of this storage container
     *
     * @return the static url of this storage container
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public String getStaticUrl() {
        if (partial) {
            throw new PartialObjectException();
        }
        return staticUrl;
    }

    /**
     * Returns the public status of this storage container
     *
     * @return the public status of this storage container
     *
     * @throws PartialObjectException if this object is partially loaded
     */
    public boolean isPublic() {
        if (partial) {
            throw new PartialObjectException();
        }
        return isPublic;
    }

    /**
     * Checks if this storage container is partially loaded or not
     *
     * @return true if partially loaded, or false
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * Updates a storage container object
     *
     * @return the Observable updated Storage object
     */
    public Observable<Storage> update() {
        return byId(project, id)
                .map((Storage t1) -> {
                    this.isPublic = t1.isPublic;
                    this.staticUrl = t1.staticUrl;
                    this.name = t1.name;
                    this.storedBytes = t1.storedBytes;
                    this.storedObjects = t1.storedObjects;
                    this.partial = false;
                    return this;
                });
    }

    /**
     * Completes a partial storage container object
     *
     * @return the Observable completed Storage object
     */
    public Observable<Storage> complete() {
        if (!partial) {
            return Observable.just(this);
        }
        return update();
    }

    /**
     * Deletes this storage container object
     *
     * @return the observable deleted Storage object
     */
    public Observable<Storage> delete() {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/storage/" + id, Method.DELETE, project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> this);
    }

    /**
     * Updates the CORS settings of this storage container
     *
     * @param _cors the CORS origin to set
     *
     * @return the observable updated Storage object
     */
    public Observable<Storage> cors(String _cors) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/storage/" + id + "/cors", Method.POST, project.getCredentials())
                .body(new JSONObject()
                        .put("origin", _cors)
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> this);
    }

    /**
     * Creates a Storage container
     *
     * @param _project The project to create the container in
     * @param _region The region ti ceate the container in
     * @param _name The name of the new container
     *
     * @return an observable Storage container matching the creation request
     */
    public static Observable<Storage> create(Project _project, Region _region, String _name) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/storage", Method.POST, _project.getCredentials())
                .body(new JSONObject()
                        .put("containerName", _name)
                        .put("region", _region.getName())
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject storage) -> new Storage(_project, storage.getString("id"), _name, _region, storage.getLong("storedBytes"), storage.getLong("storedObjects")));
    }
}
