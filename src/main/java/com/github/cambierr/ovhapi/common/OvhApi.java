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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author cambierr
 */
public class OvhApi {

    /**
     * The API version
     */
    public final static String API_VERSION = "1.0";
    /**
     * The API endpoint
     */
    public final static String API_ENDPOINT = "eu.api.ovh.com";
    /**
     * The API date format
     */
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Converts OVH API's dates to timestamps
     *
     * @param _date the date to be converted
     *
     * @return the matching timestamp
     *
     * @throws RuntimeException if date format is not the awaited one
     */
    public static long dateToTime(String _date) throws RuntimeException {
        try {
            return dateFormat.parse(_date).getTime();
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Converts timestamp to OVH API's date format
     *
     * @param _time the timestamp to convert
     *
     * @return the formated date
     */
    public static String timeToDate(long _time) {
        return dateFormat.format(new Date(_time));
    }
}
