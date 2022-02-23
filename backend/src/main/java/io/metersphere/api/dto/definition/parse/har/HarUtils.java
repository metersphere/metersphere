/**
 *
 * har - HAR file reader, writer and viewer
 * Copyright (c) 2014, Sandeep Gupta
 * 
 * http://sangupta.com/projects/har
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package io.metersphere.api.dto.definition.parse.har;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonSyntaxException;
import io.metersphere.api.dto.definition.parse.har.model.Har;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utility class for working with HAR files.
 * 
 * @author sangupta
 *
 */
public class HarUtils {
	
	public static Har read(File file) throws JsonSyntaxException, IOException {
		Har har = JSONObject.parseObject(FileUtils.readFileToString(file), Har.class);
		return har;
	}
	
	
	public static Har read(String harJson) throws JsonSyntaxException, IOException {
		if(StringUtils.isEmpty(harJson)) {
			throw new IllegalArgumentException("HAR Json cannot be null/empty");
		}
		Har har = JSONObject.parseObject(harJson, Har.class);
		return har;
	}
}
