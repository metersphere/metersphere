package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.har.HarUtils;
import io.metersphere.api.dto.definition.parse.har.model.*;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtils;
import io.swagger.models.Model;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
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
            String sourceStr = getApiTestStr(source);
            har = HarUtils.read(sourceStr);
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        }

        if (ObjectUtils.isEmpty(har)) {
            MSException.throwException("解析失败，请确认选择的是 Har 格式！");
        }

        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(har, request));
        return definitionImport;
    }


    private List<ApiDefinitionWithBLOBs> parseRequests(Har har, ApiTestImportRequest importRequest) {
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        ApiModule parentNode = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());

        List<HarEntry> harEntryList = new ArrayList<>();
        if (har.log != null && har.log.entries != null) {
            harEntryList = har.log.entries;
        }

        for (HarEntry entry : harEntryList) {
            HarRequest harRequest = entry.request;

            //默认取路径的最后一块
            String reqName = "";
            if (harRequest.url != null) {
                String[] nameArr = harRequest.url.split("/");
                reqName = nameArr[nameArr.length - 1];
            }

            if (harRequest != null) {
                MsHTTPSamplerProxy request = super.buildRequest(reqName, harRequest.url, harRequest.method);
                ApiDefinitionWithBLOBs apiDefinition = super.buildApiDefinition(request.getId(), reqName, harRequest.url, harRequest.method, importRequest);
                parseParameters(harRequest, request);
                parseRequestBody(harRequest, request.getBody());
                addBodyHeader(request);
                apiDefinition.setRequest(JSON.toJSONString(request));
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(entry.response)));
                buildModule(parentNode, apiDefinition, null);
                results.add(apiDefinition);
            }
        }

        return results;
    }

    private void parseParameters(HarRequest harRequest, MsHTTPSamplerProxy request) {

//        parsePathParameters(parameter, request.getRest());
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


    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(HarCookie harCookie, List<KeyValue> headers) {
        addCookie(headers, harCookie.name, harCookie.value, harCookie.comment, false);
    }

    private void parseHeaderParameters(HarHeader harHeader, List<KeyValue> headers) {
        addHeader(headers, harHeader.name, harHeader.value,harHeader.comment, "", false);
    }

    private HttpResponse parseResponse(HarResponse response) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestType.HTTP);
        // todo 状态码要调整？
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
        if (!StringUtils.equalsIgnoreCase("GET", requestBody.method) || requestBody.postData == null) {
            return;
        }
        // 多个contentType ，优先取json
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


    private void parseResponseBody(HarContent content, Body body) {
        if (content == null) {
            return;
        }
        String contentType = content.mimeType;
        body.setType(getBodyType(contentType));
        body.setRaw(content.text);
    }

    private void parseKvBody(Schema schema, Body body, Object data, Map<String, Schema> infoMap) {
        if (data instanceof JSONObject) {
            ((JSONObject) data).forEach((k, v) -> {
                KeyValue kv = new KeyValue(k, v.toString());
                Schema schemaInfo = infoMap.get(k);
                if (schemaInfo != null) {
                    kv.setDescription(schemaInfo.getDescription());
//                    kv.setRequired(schemaInfo.getRequired());
                    if (schemaInfo instanceof BinarySchema) {
                        kv.setType("file");
                    }
                }
                body.getKvs().add(kv);
            });
        } else {
            KeyValue kv = new KeyValue(schema.getName(), data.toString(), schema.getDescription());
            Schema schemaInfo = infoMap.get(schema.getName());
            if (schemaInfo != null) {
                kv.setDescription(schemaInfo.getDescription());
                if (schemaInfo instanceof BinarySchema) {
                    kv.setType("file");
                }
            }
            body.getKvs().add(kv);
        }
    }

    private String parseXmlBody(String xmlString) {
        JSONObject object = JSONObject.parseObject(getDefaultStringValue(xmlString));
        return XMLUtils.jsonToXmlStr(object);
    }

//    private Schema getModelByRef(String ref) {
//        if (StringUtils.isBlank(ref)) {
//            return null;
//        }
//        if (ref.split("/").length > 3) {
//            ref = ref.replace("#/components/schemas/", "");
//        }
//        return this.components.getSchemas().get(ref);
//    }

//    private Object parseSchema(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
//        infoMap.put(schema.getName(), schema);
//        if (StringUtils.isNotBlank(schema.get$ref())) {
//            if (refSet.contains(schema.get$ref())) {
//                return new JSONObject();
//            }
//            refSet.add(schema.get$ref());
//            Object propertiesResult = parseSchemaProperties(getModelByRef(schema.get$ref()), refSet, infoMap);
//            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
//        } else if (schema instanceof ArraySchema) {
//            JSONArray jsonArray = new JSONArray();
//            Schema items = ((ArraySchema) schema).getItems();
//            parseSchema(items, refSet, infoMap);
//            jsonArray.add(parseSchema(items, refSet, infoMap));
//            return jsonArray;
//        } else if (schema instanceof BinarySchema) {
//            return getDefaultValueByPropertyType(schema);
//        } else {
//            Object propertiesResult = parseSchemaProperties(schema, refSet, infoMap);
//            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
//        }
//    }

//    private Object parseSchemaProperties(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
//        if (schema == null) {
//            return null;
//        }
//        Map<String, Schema> properties = schema.getProperties();
//        if (MapUtils.isEmpty(properties)) {
//            return null;
//        }
//        JSONObject jsonObject = new JSONObject();
//        properties.forEach((key, value) -> {
//            jsonObject.put(key, parseSchema(value, refSet, infoMap));
//        });
//        return jsonObject;
//    }


    private void parseQueryParameters(HarQueryParm harQueryParm, List<KeyValue> arguments) {
//        io.swagger.v3.oas.models.parameters.QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(harQueryParm.name, harQueryParm.value, harQueryParm.comment, false));
    }
}
