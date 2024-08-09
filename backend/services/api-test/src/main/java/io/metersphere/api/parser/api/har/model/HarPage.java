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

import java.util.List;

public class HarPage {

    public String startedDateTime;

    public String id;

    public String title;

    public HarPageTiming pageTimings;

    public String comment;

    public transient List<HarEntry> entries;


    @Override
    public String toString() {
        return "HarPage [startedDateTime=" + startedDateTime + ", id=" + id + ", title=" + title + ", pageTimings="
                + pageTimings + ", comment=" + comment + "]";
    }

    @Override
    public int hashCode() {
        if (this.id == null) {
            return -1;
        }

        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HarPage)) {
            return false;
        }

        if (this.id == null) {
            return false;
        }

        HarPage harPage = (HarPage) obj;
        return this.id.equals(harPage.id);
    }

}
