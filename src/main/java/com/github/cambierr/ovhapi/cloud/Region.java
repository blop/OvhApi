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
import org.json.JSONArray;
import rx.Observable;

/**
 *
 * @author cambierr
 */
public class Region {

    private final String name;

    private final Project project;

    private Region(Project _project, String _name) {
        this.name = _name;
        project = _project;
    }

    /**
     * For internal use only
     *
     * @param _project the project to request from
     * @param _name the region name
     *
     * @return a Region object
     */
    public static Region byName(Project _project, String _name) {
        return new Region(_project, _name);
    }

    /**
     * Lists all regions available in the provided project
     *
     * @param _project the project to list regions of
     *
     * @return Zero to several observable Region objects
     */
    public static Observable<Region> list(Project _project) {
        return new RequestBuilder("/cloud/project/" + _project.getId() + "/region", Method.GET, _project.getCredentials())
                .build()
                .flatMap((SafeResponse arg0) -> arg0.validateResponse(JSONArray.class))
                .flatMap((JSONArray regions) -> Observable
                        .range(0, regions.length())
                        .map((Integer t2) -> new Region(_project, regions.getString(t2)))
                );

    }

    /**
     * Returns this region name
     *
     * @return this region name
     */
    public String getName() {
        return name;
    }

}
