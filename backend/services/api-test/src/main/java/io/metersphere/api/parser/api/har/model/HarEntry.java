/**
 * har - HAR file reader, writer and viewer
 * Copyright (c) 2014, Sandeep Gupta
 * <p>
 * http://sangupta.com/projects/har
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.metersphere.api.parser.api.har.model;

public class HarEntry implements Comparable<HarEntry> {

    public String pageref;

    public String startedDateTime;

    public double time;

    public HarRequest request;

    public HarResponse response;

    public HarCache cache;

    public HarTiming timings;

    public String serverIPAddress;

    public String connection;

    public String comment;


    @Override
    public String toString() {
        return "HarEntry [pageref=" + pageref + ", startedDateTime=" + startedDateTime + ", time=" + time + ", request="
                + request + ", response=" + response + ", cache=" + cache + ", timings=" + timings
                + ", serverIPAddress=" + serverIPAddress + ", connection=" + connection + ", comment=" + comment + "]";
    }


    @Override
    public int compareTo(HarEntry o) {
        if (o == null) {
            return -1;
        }
        // parse the time and then return
        return this.startedDateTime.compareTo(o.startedDateTime);
    }

}
