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
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.parser.api.har.model.*;
import io.metersphere.api.utils.JSONUtil;
import io.metersphere.sdk.util.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HarUtils {

    public static Har read(InputStream source) throws JsonSyntaxException, IOException {
        if (source == null) {
            throw new IllegalArgumentException("HAR Json cannot be null/empty");
        }
        return JSON.parseObject(source, Har.class);
    }

    public static void parseParameters(HarRequest harRequest, MsHTTPElement request) {
        List<HarQueryParam> queryStringList = harRequest.queryString;
        queryStringList.forEach(harQueryParam -> {
            parseQueryParameters(harQueryParam, request.getQuery());
        });
        List<HarHeader> harHeaderList = harRequest.headers;
        harHeaderList.forEach(harHeader -> {
            parseHeaderParameters(harHeader, request.getHeaders());
        });
        List<HarCookie> harCookieList = harRequest.cookies;
        harCookieList.forEach(harCookie -> {
            parseCookieParameters(harCookie, request.getHeaders());
        });
    }

    private static void parseQueryParameters(HarQueryParam harQueryParam, List<QueryParam> arguments) {
        arguments.add(new QueryParam() {{
            this.setKey(harQueryParam.name);
            this.setValue(harQueryParam.value);
            this.setDescription(harQueryParam.comment);
        }});
    }

    public static void parseResponseBody(HarContent content, ResponseBody body) {
        if (content == null) {
            return;
        }
        String contentType = content.mimeType;
        if (body != null) {
            switch (contentType) {
                case "application/x-www-form-urlencoded":
                    body.setBodyType(Body.BodyType.WWW_FORM.name());
                    break;
                case "multipart/form-data":
                    body.setBodyType(Body.BodyType.FORM_DATA.name());
                    break;
                case "application/json":
                    body.setBodyType(Body.BodyType.JSON.name());
                    body.setJsonBody(new JsonBody() {{
                        this.setJsonValue(content.text);
                    }});
                    break;
                case "application/xml":
                    body.setBodyType(Body.BodyType.XML.name());
                    body.setXmlBody(new XmlBody() {{
                        this.setValue(content.text);
                    }});
                    break;
                case "application/octet-stream":
                    body.setBodyType(Body.BodyType.BINARY.name());
                default:
                    body.setBodyType(Body.BodyType.RAW.name());
                    body.setRawBody(new RawBody() {{
                        this.setValue(content.text);
                    }});
            }
        }
    }


    public static void parseResponseHeader(HarResponse response, List<MsHeader> msHeaders) {
        List<HarHeader> harHeaders = response.headers;
        if (harHeaders != null) {
            for (HarHeader header : harHeaders) {
                msHeaders.add(new MsHeader() {{
                    this.setKey(header.name);
                    this.setValue(header.value);
                    this.setDescription(header.comment);
                }});
            }
        }
    }

    private static void parseCookieParameters(HarCookie harCookie, List<MsHeader> headers) {
        boolean hasCookie = false;
        for (MsHeader header : headers) {
            if (StringUtils.equalsIgnoreCase("Cookie", header.getKey())) {
                hasCookie = true;
                String cookies = Optional.ofNullable(header.getValue()).orElse(StringUtils.EMPTY);
                header.setValue(cookies + harCookie.name + "=" + harCookie.value + ";");
            }
        }
        if (!hasCookie) {
            addHeader(headers, "Cookie", harCookie.name + "=" + harCookie.value + ";", harCookie.comment);
        }
    }

    public static void parseRequestBody(HarRequest requestBody, Body body) {
        if (requestBody == null) {
            return;
        }
        HarPostData content = requestBody.postData;
        if (StringUtils.equalsIgnoreCase("GET", requestBody.method) || requestBody.postData == null) {
            return;
        }
        String bodyType = content.mimeType;
        if (StringUtils.isEmpty(bodyType)) {
            bodyType = Body.BodyType.RAW.name();
            body.setRawBody(new RawBody() {{
                this.setValue(content.text);
            }});
        } else {
            if (bodyType.startsWith(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                bodyType = Body.BodyType.WWW_FORM.name();
                body.setBodyType(Body.BodyType.WWW_FORM.name());
                List<HarPostParam> postParams = content.params;
                WWWFormBody kv = new WWWFormBody();
                for (HarPostParam postParam : postParams) {
                    kv.getFormValues().add(new WWWFormKV() {{
                        this.setKey(postParam.name);
                        this.setValue(postParam.value);
                    }});
                }
                body.setWwwFormBody(kv);
            } else if (bodyType.startsWith(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)) {
                if (bodyType.contains("boundary=") && StringUtils.contains(content.text, getBoundaryFromContentType(bodyType))) {
                    String[] textArr = StringUtils.split(content.text, "\r\n");
                    String paramData = parseMultipartByTextArr(textArr);
                    JSONObject obj = null;
                    try {
                        obj = JSONUtil.parseObject(paramData);

                        FormDataBody kv = new FormDataBody();
                        for (String key : obj.keySet()) {
                            String value = obj.optString(key);
                            kv.getFormValues().add(new FormDataKV() {{
                                this.setKey(key);
                                this.setValue(value);
                            }});
                        }
                        body.setFormDataBody(kv);
                    } catch (Exception e) {
                        obj = null;
                    }
                    if (obj == null) {
                        body.setRawBody(new RawBody() {{
                            this.setValue(paramData);
                        }});
                    }
                } else {
                    List<HarPostParam> postParams = content.params;
                    if (CollectionUtils.isNotEmpty(postParams)) {
                        FormDataBody kv = new FormDataBody();
                        for (HarPostParam postParam : postParams) {
                            kv.getFormValues().add(new FormDataKV() {{
                                this.setKey(postParam.name);
                                this.setValue(postParam.value);
                            }});
                        }
                        body.setFormDataBody(kv);
                    }
                }
                bodyType = Body.BodyType.FORM_DATA.name();
            } else if (bodyType.startsWith(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
                bodyType = Body.BodyType.JSON.name();
                body.setJsonBody(new JsonBody() {{
                    this.setJsonValue(content.text);
                }});
            } else if (bodyType.startsWith(org.springframework.http.MediaType.APPLICATION_XML_VALUE)) {
                bodyType = Body.BodyType.XML.name();
                body.setXmlBody(new XmlBody() {{
                    this.setValue(content.text);
                }});
            } else if (bodyType.startsWith(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
                bodyType = Body.BodyType.BINARY.name();
                body.setBinaryBody(new BinaryBody());
            } else {
                bodyType = Body.BodyType.RAW.name();
                body.setRawBody(new RawBody() {{
                    this.setValue(content.text);
                }});
            }
        }
        body.setBodyType(bodyType);
    }

    private static String getBoundaryFromContentType(String contentType) {
        if (StringUtils.contains(contentType, "boundary=")) {
            String[] strArr = StringUtils.split(contentType, "boundary=");
            return strArr[strArr.length - 1];
        }
        return null;
    }

    private static String parseMultipartByTextArr(String[] textArr) {
        String data = null;
        if (textArr != null && textArr.length > 2) {
            data = textArr[textArr.length - 2];
        }
        return data;
    }


    protected static void addHeader(List<MsHeader> headers, String key, String value, String description) {
        boolean hasContentType = false;
        for (MsHeader header : headers) {
            if (StringUtils.equalsIgnoreCase(header.getKey(), key)) {
                hasContentType = true;
            }
        }
        if (!hasContentType) {
            headers.add(new MsHeader() {{
                this.setKey(key);
                this.setValue(value);
                this.setDescription(description);
            }});
        }
    }

    private static void parseHeaderParameters(HarHeader harHeader, List<MsHeader> headers) {
        String key = harHeader.name;
        String value = harHeader.value;
        String description = harHeader.comment;
        addHeader(headers, key, value, description);
    }

    public static HttpResponse parseResponse(HarResponse response) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new ResponseBody());
        msResponse.setName("har");
        msResponse.setHeaders(new ArrayList<>());
        if (response != null) {
            msResponse.setStatusCode(String.valueOf(response.status));
            HarUtils.parseResponseHeader(response, msResponse.getHeaders());
            HarUtils.parseResponseBody(response.content, msResponse.getBody());
        }
        return msResponse;
    }
}
