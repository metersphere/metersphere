package io.metersphere.api.parse.api;


import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.parse.api.har.HarUtils;
import io.metersphere.api.parse.api.har.model.*;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtil;
import io.swagger.models.Model;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/3/10 11:14 上午
 * @Description
 */
public class HarParser extends HarAbstractParser {

    private Map<String, Model> definitions = null;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        Har har = null;
        try {
            har = HarUtils.read(source);
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        }
        if (ObjectUtils.isEmpty(har) || har.log == null) {
            MSException.throwException("解析失败，请确认选择的是 Har 格式！");
        }
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(har, request));
        return definitionImport;
    }


    private List<ApiDefinitionWithBLOBs> parseRequests(Har har, ApiTestImportRequest importRequest) {
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

       /* ApiModule selectModule = null;
        String selectModulePath = null;*/
        /*if (StringUtils.isNotBlank(importRequest.getModuleId())) {
            selectModule = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
            if (selectModule != null) {
                selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
            }
        }*/


        List<HarEntry> harEntryList = new ArrayList<>();
        if (har.log != null && har.log.entries != null) {
            harEntryList = har.log.entries;
        }


        for (HarEntry entry : harEntryList) {
            HarRequest harRequest = entry.request;
            String url = harRequest.url;
            if (url == null) {
                continue;
            }

            try {
                url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
                if (url.contains("?")) {
                    url = url.split("\\?")[0];
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }

            //默认取路径的最后一块
            String reqName = StringUtils.EMPTY;
            if (harRequest.url != null) {
                String[] nameArr = url.split("/");
                reqName = nameArr[nameArr.length - 1];
            }

            if (harRequest != null) {
                MsHTTPSamplerProxy request = super.buildRequest(reqName, url, harRequest.method, null);
                request.setFollowRedirects(true);
                request.setResponseTimeout("60000");
                request.setConnectTimeout("60000");
                ApiDefinitionWithBLOBs apiDefinition = super.buildApiDefinition(request.getId(), reqName, url, harRequest.method, importRequest);
                parseParameters(harRequest, request);
                parseRequestBody(harRequest, request.getBody());
                addBodyHeader(request);
                apiDefinition.setRequest(JSON.toJSONString(request));
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(entry.response)));
                results.add(apiDefinition);
            }
        }

        return results;
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


    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? StringUtils.EMPTY : val;
    }

    private void parseCookieParameters(HarCookie harCookie, List<KeyValue> headers) {
        addCookie(headers, harCookie.name, harCookie.value, harCookie.comment, false);
    }

    private void parseHeaderParameters(HarHeader harHeader, List<KeyValue> headers) {
        addHeader(headers, harHeader.name, harHeader.value, harHeader.comment, StringUtils.EMPTY, false);
    }

    private HttpResponse parseResponse(HarResponse response) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestTypeConstants.HTTP);
        msResponse.setStatusCode(new ArrayList<>());
        if (response != null) {
            String responseCode = String.valueOf(response.status);
            msResponse.getStatusCode().add(new KeyValue(responseCode, responseCode));
            parseResponseHeader(response, msResponse.getHeaders());
            parseResponseBody(response, msResponse.getBody());
        }
        return msResponse;
    }

    private void parseResponseHeader(HarResponse response, List<KeyValue> msHeaders) {
        List<HarHeader> harHeaders = response.headers;
        if (harHeaders != null) {
            for (HarHeader header : harHeaders) {
                msHeaders.add(new KeyValue(header.name, header.value, header.comment));
            }
        }
    }

    private void parseResponseBody(HarResponse response, Body body) {
        parseResponseBody(response.content, body);
    }

    private void parseRequestBody(HarRequest requestBody, Body body) {
        if (requestBody == null) {
            return;
        }
        HarPostData content = requestBody.postData;
        if (StringUtils.equalsIgnoreCase("GET", requestBody.method) || requestBody.postData == null) {
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


    private void parseResponseBody(HarContent content, Body body) {
        if (content == null) {
            return;
        }
        String contentType = content.mimeType;
        if (body != null) {
            body.setType(getBodyType(contentType));
            body.setRaw(content.text);
        }
    }

    private String parseXmlBody(String xmlString) {
        JSONObject object = JSONUtil.parseObject(getDefaultStringValue(xmlString));
        return XMLUtil.jsonToXmlStr(object);
    }

    private void parseQueryParameters(HarQueryParam harQueryParam, List<KeyValue> arguments) {
        arguments.add(new KeyValue(harQueryParam.name, harQueryParam.value, harQueryParam.comment, false));
    }
}
