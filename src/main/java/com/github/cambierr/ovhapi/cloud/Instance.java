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
import com.github.cambierr.ovhapi.common.OvhApi;
import com.github.cambierr.ovhapi.common.RequestBuilder;
import com.github.cambierr.ovhapi.common.SafeResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;

/**
 *
 * @author cambierr
 */
public class Instance {

    private Status status;
    private final Region region;
    private String name;
    private Image image;
    private final long creationDate;
    private Flavor flavor;
    private final SshKey sshKey;
    private final String id;

    private final Project project;

    private Instance(Project _project, Status _status, Region _region, String _name, Image _image, long _creationDate, Flavor _flavor, SshKey _sshKey, String _id) {
        project = _project;
        status = _status;
        region = _region;
        name = _name;
        image = _image;
        creationDate = _creationDate;
        flavor = _flavor;
        sshKey = _sshKey;
        id = _id;
    }

    /**
     * Lists all instances in a project and in a region (if provided)
     *
     * @param _project The project to list instances from
     * @param _region The region to list instances from (null = all regions)
     *
     * @return Zero to several observable Instance objects
     */
    public static Observable<Instance> list(Project _project, Region _region) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/instance?region=" + ((_region == null) ? "" : _region.getName()), Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray instances) -> Observable
                        .range(0, instances.length())
                        .map((Integer t2) -> {
                            JSONObject instance = instances.getJSONObject(t2);
                            return new Instance(_project,
                                    Status.valueOf(instance.getString("status")),
                                    Region.byName(_project, instance.getString("region")),
                                    instance.getString("name"),
                                    Image.byId(_project,
                                            instance.getString("imageId"),
                                            Region.byName(_project,
                                                    instance.getString("region")
                                            )),
                                    OvhApi.dateToTime(instance.getString("created")),
                                    Flavor.byId(_project,
                                            instance.getString("flavorId"),
                                            Region.byName(_project,
                                                    instance.getString("region")
                                            )),
                                    instance.get("sshKeyId") == JSONObject.NULL ? null : SshKey.byIdPartial(_project, instance.getString("sshKeyId")),
                                    instance.getString("id"));
                        }));
    }

    /**
     * Loads an Instance by its id
     *
     * @param _project the project to load the Instance from
     * @param _id the Instance id
     *
     * @return an observable instance object
     */
    public static Observable<Instance> byId(Project _project, String _id) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/instance/" + _id, Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject instance) -> {
                    JSONObject jsonImage = instance.getJSONObject("image");
                    JSONObject jsonFlavor = instance.getJSONObject("flavor");
                    JSONObject jsonSshKey = instance.get("sshKey") == JSONObject.NULL ? null : instance.getJSONObject("sshKey");
                    return new Instance(
                            _project,
                            Status.valueOf(instance.getString("status")),
                            Region.byName(_project, instance.getString("region")),
                            instance.getString("name"),
                            new Image(_project,
                                    jsonImage.getString("id"),
                                    jsonImage.getString("visibility"),
                                    OvhApi.dateToTime(jsonImage.getString("creationDate")),
                                    jsonImage.getString("status"),
                                    Region.byName(_project, jsonImage.getString("region")),
                                    jsonImage.getString("name"),
                                    jsonImage.getString("type"),
                                    jsonImage.getInt("minDisk")),
                            OvhApi.dateToTime(instance.getString("created")),
                            new Flavor(_project,
                                    jsonFlavor.getString("id"),
                                    jsonFlavor.getInt("disk"),
                                    Region.byName(_project, jsonFlavor.getString("region")),
                                    jsonFlavor.getString("name"),
                                    jsonFlavor.getInt("vcpus"),
                                    jsonFlavor.getString("type"),
                                    jsonFlavor.getString("osType"),
                                    jsonFlavor.getInt("ram")),
                            jsonSshKey == null ? null : new SshKey(_project,
                                            jsonSshKey.getString("id"),
                                            Region.byName(_project, jsonSshKey.getJSONArray("regions").getString(0)),
                                            jsonSshKey.getString("name"),
                                            jsonSshKey.getString("publicKey"),
                                            jsonSshKey.getString("fingerPrint")),
                            instance.getString("id"));
                });
    }

    /**
     * Returns the status of this instance
     *
     * @return the status of this instance
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the region of this instance
     *
     * @return the region of this instance
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Returns the name of this instance
     *
     * @return the name of this instance
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the image of this instance
     *
     * @return the image of this instance
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns the creation date of this instance
     *
     * @return the creation date (timestamp) of this instance
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the flavor of this instance
     *
     * @return the flavor of this instance
     */
    public Flavor getFlavor() {
        return flavor;
    }

    /**
     * Returns the SSH key of this instance
     *
     * @return the SSH key of this instance
     */
    public SshKey getSshKey() {
        return sshKey;
    }

    /**
     * Returns the id of this instance
     *
     * @return the id of this instance
     */
    public String getId() {
        return id;
    }

    public enum Status {

        ACTIVE,
        BUILDING,
        DELETED,
        ERROR,
        HARD_REBOOT,
        PASSWORD,
        PAUSED,
        REBOOT,
        REBUILD,
        RESCUED,
        RESIZED,
        REVERT_RESIZE,
        SOFT_DELETED,
        STOPPED,
        SUSPENDED,
        UNKNOWN,
        VERIFY_RESIZE,
        MIGRATING,
        RESIZE,
        BUILD,
        SHUTOFF,
        RESCUE,
        SHELVED,
        SHELVED_OFFLOADED
    }

    public enum RebootType {

        soft,
        hard
    }

    /**
     * Kills this Instance
     *
     * @return an observable Instance object matching the kill request
     */
    public Observable<Instance> kill() {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id, Method.DELETE, project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> this);
    }

    /**
     * Creates an instance
     *
     * @param _project The project to create the instance in
     * @param _flavor The flavor to be used
     * @param _image The image to be used
     * @param _region The region to create the instance in
     * @param _key The SSH key to be used (can be null)
     * @param _name The name of the new instance
     *
     * @return an observable Instance matching the creation request
     */
    public static Observable<Instance> create(Project _project, Flavor _flavor, Image _image, Region _region, SshKey _key, String _name) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/instance", Method.POST, _project.getCredentials())
                .body(new JSONObject()
                        .put("flavorId", _flavor.getId())
                        .put("imageId", _image.getId())
                        .put("name", _name)
                        .put("region", _region.getName())
                        .put("sshKeyId", (_key == null) ? "" : _key.getId())
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject instance) -> {
                    JSONObject jsonImage = instance.getJSONObject("image");
                    JSONObject jsonFlavor = instance.getJSONObject("flavor");
                    JSONObject jsonSshKey = instance.get("sshKey") == JSONObject.NULL ? null : instance.getJSONObject("sshKey");
                    return new Instance(
                            _project,
                            Status.valueOf(instance.getString("status")),
                            Region.byName(_project, instance.getString("region")),
                            instance.getString("name"),
                            new Image(_project,
                                    jsonImage.getString("id"),
                                    jsonImage.getString("visibility"),
                                    OvhApi.dateToTime(jsonImage.getString("creationDate")),
                                    jsonImage.getString("status"),
                                    Region.byName(_project, jsonImage.getString("region")),
                                    jsonImage.getString("name"),
                                    jsonImage.getString("type"),
                                    jsonImage.getInt("minDisk")),
                            OvhApi.dateToTime(instance.getString("created")),
                            new Flavor(_project,
                                    jsonFlavor.getString("id"),
                                    jsonFlavor.getInt("disk"),
                                    Region.byName(_project, jsonFlavor.getString("region")),
                                    jsonFlavor.getString("name"),
                                    jsonFlavor.getInt("vcpus"),
                                    jsonFlavor.getString("type"),
                                    jsonFlavor.getString("osType"),
                                    jsonFlavor.getInt("ram")),
                            jsonSshKey == null ? null : new SshKey(_project,
                                            jsonSshKey.getString("id"),
                                            Region.byName(_project, jsonSshKey.getJSONArray("regions").getString(0)),
                                            jsonSshKey.getString("name"),
                                            jsonSshKey.getString("publicKey"),
                                            jsonSshKey.getString("fingerPrint")),
                            instance.getString("id"));
                });
    }

    /**
     * Creates multiple instances
     *
     * @param _project The project to create the instances in
     * @param _flavor The flavor to be used
     * @param _image The image to be used
     * @param _region The region to create the instance in
     * @param _key The SSH key to be used (can be null)
     * @param _name The name of the new instances
     * @param _count The number of isntances to spawn
     *
     * @return an observable Instance matching the creation request
     */
    public static Observable<Instance> createBulk(Project _project, Flavor _flavor, Image _image, Region _region, SshKey _key, String _name, int _count) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/instance", Method.POST, _project.getCredentials())
                .body(new JSONObject()
                        .put("flavorId", _flavor.getId())
                        .put("imageId", _image.getId())
                        .put("name", _name)
                        .put("region", _region.getName())
                        .put("sshKeyId", (_key == null) ? "" : _key.getId())
                        .put("number", _count)
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray instances) -> Observable
                        .range(0, instances.length())
                        .map((Integer t) -> {
                            return new Instance(_project,
                                    Status.valueOf(instances.getJSONObject(t).getString("status")),
                                    Region.byName(_project, instances.getJSONObject(t).getString("region")),
                                    instances.getJSONObject(t).getString("name"),
                                    Image.byId(_project,
                                            instances.getJSONObject(t).getString("imageId"),
                                            Region.byName(_project, instances.getJSONObject(t).getString("region"))),
                                    OvhApi.dateToTime(instances.getJSONObject(t).getString("created")),
                                    Flavor.byId(_project,
                                            instances.getJSONObject(t).getString("flavorId"),
                                            Region.byName(_project, instances.getJSONObject(t).getString("region"))),
                                    SshKey.byIdPartial(_project, instances.getJSONObject(t).getString("sshKeyId")),
                                    instances.getJSONObject(t).getString("id"));
                        }));
    }

    /**
     * Resizes an instance to a new flavor
     *
     * @param _flavor The new Flavor to be used
     *
     * @return an observable Instance matching the resize request
     */
    public Observable<Instance> resize(Flavor _flavor) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id + "/resize", Method.POST, project.getCredentials())
                .body(new JSONObject()
                        .put("flavorId", _flavor.getId())
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject instance) -> {
                    this.flavor = _flavor;
                    this.status = Status.valueOf(instance.getString("status"));
                    return this;
                });
    }

    /**
     * Reinstalls an instance
     *
     * @param _image The new Image to be used
     *
     * @return and observable Instance matching the reinstall request
     */
    public Observable<Instance> reinstall(Image _image) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id + "/reinstall", Method.POST, project.getCredentials())
                .body(new JSONObject()
                        .put("imageId", _image.getId())
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONObject.class))
                .map((JSONObject instance) -> {
                    this.image = _image;
                    this.status = Status.valueOf(instance.getString("status"));
                    return this;
                });
    }

    /**
     * Renames an instance
     *
     * @param _name The new name to be used
     *
     * @return and observable Instance matching the rename request
     */
    public Observable<Instance> rename(String _name) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id, Method.PUT, project.getCredentials())
                .body(new JSONObject()
                        .put("instanceName", _name)
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> {
                    this.name = _name;
                    return this;
                });
    }

    /**
     * Updates an Instance object
     *
     * @return the Observable updated Instance object
     */
    public Observable<Instance> update() {
        return byId(project, id)
                .map((Instance t1) -> {
                    this.flavor = t1.flavor;
                    this.image = t1.image;
                    this.name = t1.name;
                    this.status = t1.status;
                    return this;
                });
    }

    /**
     * Reboots an instance
     *
     * @param _reboot The reboot type
     *
     * @return an observable Instance matching the reboot request
     */
    public Observable<Instance> reboot(RebootType _reboot) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id + "/reboot", Method.POST, project.getCredentials())
                .body(new JSONObject()
                        .put("type", _reboot.name())
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> {
                    this.status = Status.REBOOT;
                    return this;
                });
    }

    /**
     * Creates a Snapshot of this instance
     *
     * @param _name the Snapshot name
     *
     * @return an observable instance matching the snapshot request
     */
    public Observable<Instance> snapshot(String _name) {
        return new RequestBuilder("/cloud/project/" + project.getId() + "/instance/" + id + "/snapshot", Method.POST, project.getCredentials())
                .body(new JSONObject()
                        .put("snapshotName", _name)
                        .toString()
                )
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(null))
                .map((Object t1) -> this);
    }

}
