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

package io.metersphere.api.dto.definition.parse.har.model;

import java.util.List;

public class HarRequest {
	
	public String method;
	
	public String url;
	
	public String httpVersion;
	
	public List<HarCookie> cookies;
	
	public List<HarHeader> headers;

	public List<HarQueryParm> queryString;
	
	public HarPostData postData;
	
	public long headersSize;
	
	public long bodySize;
	
	public String comment;
	
	@Override
	public String toString() {
		return this.method + " " + this.url + " " + this.httpVersion;
	}
	
}
