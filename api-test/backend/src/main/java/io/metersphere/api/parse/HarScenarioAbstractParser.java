package io.metersphere.api.parse;


import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.parse.api.har.model.*;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.XMLUtil;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HarScenarioAbstractParser<T> extends ApiImportAbstractParser<T> {

    protected MsHTTPSamplerProxy parseHar(HarEntry harEntry) {
        HarRequest harRequest = harEntry.request;
        if (harRequest == null) {
            return null;
        }
        MsHTTPSamplerProxy request = buildRequest(harRequest.url, harRequest.url, harRequest.method);
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = path.replace("{{", "${");
            path = path.replace("}}", "}");
            request.setPath(path);
        } else {
            request.setPath("/");
        }
        parseParameters(harRequest, request);
        parseRequestBody(harRequest, request.getBody());
        addBodyHeader(request);
        return request;
    }

    private void parseParameters(HarRequest harRequest, MsHTTPSamplerProxy request) {
        List<HarQueryParam> queryStringList = harRequest.queryString;
        queryStringList.forEach(harQueryParam -> {
            parseQueryParameters(harQueryParam, request.getArguments());
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

    private void parseRequestBody(HarRequest requestBody, Body body) {
        if (requestBody == null) {
            return;
        }
        HarPostData content = requestBody.postData;
        if (!StringUtils.equalsIgnoreCase("GET", requestBody.method) && requestBody.postData == null) {
            return;
        } else if (requestBody.postData == null) {
            return;
        }
        String contentType = content.mimeType;
        if (StringUtils.isEmpty(contentType)) {
            body.setRaw(content.text);
        } else {
            Map<String, Schema> infoMap = new HashMap();

            if (contentType.startsWith(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                contentType = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
                List<HarPostParam> postParams = content.params;
                for (HarPostParam postParam : postParams) {
                    KeyValue kv = new KeyValue(postParam.name, postParam.value);
                    body.getKvs().add(kv);
                }
            } else if (contentType.startsWith(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)) {
                if (contentType.contains("boundary=") && StringUtils.contains(content.text, this.getBoundaryFromContentType(contentType))) {
                    contentType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
                    String[] textArr = StringUtils.split(content.text, "\r\n");
                    String paramData = this.parseMultipartByTextArr(textArr);
                    JSONObject obj = null;
                    try {
                        obj = JSONUtil.parseObject(paramData);
                        if (obj != null) {
                            for (String key : obj.keySet()) {
                                KeyValue kv = new KeyValue(key, obj.optString(key));
                                body.getKvs().add(kv);
                            }
                        }
                    } catch (Exception e) {
                        obj = null;
                    }
                    if (obj == null) {
                        body.setRaw(paramData);
                    }
                } else {
                    contentType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
                    List<HarPostParam> postParams = content.params;
                    if (CollectionUtils.isNotEmpty(postParams)) {
                        for (HarPostParam postParam : postParams) {
                            KeyValue kv = new KeyValue(postParam.name, postParam.value);
                            body.getKvs().add(kv);
                        }
                    }
                }
            } else if (contentType.startsWith(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
                contentType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
                body.setRaw(content.text);
            } else if (contentType.startsWith(org.springframework.http.MediaType.APPLICATION_XML_VALUE)) {
                contentType = org.springframework.http.MediaType.APPLICATION_XML_VALUE;
                body.setRaw(parseXmlBody(content.text));
            } else if (contentType.startsWith(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
                contentType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
                List<HarPostParam> postParams = content.params;
                for (HarPostParam postParam : postParams) {
                    KeyValue kv = new KeyValue(postParam.name, postParam.value);
                    body.getKvs().add(kv);
                }
            } else {
                body.setRaw(content.text);
            }
        }
        body.setType(getBodyType(contentType));
    }


    private void parseQueryParameters(HarQueryParam harQueryParam, List<KeyValue> arguments) {
        arguments.add(new KeyValue(harQueryParam.name, harQueryParam.value, harQueryParam.comment, false));
    }

    private void parseCookieParameters(HarCookie harCookie, List<KeyValue> headers) {
        addCookie(headers, harCookie.name, harCookie.value, harCookie.comment, false);
    }

    private void parseHeaderParameters(HarHeader harHeader, List<KeyValue> headers) {
        addHeader(headers, harHeader.name, harHeader.value, harHeader.comment, StringUtils.EMPTY, false);
    }

    private String parseXmlBody(String xmlString) {
        JSONObject object = JSONUtil.parseObject(getDefaultStringValue(xmlString));
        return XMLUtil.jsonToXmlStr(object);
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? StringUtils.EMPTY : val;
    }

    private String getBoundaryFromContentType(String contentType) {
        if (StringUtils.contains(contentType, "boundary=")) {
            String[] strArr = StringUtils.split(contentType, "boundary=");
            return strArr[strArr.length - 1];
        }
        return null;
    }

    private String parseMultipartByTextArr(String[] textArr) {
        String data = null;
        if (textArr != null && textArr.length > 2) {
            data = textArr[textArr.length - 2];
        }
        return data;
    }

}
