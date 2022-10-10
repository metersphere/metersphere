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

package io.metersphere.api.parse.api.har;


import com.google.gson.JsonSyntaxException;
import io.metersphere.api.parse.api.har.model.Har;
import io.metersphere.commons.utils.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for working with HAR files.
 *
 * @author sangupta
 */
public class HarUtils {

    public static Har read(File file) throws JsonSyntaxException, IOException {
        Har har = JSON.parseObject(FileUtils.readFileToString(file), Har.class);
        return har;
    }


    public static Har read(String harJson) throws JsonSyntaxException, IOException {
        if (StringUtils.isEmpty(harJson)) {
            throw new IllegalArgumentException("HAR Json cannot be null/empty");
        }
        Har har = JSON.parseObject(harJson, Har.class);
        return har;
    }
}
