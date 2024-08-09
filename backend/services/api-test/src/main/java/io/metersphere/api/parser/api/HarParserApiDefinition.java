package io.metersphere.api.parser.api;


import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiImportFileParseResult;
import io.metersphere.api.dto.converter.ExistenceApiDefinitionDetail;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.parser.api.har.HarUtils;
import io.metersphere.api.parser.api.har.model.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.JSONUtil;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HarParserApiDefinition extends HttpApiDefinitionImportAbstractParser<ApiImportFileParseResult> {

    @Override
    public ApiImportFileParseResult parse(InputStream source, ImportRequest request) throws Exception {
        Har har = null;
        try {
            har = HarUtils.read(source);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (ObjectUtils.isEmpty(har) || har.log == null) {
            throw new MSException("解析失败，请确认选择的是 Har 格式！");
        }
        ApiImportFileParseResult definitionImport = new ApiImportFileParseResult();
        definitionImport.setData(parseRequests(har, request));
        return definitionImport;
    }

    @Override
    public ApiImportDataAnalysisResult generateInsertAndUpdateData(ApiImportFileParseResult importParser, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        ApiImportDataAnalysisResult insertAndUpdateData = super.generateInsertAndUpdateData(importParser, existenceApiDefinitionList);
        ApiDefinitionBlobMapper apiDefinitionBlobMapper = CommonBeanFactory.getBean(ApiDefinitionBlobMapper.class);

        for (ExistenceApiDefinitionDetail definitionDetail : insertAndUpdateData.getExistenceApiList()) {
            ApiDefinitionDetail importApi = definitionDetail.getImportApiDefinition();
            ApiDefinitionDetail savedApi = definitionDetail.getExistenceApiDefinition();

            ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(savedApi.getId());
            if (blob != null) {
                if (blob.getRequest() != null) {
                    AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
                    savedApi.setRequest(msTestElement);
                }
                if (blob.getResponse() != null) {
                    List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
                    savedApi.setResponse(httpResponses);
                }
            }
            this.mergeExistenceApiMap(importApi, savedApi);
        }
        return insertAndUpdateData;
    }

    private void mergeExistenceApiMap(ApiDefinitionDetail importApi, ApiDefinitionDetail savedApi) {
        MsHTTPElement importHttpElement = (MsHTTPElement) importApi.getRequest();
        if (savedApi.getRequest() != null) {
            MsHTTPElement existenceHttpElement = (MsHTTPElement) savedApi.getRequest();
            importHttpElement.setOtherConfig(existenceHttpElement.getOtherConfig());
            importHttpElement.setModuleId(existenceHttpElement.getModuleId());
            importHttpElement.setNum(existenceHttpElement.getNum());
            importHttpElement.setMockNum(existenceHttpElement.getMockNum());
            importHttpElement.setBody(this.mergeBody(importHttpElement.getBody(), existenceHttpElement.getBody()));
            importHttpElement.setHeaders(this.mergeHeaders(importHttpElement.getHeaders(), existenceHttpElement.getHeaders()));
            importHttpElement.setQuery(this.mergeQuery(importHttpElement.getQuery(), existenceHttpElement.getQuery()));
            importHttpElement.setRest(this.mergeRest(importHttpElement.getRest(), existenceHttpElement.getRest()));
        }
        importApi.setRequest(importHttpElement);

        if (CollectionUtils.isEmpty(importApi.getResponse())) {
            importApi.setResponse(savedApi.getResponse());
        } else {

            if (CollectionUtils.isEmpty(savedApi.getResponse())) {
                importApi.getResponse().getFirst().setDefaultFlag(true);
            } else {
                List<HttpResponse> existenceResponseList = savedApi.getResponse();
                for (HttpResponse importRsp : savedApi.getResponse()) {
                    boolean isExistence = false;
                    for (HttpResponse existenceRsp : existenceResponseList) {
                        if (StringUtils.equals(importRsp.getName(), existenceRsp.getName())) {
                            isExistence = true;
                            existenceRsp.setBody(importRsp.getBody());
                            existenceRsp.setHeaders(importRsp.getHeaders());
                            existenceRsp.setStatusCode(importRsp.getStatusCode());
                        }
                    }
                    if (!isExistence) {
                        importRsp.setId(IDGenerator.nextStr());
                        existenceResponseList.add(importRsp);
                    }
                }
            }
        }

    }

    private List<RestParam> mergeRest(List<RestParam> importRestList, List<RestParam> existenceRestList) {
        if (CollectionUtils.isNotEmpty(importRestList) && CollectionUtils.isNotEmpty(existenceRestList)) {
            for (RestParam importRest : importRestList) {
                for (RestParam existenceRest : existenceRestList) {
                    if (StringUtils.equals(importRest.getKey(), existenceRest.getKey())) {
                        importRest.setDescription(existenceRest.getDescription());
                        importRest.setMaxLength(existenceRest.getMaxLength());
                        importRest.setMinLength(existenceRest.getMinLength());
                        importRest.setEncode(existenceRest.getEncode());
                    }
                }
            }
        }
        return importRestList;
    }

    private List<QueryParam> mergeQuery(List<QueryParam> importQueryList, List<QueryParam> existenceQueryList) {
        if (CollectionUtils.isNotEmpty(importQueryList) && CollectionUtils.isNotEmpty(existenceQueryList)) {
            for (QueryParam importQuery : importQueryList) {
                for (QueryParam existenceQuery : existenceQueryList) {
                    if (StringUtils.equals(importQuery.getKey(), existenceQuery.getKey())) {
                        importQuery.setDescription(existenceQuery.getDescription());
                        importQuery.setMaxLength(existenceQuery.getMaxLength());
                        importQuery.setMinLength(existenceQuery.getMinLength());
                        importQuery.setEncode(existenceQuery.getEncode());
                    }
                }
            }
        }
        return importQueryList;
    }

    private List<MsHeader> mergeHeaders(List<MsHeader> importHeaders, List<MsHeader> existenceHeaders) {
        if (CollectionUtils.isNotEmpty(importHeaders) && CollectionUtils.isNotEmpty(existenceHeaders)) {
            for (MsHeader importHeader : importHeaders) {
                for (MsHeader existenceHeader : existenceHeaders) {
                    if (StringUtils.equals(importHeader.getKey(), existenceHeader.getKey())) {
                        importHeader.setDescription(existenceHeader.getDescription());
                    }
                }
            }
        }
        return importHeaders;
    }

    private Body mergeBody(Body importBody, Body existenceBody) {
        if (importBody == null) {
            return existenceBody;
        } else if (existenceBody == null) {
            return importBody;
        } else {
            Body returnBody = new Body();
            BeanUtils.copyBean(returnBody, existenceBody);

            if (importBody.getBinaryBody() != null) {
                returnBody.setBinaryBody(importBody.getBinaryBody());
            }
            if (importBody.getFormDataBody() != null) {
                if (returnBody.getFormDataBody() != null) {
                    for (FormDataKV importKv : importBody.getFormDataBody().getFormValues()) {
                        for (FormDataKV existenceKv : returnBody.getFormDataBody().getFormValues()) {
                            if (StringUtils.equals(existenceKv.getKey(), importKv.getKey())) {
                                importKv.setDescription(existenceKv.getDescription());
                                importKv.setMaxLength(existenceKv.getMaxLength());
                                importKv.setMinLength(existenceKv.getMinLength());
                            }
                        }
                    }
                }
                returnBody.setFormDataBody(importBody.getFormDataBody());
            }
            if (importBody.getJsonBody() != null) {
                if (returnBody.getJsonBody() != null) {
                    returnBody.getJsonBody().setJsonValue(importBody.getJsonBody().getJsonValue());
                } else {
                    returnBody.setJsonBody(importBody.getJsonBody());
                }
            }
            if (importBody.getNoneBody() != null) {
                returnBody.setNoneBody(importBody.getNoneBody());
            }
            if (importBody.getRawBody() != null) {
                returnBody.setRawBody(importBody.getRawBody());
            }
            if (importBody.getWwwFormBody() != null) {
                if (returnBody.getWwwFormBody() != null) {
                    for (WWWFormKV importKv : importBody.getWwwFormBody().getFormValues()) {
                        for (WWWFormKV existenceKv : returnBody.getWwwFormBody().getFormValues()) {
                            if (StringUtils.equals(existenceKv.getKey(), importKv.getKey())) {
                                importKv.setDescription(existenceKv.getDescription());
                                importKv.setMaxLength(existenceKv.getMaxLength());
                                importKv.setMinLength(existenceKv.getMinLength());
                            }
                        }
                    }
                }
                returnBody.setWwwFormBody(importBody.getWwwFormBody());
            }
            if (importBody.getXmlBody() != null) {
                returnBody.setXmlBody(importBody.getXmlBody());
            }
            return returnBody;
        }
    }

    private List<ApiDefinitionDetail> parseRequests(Har har, ImportRequest importRequest) {
        List<ApiDefinitionDetail> resultList = new ArrayList<>();

        List<HarEntry> harEntryList = new ArrayList<>();
        if (har.log != null && har.log.entries != null) {
            harEntryList = har.log.entries;
        }

        for (HarEntry entry : harEntryList) {
            HarRequest harRequest = entry.request;
            if (harRequest != null) {
                String url = harRequest.url;
                if (url == null) {
                    continue;
                }
                //默认取路径的最后一块
                String[] nameArr = url.split("/");
                String reqName = nameArr[nameArr.length - 1];
                try {
                    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
                    if (url.contains("?")) {
                        url = url.split("\\?")[0];
                    }
                } catch (Exception e) {
                    LogUtils.error(e.getMessage(), e);
                }


                ApiDefinitionDetail detail = buildApiDefinition(reqName, url, harRequest.method, null, importRequest);
                MsHTTPElement request = super.buildHttpRequest(reqName, url, harRequest.method);
                parseParameters(harRequest, request);
                parseRequestBody(harRequest, request.getBody());
                detail.setRequest(request);
                detail.setResponse(Collections.singletonList(parseResponse(entry.response)));
                resultList.add(detail);
            }
        }

        return resultList;
    }
    //    private void addBodyHeader(MsHTTPElement request) {
    //        String contentType = StringUtils.EMPTY;
    //        if (request.getBody() != null && StringUtils.isNotBlank(request.getBody().getType())) {
    //            switch (request.getBody().getType()) {
    //                case Body.WWW_FROM:
    //                    contentType = "application/x-www-form-urlencoded";
    //                    break;
    //                case Body.JSON_STR:
    //                    contentType = "application/json";
    //                    break;
    //                case Body.XML:
    //                    contentType = "application/xml";
    //                    break;
    //                case Body.BINARY:
    //                    contentType = "application/octet-stream";
    //                    break;
    //            }
    //            List<KeyValue> headers = request.getHeaders();
    //            if (headers == null) {
    //                headers = new ArrayList<>();
    //                request.setHeaders(headers);
    //            }
    //            if (StringUtils.isNotEmpty(contentType)) {
    //                addContentType(request.getHeaders(), contentType);
    //            }
    //        }
    //    }

    private void parseParameters(HarRequest harRequest, MsHTTPElement request) {
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


    private void parseCookieParameters(HarCookie harCookie, List<MsHeader> headers) {
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

    protected void addHeader(List<MsHeader> headers, String key, String value, String description) {
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

    private void parseHeaderParameters(HarHeader harHeader, List<MsHeader> headers) {
        String key = harHeader.name;
        String value = harHeader.value;
        String description = harHeader.comment;
        this.addHeader(headers, key, value, description);
    }

    private HttpResponse parseResponse(HarResponse response) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new ResponseBody());
        msResponse.setName("har");
        msResponse.setHeaders(new ArrayList<>());
        if (response != null) {
            msResponse.setStatusCode(String.valueOf(response.status));
            parseResponseHeader(response, msResponse.getHeaders());
            parseResponseBody(response.content, msResponse.getBody());
        }
        return msResponse;
    }

    private void parseResponseHeader(HarResponse response, List<MsHeader> msHeaders) {
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


    private void parseRequestBody(HarRequest requestBody, Body body) {
        if (requestBody == null) {
            return;
        }
        HarPostData content = requestBody.postData;
        if (StringUtils.equalsIgnoreCase("GET", requestBody.method) || requestBody.postData == null) {
            return;
        }
        String bodyType = content.mimeType;
        if (StringUtils.isEmpty(bodyType)) {
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
                if (bodyType.contains("boundary=") && StringUtils.contains(content.text, this.getBoundaryFromContentType(bodyType))) {
                    String[] textArr = StringUtils.split(content.text, "\r\n");
                    String paramData = this.parseMultipartByTextArr(textArr);
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
                List<HarPostParam> postParams = content.params;
                //                for (HarPostParam postParam : postParams) {
                //                    KeyValue kv = new KeyValue(postParam.name, postParam.value);
                //                    body.getFormDataBody().add(kv);
                //                }
            } else {
                body.setRawBody(new RawBody() {{
                    this.setValue(content.text);
                }});
            }
        }
        body.setBodyType(bodyType);
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


    private void parseResponseBody(HarContent content, ResponseBody body) {
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

    private void parseQueryParameters(HarQueryParam harQueryParam, List<QueryParam> arguments) {
        arguments.add(new QueryParam() {{
            this.setKey(harQueryParam.name);
            this.setValue(harQueryParam.value);
            this.setDescription(harQueryParam.comment);
        }});
    }
}
