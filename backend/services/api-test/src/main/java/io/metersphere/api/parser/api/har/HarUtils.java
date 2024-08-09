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

package io.metersphere.api.parser.api.har;


import com.google.gson.JsonSyntaxException;
import io.metersphere.api.parser.api.har.model.Har;
import io.metersphere.sdk.util.JSON;

import java.io.IOException;
import java.io.InputStream;

public class HarUtils {

    public static Har read(InputStream source) throws JsonSyntaxException, IOException {
        if (source == null) {
            throw new IllegalArgumentException("HAR Json cannot be null/empty");
        }
        return JSON.parseObject(source, Har.class);
    }
}
