package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.parse.har.model.*;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.PostmanEvent;
import io.metersphere.api.dto.parse.postman.PostmanKeyValue;
import io.metersphere.api.dto.parse.postman.PostmanRequest;
import io.metersphere.api.dto.parse.postman.PostmanScript;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import io.metersphere.commons.utils.XMLUtils;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class HarScenarioAbstractParser<T> extends ApiImportAbstractParser<T> {

    protected MsHTTPSamplerProxy parseHar(HarEntry harEntry) {
        HarRequest harRequest = harEntry.request;
        if (harRequest == null) {
            return null;
        }
        MsHTTPSamplerProxy request = buildRequest(harRequest.url, harRequest.url,harRequest.method);
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
        List<HarQueryParm> queryStringList = harRequest.queryString;
        queryStringList.forEach(harQueryParm -> {
            parseQueryParameters(harQueryParm, request.getArguments());
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
        }else if(requestBody.postData == null){
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
                    KeyValue kv = new KeyValue(postParam.name,postParam.value);
                    body.getKvs().add(kv);
                }
            } else if (contentType.startsWith(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)) {
                contentType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
                List<HarPostParam> postParams = content.params;
                for (HarPostParam postParam : postParams) {
                    KeyValue kv = new KeyValue(postParam.name,postParam.value);
                    body.getKvs().add(kv);
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
                    KeyValue kv = new KeyValue(postParam.name,postParam.value);
                    body.getKvs().add(kv);
                }
            } else {
                body.setRaw(content.text);
            }
        }
        body.setType(getBodyType(contentType));
    }


    private void parseQueryParameters(HarQueryParm harQueryParm, List<KeyValue> arguments) {
        arguments.add(new KeyValue(harQueryParm.name, harQueryParm.value, harQueryParm.comment, false));
    }
    private void parseCookieParameters(HarCookie harCookie, List<KeyValue> headers) {
        addCookie(headers, harCookie.name, harCookie.value, harCookie.comment, false);
    }

    private void parseHeaderParameters(HarHeader harHeader, List<KeyValue> headers) {
        addHeader(headers, harHeader.name, harHeader.value,harHeader.comment, "", false);
    }

    private void addPreScript(MsHTTPSamplerProxy request, List<PostmanEvent> event) {
        if (request != null && CollectionUtils.isNotEmpty(event)) {
            StringBuilder scriptStr = new StringBuilder();
            event = event.stream()
                    .filter(item -> item.getScript() != null)
                    .collect(Collectors.toList());
            event.forEach(item -> {
                PostmanScript script = item.getScript();
                if (script != null && item.getListen().contains("prerequest")) {
                    List<String> exec = script.getExec();
                    if (CollectionUtils.isNotEmpty(exec)) {
                        exec.forEach(col -> {
                            if (StringUtils.isNotEmpty(col)) {
                                scriptStr.append(col + "\n");
                            }
                        });
                    }
                }
            });
            if (StringUtils.isNotBlank(scriptStr)) {
                MsJSR223PreProcessor jsr223PreProcessor = new MsJSR223PreProcessor();
                jsr223PreProcessor.setName("JSR223PreProcessor");
                jsr223PreProcessor.setScriptLanguage("javascript");
                jsr223PreProcessor.setScript(scriptStr.toString());
                LinkedList<MsTestElement> hashTree = new LinkedList<>();
                hashTree.add(jsr223PreProcessor);
                request.setHashTree(hashTree);
            }
        }
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new KeyValue(item.getKey(), item.getValue(), item.getDescription(), item.getContentType())));
        return keyValues;
    }

    private void parseBody(Body body, PostmanRequest requestDesc) {
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.isBlank(bodyMode)) {
            return;
        }
        if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            parseRawBody(body, postmanBody, bodyMode);
        } else if (StringUtils.equalsAny(bodyMode, PostmanRequestBodyMode.FORM_DATA.value(), PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
            body.setKvs(parseKeyValue(postmanKeyValues));
            if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value())) {
                body.setType(Body.FORM_DATA);
            } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
                body.setType(Body.WWW_FROM);
            }
        } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FILE.value())) {
            body.setType(Body.BINARY);
            body.setKvs(new ArrayList<>());
        }
    }

    private String parseXmlBody(String xmlString) {
        JSONObject object = JSONObject.parseObject(getDefaultStringValue(xmlString));
        return XMLUtils.jsonToXmlStr(object);
    }

    private void parseRawBody(Body body, JSONObject postmanBody, String bodyMode) {
        body.setRaw(postmanBody.getString(bodyMode));
        body.setType(MsRequestBodyType.RAW.value());
        JSONObject options = postmanBody.getJSONObject("options");
        if (options != null) {
            JSONObject raw = options.getJSONObject(PostmanRequestBodyMode.RAW.value());
            if (raw != null) {
                String bodyType = "";
                switch (raw.getString("language")) {
                    case "json":
                        bodyType = Body.JSON;
                        break;
                    case "xml":
                        bodyType = Body.XML;
                        break;
                    default:
                        bodyType = Body.RAW;
                }
                body.setType(bodyType);
            }
        }
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }
}
