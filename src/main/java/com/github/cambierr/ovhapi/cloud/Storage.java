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

import static com.github.cambierr.ovhapi.cloud.Image.byId;
import static com.github.cambierr.ovhapi.cloud.Instance.byId;
import com.github.cambierr.ovhapi.common.Method;
import com.github.cambierr.ovhapi.common.RequestBuilder;
import com.github.cambierr.ovhapi.common.Response;
import com.github.cambierr.ovhapi.exception.PartialObjectException;
import com.github.cambierr.ovhapi.exception.RequestException;
import java.io.IOException;
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

    public static Observable<Storage> list(Project _project) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/storage", Method.GET, _project.getCredentials())
                .build()
                .flatMap((Response t1) -> {
                    try {
                        if (t1.responseCode() < 200 || t1.responseCode() >= 300) {
                            return Observable.error(new RequestException(t1.responseCode(), t1.responseMessage(), t1.entity()));
                        }
                        final JSONArray containers = t1.jsonArray();
                        return Observable
                        .range(0, containers.length())
                        .map((Integer t2) -> {
                            JSONObject container = containers.getJSONObject(t2);
                            Storage temp = new Storage(_project, container.getString("id"), container.getString("name"), Region.byName(_project, container.getString("region")), container.getLong("storedBytes"), container.getLong("storedObjects"), null, false);
                            temp.partial = true;
                            return temp;
                        });

                    } catch (IOException ex) {
                        return Observable.error(ex);
                    }
                });
    }
    
    public static Observable<Storage> byId(Project _project, String _id) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/storage/"+_id, Method.GET, _project.getCredentials())
                .build()
                .flatMap((Response t1) -> {
                    try {
                        if (t1.responseCode() < 200 || t1.responseCode() >= 300) {
                            return Observable.error(new RequestException(t1.responseCode(), t1.responseMessage(), t1.entity()));
                        }
                        final JSONObject container = t1.jsonObject();
                        return Observable.just(new Storage(_project, container.getString("id"), container.getString("name"), Region.byName(_project, container.getString("region")), container.getLong("storedBytes"), container.getLong("storedObjects"), container.getString("staticUrl"), container.getBoolean("public")));

                    } catch (IOException ex) {
                        return Observable.error(ex);
                    }
                });
    }

    public Region getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public long getStoredBytes() {
        return storedBytes;
    }

    public String getId() {
        return id;
    }

    public long getStoredObjects() {
        return storedObjects;
    }

    public String getStaticUrl() {
        if(partial){
            throw new PartialObjectException();
        }
        return staticUrl;
    }

    public boolean isPublic() {
        if(partial){
            throw new PartialObjectException();
        }
        return isPublic;
    }

    public boolean isPartial() {
        return partial;
    }
    
    public Observable<Storage> update(){
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
    
    public Observable<Storage> complete() {
        if (!partial) {
            return Observable.just(this);
        }
        return update();
    }
}