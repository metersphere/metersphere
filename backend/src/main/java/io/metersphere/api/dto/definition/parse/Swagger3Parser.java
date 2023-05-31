package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.SwaggerApiExportResult;
import io.metersphere.api.dto.definition.parse.swagger.SwaggerApiInfo;
import io.metersphere.api.dto.definition.parse.swagger.SwaggerInfo;
import io.metersphere.api.dto.definition.parse.swagger.SwaggerParams;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.JsonSchemaItem;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.BasicConstant;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtils;
import io.metersphere.i18n.Translator;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.io.InputStream;
import java.util.*;


public class Swagger3Parser extends SwaggerAbstractParser {

    private Components components;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String sourceStr = "";
        if (StringUtils.isBlank(request.getSwaggerUrl())) {
            sourceStr = getApiTestStr(source);
        }
        return parse(sourceStr, request);
    }

    public ApiDefinitionImport parse(String sourceStr, ApiTestImportRequest request) {

        List<AuthorizationValue> auths = setAuths(request);
        SwaggerParseResult result = null;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            try {
                result = new OpenAPIParser().readLocation(request.getSwaggerUrl(), auths, null);
            } catch (Exception e) {
                MSException.throwException(e.getMessage());
            }
        } else {
            result = new OpenAPIParser().readContents(sourceStr, null, null);
        }
        if (result == null || result.getOpenAPI() == null) {
            MSException.throwException(Translator.get(CollectionUtils.isEmpty(auths) ?
                    "swagger_parse_error" : "swagger_parse_error_with_auth"));
        }
        OpenAPI openAPI = result.getOpenAPI();
        if (result.getMessages() != null) {
            result.getMessages().forEach(LogUtil::error); // validation errors and warnings
        }
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(openAPI, request));
        return definitionImport;
    }


    // 鉴权设置
    private List<AuthorizationValue> setAuths(ApiTestImportRequest request) {
        List<AuthorizationValue> auths = new ArrayList<>();
        // 如果有 BaseAuth 参数，base64 编码后转换成 headers
        if (request.getAuthManager() != null
                && StringUtils.isNotBlank(request.getAuthManager().getUsername())
                && StringUtils.isNotBlank(request.getAuthManager().getPassword())
                && MsAuthManager.mechanismMap.containsKey(request.getAuthManager().getVerification())) {
            AuthorizationValue authorizationValue = new AuthorizationValue();
            authorizationValue.setType("header");
            authorizationValue.setKeyName("Authorization");
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((request.getAuthManager().getUsername()
                    + ":" + request.getAuthManager().getPassword()).getBytes());
            authorizationValue.setValue(authValue);
            auths.add(authorizationValue);
        }
        // 设置 headers
        if (!CollectionUtils.isEmpty(request.getHeaders())) {
            for (KeyValue keyValue : request.getHeaders()) {
                // 当有 key 时才进行设置
                if (StringUtils.isNotBlank(keyValue.getName())) {
                    AuthorizationValue authorizationValue = new AuthorizationValue();
                    authorizationValue.setType("header");
                    authorizationValue.setKeyName(keyValue.getName());
                    authorizationValue.setValue(keyValue.getValue());
                    auths.add(authorizationValue);
                }
            }
        }
        // 设置 query 参数
        if (!CollectionUtils.isEmpty(request.getArguments())) {
            for (KeyValue keyValue : request.getArguments()) {
                if (StringUtils.isNotBlank(keyValue.getName())) {
                    AuthorizationValue authorizationValue = new AuthorizationValue();
                    authorizationValue.setType("query");
                    authorizationValue.setKeyName(keyValue.getName());
                    authorizationValue.setValue(keyValue.getValue());
                    auths.add(authorizationValue);
                }
            }
        }
        return CollectionUtils.size(auths) == 0 ? null : auths;
    }


    private List<ApiDefinitionWithBLOBs> parseRequests(OpenAPI openAPI, ApiTestImportRequest importRequest) {
        Paths paths = openAPI.getPaths();

        Set<String> pathNames = paths.keySet();

        this.components = openAPI.getComponents();

        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        for (String pathName : pathNames) {
            PathItem pathItem = paths.get(pathName);

            Map<String, Operation> operationsMap = new HashMap<>();
            operationsMap.put(HttpMethod.GET.name(), pathItem.getGet());
            operationsMap.put(HttpMethod.POST.name(), pathItem.getPost());
            operationsMap.put(HttpMethod.DELETE.name(), pathItem.getDelete());
            operationsMap.put(HttpMethod.PUT.name(), pathItem.getPut());
            operationsMap.put(HttpMethod.PATCH.name(), pathItem.getPatch());
            operationsMap.put(HttpMethod.HEAD.name(), pathItem.getHead());
            operationsMap.put(HttpMethod.OPTIONS.name(), pathItem.getOptions());
            operationsMap.put(HttpMethod.TRACE.name(), pathItem.getTrace());

            for (String method : operationsMap.keySet()) {
                Operation operation = operationsMap.get(method);
                if (operation != null) {
                    MsHTTPSamplerProxy request = buildRequest(operation, pathName, method);
                    request.setFollowRedirects(true);
                    request.setResponseTimeout("60000");
                    request.setConnectTimeout("60000");
                    ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method, importRequest);
                    apiDefinition.setDescription(operation.getDescription());
                    parseParameters(operation, request);
                    parseRequestBody(operation.getRequestBody(), request.getBody());
                    addBodyHeader(request);
                    if (request.getBody().getKvs().size() > 1 && request.getBody().getKvs().get(0).getName() == null) {
                        request.getBody().getKvs().remove(0);
                    }   //  有数据的话，去掉 Kvs 里初始化的第一个全 null 的数据，否则有空行
                    apiDefinition.setRequest(JSON.toJSONString(request));
                    apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation.getResponses())));
                    buildModulePath(apiDefinition, operation.getTags());
                    if (operation.getDeprecated() != null && operation.getDeprecated()) {
                        apiDefinition.setTags("[\"Deleted\"]");
                    }
                    results.add(apiDefinition);
                }
            }
        }

        return results;
    }

    private void buildModulePath(ApiDefinitionWithBLOBs apiDefinition, List<String> tags) {
        StringBuilder modulePathBuilder = new StringBuilder();
        String modulePath = getModulePath(tags, modulePathBuilder);
        apiDefinition.setModulePath(modulePath);
    }

    private ApiDefinitionWithBLOBs buildApiDefinition(String id, Operation operation, String path, String method, ApiTestImportRequest importRequest) {
        String name;
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        return buildApiDefinition(id, name, path, method, importRequest);
    }

    private MsHTTPSamplerProxy buildRequest(Operation operation, String path, String method) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else {
            name = operation.getOperationId();
        }
        return buildRequest(name, path, method);
    }

    private void parseParameters(Operation operation, MsHTTPSamplerProxy request) {

        List<Parameter> parameters = operation.getParameters();

        if (CollectionUtils.isEmpty(parameters)) {
            return;
        }

        // todo 路径变量 {xxx} 是否要转换
        parameters.forEach(parameter -> {
            if (parameter instanceof QueryParameter) {
                parseQueryParameters(parameter, request.getArguments());
            } else if (parameter instanceof PathParameter) {
                parsePathParameters(parameter, request.getRest());
            } else if (parameter instanceof HeaderParameter) {
                parseHeaderParameters(parameter, request.getHeaders());
            } else if (parameter instanceof CookieParameter) {
                parseCookieParameters(parameter, request.getHeaders());
            }
        });
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(),
                pathParameter.getExample() != null ? String.valueOf(pathParameter.getExample()) : null,
                getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(),
                cookieParameter.getExample() != null ? String.valueOf(cookieParameter.getExample()) : null,
                getDefaultStringValue(cookieParameter.getDescription()), parameter.getRequired());
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(),
                headerParameter.getExample() != null ? String.valueOf(headerParameter.getExample()) : null,
                getDefaultStringValue(headerParameter.getDescription()), StringUtils.EMPTY, parameter.getRequired());
    }

    private HttpResponse parseResponse(ApiResponses responses) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestType.HTTP);
        // todo 状态码要调整？
        msResponse.setStatusCode(new ArrayList<>());
        if (responses != null) {
            ApiResponse apiResponse = responses.get("200");
            if (apiResponse == null) {
                responses.forEach((responseCode, response) -> {
                    parseResponseHeader(response, msResponse.getHeaders());
                    parseResponseBody(response, msResponse.getBody());
                });
            } else {
                parseResponseHeader(apiResponse, msResponse.getHeaders());
                parseResponseBody(apiResponse, msResponse.getBody());
            }
            responses.forEach((responseCode, response) -> {
                parseResponseCode(msResponse.getStatusCode(), responseCode, response);
            });
        }
        return msResponse;
    }

    private void parseResponseHeader(ApiResponse response, List<KeyValue> msHeaders) {
        Map<String, Header> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> {
                msHeaders.add(new KeyValue(k, String.valueOf(v.getExample()), v.getDescription()));
            });
        }
    }

    private void parseResponseCode(List<KeyValue> statusCode, String responseCode, ApiResponse response) {
        try {
            statusCode.add(new KeyValue(responseCode, response.getDescription(), response.getDescription()));
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    private void parseResponseBody(ApiResponse response, Body body) {
        body.setRaw(response.getDescription());
        Content content = response.getContent();
        if (content == null) {
            body.setType(Body.RAW);
            body.setRaw(response.getDescription());
        } else {
            parseBody(response.getContent(), body);
        }
    }

    private void parseRequestBody(RequestBody requestBody, Body body) {
        if (requestBody == null) {
            return;
        }
        parseBody(requestBody.getContent(), body);
    }

    private void parseBody(Content content, Body body) {
        if (content == null) {
            return;
        }
        // 多个contentType ，优先取json
        String contentType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
        MediaType mediaType = content.get(contentType);
        if (mediaType == null) {
            Set<String> contentTypes = content.keySet();
            if (contentTypes.size() == 0) {  //  防止空指针
                return;
            }
            contentType = contentTypes.iterator().next();
            if (StringUtils.isBlank(contentType)) {
                return;
            }
            mediaType = content.get(contentType);
            if (contentType.equals("*/*")) {
                contentType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
            }
        }

        Set<String> refSet = new HashSet<>();
        Map<String, Schema> infoMap = new HashMap<>();
        Schema schema = getSchema(mediaType.getSchema());
        if (content.get(contentType) != null && content.get(contentType).getExample() != null && schema.getExample() == null) {
            schema.setExample(content.get(contentType).getExample());
        }
        Object bodyData = null;
        if (!StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
            bodyData = parseSchemaToJson(schema, refSet, infoMap);
            if (bodyData == null) return;
        }

        body.setType(getBodyType(contentType));

        if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
            JsonSchemaItem jsonSchemaItem = parseSchema(schema, refSet);
            if (jsonSchemaItem == null) {
                jsonSchemaItem = new JsonSchemaItem();
                if (schema != null && StringUtils.isNotBlank(schema.getType())) {
                    jsonSchemaItem.setType(schema.getType());
                }
            }
            if (MapUtils.isEmpty(jsonSchemaItem.getProperties())) {
                jsonSchemaItem.setProperties(new HashMap<>());
            }
            body.setJsonSchema(jsonSchemaItem);
            body.setFormat("JSON-SCHEMA");
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_XML_VALUE)) {
            body.setRaw(parseXmlBody(schema, bodyData));
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else {
            body.setRaw(bodyData.toString());
        }
    }

    private Object parseSchemaToJson(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
        if (schema == null) {
            return new JSONObject(true);
        }
        infoMap.put(schema.getName(), schema);
        if (StringUtils.isNotBlank(schema.get$ref())) {
            if (refSet.contains(schema.get$ref())) {
                return new JSONObject(true);
            }
            refSet.add(schema.get$ref());
            Schema modelByRef = getModelByRef(schema.get$ref());
            Object propertiesResult = null;
            if (modelByRef != null)
                propertiesResult = parseSchemaPropertiesToJson(modelByRef, refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        } else if (schema instanceof ArraySchema) {
            JSONArray jsonArray = new JSONArray();
            Schema items = ((ArraySchema) schema).getItems();
            Object itemObject = parseSchemaToJson(items, refSet, infoMap);
            jsonArray.add(itemObject);
            return jsonArray;
        } else if (schema instanceof BinarySchema) {
            return getDefaultValueByPropertyType(schema);
        } else if (schema instanceof ObjectSchema) {
            Object propertiesResult = parseSchemaPropertiesToJson(schema, refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        } else {
            if (MapUtils.isNotEmpty(schema.getProperties())) {
                Object propertiesResult = parseSchemaPropertiesToJson(schema, refSet, infoMap);
                return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
            } else {
                return getDefaultValueByPropertyType(schema);
            }
        }
    }

    private Object parseSchemaPropertiesToJson(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
        if (schema == null) return null;
        Map<String, Schema> properties = schema.getProperties();
        if (MapUtils.isEmpty(properties)) return null;
        JSONObject jsonObject = new JSONObject(true);
        properties.forEach((key, value) -> {
            if (StringUtils.isBlank(value.getName())) {
                value.setName(key);
            }
            jsonObject.put(key, parseSchemaToJson(value, refSet, infoMap));
        });
        return jsonObject;
    }

    private void parseKvBody(Schema schema, Body body, Object data, Map<String, Schema> infoMap) {
        if (data instanceof JSONObject) {
            if (MapUtils.isNotEmpty(schema.getProperties())) {
                schema.getProperties().forEach((key, value) -> {
                    if (value instanceof Schema) {
                        parseKvBodyItem(value, body, key.toString(), infoMap);
                    }
                });
            } else {
                ((JSONObject) data).forEach((k, v) -> {
                    parseKvBodyItem(schema, body, k, infoMap);
                });
            }
        } else {
            if (data instanceof Schema) {
                Schema dataSchema = (Schema) data;
                if (StringUtils.isNotBlank(dataSchema.getName())) {
                    parseKvBodyItem(schema, body, dataSchema.getName(), infoMap);
                } else if (dataSchema.getProperties() != null) {
                    dataSchema.getProperties().forEach((k, v) -> {
                        if (v instanceof Schema) {
                            parseKvBodyItem(v, body, k.toString(), infoMap);
                        }
                    });
                }
            }
        }
    }

    private void parseKvBodyItem(Object schemaObject, Body body, String name, Map<String, Schema> infoMap) {
        Schema schema = (Schema) schemaObject;
        if (schema == null) return;
        KeyValue kv = new KeyValue(name, String.valueOf(schema.getExample() == null ? "" : schema.getExample()), schema.getDescription());
        Schema schemaInfo = infoMap.get(name);
        if (schemaInfo != null) {
            if (schemaInfo instanceof BinarySchema) {
                kv.setType("file");
            }
        }
        if (StringUtils.isNotBlank(schema.getType()) && StringUtils.equals("file", schema.getType())) {
            kv.setType("file");
        }
        if (body != null) {
            if (body.getKvs() == null) {
                body.setKvs(new ArrayList<>());
            }
            body.getKvs().add(kv);
        }
    }

    private String parseXmlBody(Schema schema, Object data) {
        if (data instanceof JSONObject) {
            if (((JSONObject) data).keySet().size() > 1) {
                JSONObject object = new JSONObject();
                if (StringUtils.isNotBlank(schema.get$ref())) {
                    String ref = schema.get$ref();
                    if (ref.split("/").length > 3) {
                        ref = ref.replace("#/components/schemas/", StringUtils.EMPTY);
                        object.put(ref, data);
                        return XMLUtils.jsonToPrettyXml(object);
                    }
                }
            }
            return XMLUtils.jsonToPrettyXml((JSONObject) data);
        } else {
            JSONObject object = new JSONObject();
            if (StringUtils.isNotBlank(schema.getName())) {
                object.put(schema.getName(), schema.getExample());
            }
            return XMLUtils.jsonToPrettyXml(object);
        }
    }

    private Schema getModelByRef(String ref) {
        if (StringUtils.isBlank(ref)) {
            return null;
        }
        if (ref.split("/").length > 3) {
            ref = ref.replace("#/components/schemas/", "");
        }
        if (this.components.getSchemas() != null) {
            Schema schema = this.components.getSchemas().get(ref);
            if (ObjectUtils.isNotEmpty(schema)) {
                schema.setName(ref);
            }
            return schema;
        }
        return null;
    }

    private JsonSchemaItem parseSchema(Schema schema, Set<String> refSet) {
        if (schema == null) return null;
        JsonSchemaItem item = new JsonSchemaItem();
        if (schema.getRequired() != null) {
            item.setRequired(schema.getRequired());
        }
        if (StringUtils.isNotBlank(schema.get$ref())) {
            if (refSet.contains(schema.get$ref())) return item;
            item.setType("object");
            refSet.add(schema.get$ref());
            Schema modelByRef = getModelByRef(schema.get$ref());
            if (modelByRef != null) {
                item.setProperties(parseSchemaProperties(modelByRef, refSet));
                item.setRequired(modelByRef.getRequired());
            }
        } else if (schema instanceof ArraySchema) {
            Schema items = ((ArraySchema) schema).getItems();
            item.setType("array");
            JsonSchemaItem arrayItem = parseSchema(items, refSet);
            Map<String, String> mock = new LinkedHashMap<>();
            if (arrayItem != null && MapUtils.isNotEmpty(arrayItem.getProperties())) {
                arrayItem.getProperties().forEach((k, v) -> {
                    mock.put(k, StringUtils.isBlank(v.getMock().get("mock").toString()) ? v.getType() :
                            v.getMock().get("mock").toString());
                });
            }
            item.getMock().put("mock", JSON.toJSONString(mock));
        } else if (schema instanceof ObjectSchema) {
            item.setType("object");
            item.setProperties(parseSchemaProperties(schema, refSet));
        } else if (schema instanceof StringSchema) {
            item.setType("string");
        } else if (schema instanceof IntegerSchema) {
            item.setType("integer");
        } else if (schema instanceof NumberSchema) {
            item.setType("number");
        } else if (schema instanceof BooleanSchema) {
            item.setType("boolean");
        } else {
            return null;
        }
        if (schema.getExample() != null) {
            item.getMock().put("mock", schema.getExample());
        } else if (StringUtils.isNotBlank(item.getMock().get("mock").toString())) {
            item.getMock().put("mock", item.getMock().get("mock"));
        } else {
            item.getMock().put("mock", "");
        }

        item.setDescription(schema.getDescription());
        item.setPattern(schema.getPattern());
        item.setMaxLength(schema.getMaxLength());
        item.setMinLength(schema.getMinLength());
        Object aDefault = schema.getDefault();
        item.setDefaultValue(aDefault);
        item.setMaximum(schema.getMaximum());
        item.setMinimum(schema.getMinimum());
        return item;
    }

    private Map<String, JsonSchemaItem> parseSchemaProperties(Schema schema, Set<String> refSet) {
        if (schema == null) return null;
        Map<String, Schema> properties = schema.getProperties();
        if (MapUtils.isEmpty(properties)) return null;
        Map<String, JsonSchemaItem> JsonSchemaProperties = new LinkedHashMap<>();
        properties.forEach((key, value) -> {
            JsonSchemaItem item = new JsonSchemaItem();
            if (StringUtils.isNotBlank(schema.getType())) {
                item.setType(schema.getType());
            }
            if (StringUtils.isNotBlank(schema.getDescription())) {
                item.setDescription(schema.getDescription());
            }
            JsonSchemaItem proItem = parseSchema(value, refSet);
            if (proItem != null) JsonSchemaProperties.put(key, proItem);
        });
        return JsonSchemaProperties;
    }

    private Object getDefaultValueByPropertyType(Schema value) {
        Object example = value.getExample();
        if (value instanceof IntegerSchema) {
            return example == null ? 0 : example;
        } else if (value instanceof NumberSchema) {
            return example == null ? 0.0 : example;
        } else if (value instanceof StringSchema || StringUtils.equals("string", value.getType()) || StringUtils.equals("text", value.getType()) || value instanceof JsonSchema) {
            if (example == null) {
                if (value.getXml() == null) {
                    return StringUtils.EMPTY;
                } else {
                    XML xml = value.getXml();
                    JSONObject jsonObject = new JSONObject();
                    if (xml.getWrapped() != null && xml.getWrapped()) {
                        jsonObject.put(xml.getName(), BasicConstant.OBJECT);
                    } else {
                        jsonObject.put(xml.getName(), BasicConstant.STRING);
                    }
                    return jsonObject;
                }
            } else {
                return example;
            }
        } else {// todo 其他类型?
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        Schema schema = getSchema(parameter.getSchema());
        Set<String> refSet = new HashSet<>();
        JsonSchemaItem jsonSchemaItem = parseSchema(schema, refSet);
        if (ObjectUtils.isNotEmpty(jsonSchemaItem)){
            if (MapUtils.isEmpty(jsonSchemaItem.getProperties())) {
                arguments.add(new KeyValue(queryParameter.getName(), getDefaultValue(queryParameter, jsonSchemaItem), getDefaultStringValue(queryParameter.getDescription()), parameter.getRequired(), getMin(jsonSchemaItem), getMax(jsonSchemaItem)));
            } else {
                Map<String, JsonSchemaItem> properties = jsonSchemaItem.getProperties();
                properties.forEach((key, value) -> {
                    arguments.add(new KeyValue(key, getDefaultValue(queryParameter, value),
                            getDefaultStringValue(value.getDescription()),
                            parameter.getRequired(),
                            getMin(value), getMax(value)));
                });
            }
        }
    }

    private Schema getSchema(Schema schema) {
        if (schema != null && StringUtils.isBlank(schema.get$ref()) && StringUtils.equalsIgnoreCase(schema.getType(), "string")) {
            ObjectSchema objectSchema = new ObjectSchema();
            objectSchema.setExample(schema.getExample());
            schema = objectSchema;
        }
        return schema;
    }

    private Integer getMax(JsonSchemaItem jsonSchemaItem) {
        if (jsonSchemaItem != null && jsonSchemaItem.getMaxLength() != null) {
            return jsonSchemaItem.getMaxLength();
        } else {
            return null;
        }
    }

    private Integer getMin(JsonSchemaItem jsonSchemaItem) {
        if (jsonSchemaItem != null && jsonSchemaItem.getMinLength() != null) {
            return jsonSchemaItem.getMinLength();
        } else {
            return null;
        }
    }

    private String getDefaultValue(QueryParameter queryParameter, JsonSchemaItem jsonSchemaItem) {
        if (queryParameter.getExample() != null) {
            return String.valueOf(queryParameter.getExample());
        } else {
            if (MapUtils.isNotEmpty(jsonSchemaItem.getMock())) {
                return String.valueOf(jsonSchemaItem.getMock().get("mock"));
            }
            if (jsonSchemaItem != null && jsonSchemaItem.getDefaultValue() != null) {
                return String.valueOf(jsonSchemaItem.getDefaultValue());
            }
            return null;
        }
    }

    /*    导出的 swagger json描述文件样例
    {
        "openapi":"3.0.1",
        "info":{},
        "externalDocs":{},
        "servers":{},
        "tags":{},
        "paths":{	//	对应 SwaggerApiExportResult 类的paths
            "/lzx/test/{ball}":{	//	key
                "get":{	//	对应 SwaggerPath 类的 JSONObject 类型的成员，”get“为key
                    "tags":[
                    "subModule2"
                    ],
                    "summary":"API",
                    "parameters":[
                        {
                            "name":"ballName",
                            "in":"query",//	path,header,query都可选。
                            "description":"描述param",
                            "required":true	//	是否必填参数
                        }
                    ],
                    "requestBody":{
                        "content":{
                            "application/octet-stream":{    //  type
                                "schema":{
                                    "type":null,
                                    "format":null
                                }
                            }
                        }
                    }
                }	//	SwaggerApiInfo类，为 value
            }
        },
        "components":{}
    }       */
    public SwaggerApiExportResult swagger3Export(List<ApiDefinitionWithBLOBs> apiDefinitionList, Project project) {
        SwaggerApiExportResult result = new SwaggerApiExportResult();

        result.setOpenapi("3.0.1");
        SwaggerInfo swaggerInfo = new SwaggerInfo();
        swaggerInfo.setVersion("1.0.1");
        swaggerInfo.setTitle("ms-" + project.getName());
        swaggerInfo.setDescription("");
        swaggerInfo.setTermsOfService("");
        result.setInfo(swaggerInfo);
        result.setServers(new ArrayList<>());
        result.setTags(new ArrayList<>());
        result.setComponents(new JSONObject());
        result.setExternalDocs(new JSONObject());

        JSONObject paths = new JSONObject();
        JSONObject components = new JSONObject();
        List<JSONObject> schemas = new LinkedList<>();
        for (ApiDefinitionWithBLOBs apiDefinition : apiDefinitionList) {
            SwaggerApiInfo swaggerApiInfo = new SwaggerApiInfo();   //  {tags:, summary:, description:, parameters:}
            swaggerApiInfo.setSummary(apiDefinition.getName());
            //  设置导入后的模块名 （根据 api 的 moduleID 查库获得所属模块，作为导出的模块名）
            //直接导出完整路径
            if (StringUtils.isNotBlank(apiDefinition.getModulePath())) {
                String[] split = new String[0];
                String modulePath = apiDefinition.getModulePath();
                String substring = modulePath.substring(0, 1);
                if (substring.equals("/")) {
                    modulePath = modulePath.substring(1);
                }
                if (modulePath.contains("/")) {
                    split = modulePath.split("/");
                }
                if (split.length == 0 && StringUtils.isNotBlank(modulePath)) {
                    split = new String[]{modulePath};
                }
                swaggerApiInfo.setTags(Arrays.asList(split));
            } else {
                swaggerApiInfo.setTags(new ArrayList<>());
            }

            //  设置请求体
            JSONObject requestObject = JSON.parseObject(apiDefinition.getRequest(), Feature.DisableSpecialKeyDetect);    //  将api的request属性转换成JSON对象以便获得参数
            JSONObject requestBody = buildRequestBody(requestObject, schemas);
            swaggerApiInfo.setRequestBody(requestBody);
            //  设置响应体
            JSONObject responseObject = JSON.parseObject(apiDefinition.getResponse(), Feature.DisableSpecialKeyDetect);
            swaggerApiInfo.setResponses(buildResponseBody(responseObject));
            //  设置请求参数列表
            List<JSONObject> paramsList = buildParameters(requestObject);
            swaggerApiInfo.setParameters(paramsList);
            swaggerApiInfo.setDescription(apiDefinition.getDescription());
            JSONObject methodDetail = JSON.parseObject(JSON.toJSONString(swaggerApiInfo), Feature.DisableSpecialKeyDetect);
            if (paths.getJSONObject(apiDefinition.getPath()) == null) {
                paths.put(apiDefinition.getPath(), new JSONObject());
            }   //  一个路径下有多个发方法，如post，get，因此是一个 JSONObject 类型
            paths.getJSONObject(apiDefinition.getPath()).put(apiDefinition.getMethod().toLowerCase(), methodDetail);
        }
        result.setPaths(paths);
        if (CollectionUtils.isNotEmpty(schemas)) {
            components.put("schemas", schemas.get(0));
        }
        result.setComponents(components);
        return result;
    }

    private List<JSONObject> buildParameters(JSONObject request) {
        List<JSONObject> paramsList = new ArrayList<>();
        Hashtable<String, String> typeMap = new Hashtable<String, String>() {{
            put("headers", "header");
            put("rest", "path");
            put("arguments", "query");
        }};
        Set<String> typeKeys = typeMap.keySet();
        for (String type : typeKeys) {
            JSONArray params = request.getJSONArray(type);  //  获得请求参数列表
            if (params != null) {
                for (int i = 0; i < params.size(); ++i) {
                    JSONObject param = params.getJSONObject(i); //  对于每个参数:
                    if (param.get("name") == null || StringUtils.isEmpty(((String) param.get("name")))) {
                        continue;
                    }   //  否则无参数的情况，可能多出一行空行
                    SwaggerParams swaggerParam = new SwaggerParams();
                    swaggerParam.setIn(typeMap.get(type));  //  利用 map，根据 request 的 key 设置对应的参数类型
                    swaggerParam.setDescription((String) param.get("description"));
                    swaggerParam.setName((String) param.get("name"));
                    swaggerParam.setRequired((boolean) param.get("required"));
                    swaggerParam.setExample((String) param.get("value"));
                    JSONObject schema = new JSONObject();
                    schema.put("type", "string");
                    swaggerParam.setSchema(schema);
                    paramsList.add(JSON.parseObject(JSON.toJSONString(swaggerParam), Feature.DisableSpecialKeyDetect));
                }
            }
        }
        return paramsList;
    }

    private JSONObject buildRequestBody(JSONObject request, List<JSONObject> schemas) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("content", buildContent(request, schemas));
        return requestBody;
    }

    //  将请求体中的一个 json 对象转换成 swagger 格式的 json 对象返回
    private JSONObject buildRequestBodyJsonInfo(JSONObject requestBody) {
        if (requestBody == null)
            return null;
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject properties = buildSchema(requestBody);
        schema.put("properties", properties);
        return schema;
    }

    //  请求体是 array 类型的情况
/* 例子："schema":{
        "type":"array",
            "items":{
            "type":"object",
            "properties":{
                "ids":{
                    "type":"string"
                }
            }
        }
    }   */
    private JSONObject buildRequestBodyJsonInfo(JSONArray requestBody) {
        if (requestBody == null)
            return null;
        JSONObject schema = new JSONObject();
        schema.put("type", "array");
        JSONObject items = new JSONObject();

        if (requestBody.size() > 0) {
            Object example = requestBody.get(0);
            if (example instanceof JSONObject) {
                items.put("type", "object");
                items.put("properties", buildSchema((JSONObject) example));
            } else if (example instanceof java.lang.String) {
                items.put("type", "string");
            } else if (example instanceof java.lang.Integer) {
                items.put("type", "integer");
                items.put("format", "int64");
            } else if (example instanceof java.lang.Boolean) {
                items.put("type", "boolean");
            } else if (example instanceof java.math.BigDecimal) {
                items.put("type", "double");
            } else {    //  JSONOArray
                items.put("type", "array");
                JSONObject item = new JSONObject();
                if (((JSONArray) example).size() > 0) {
                    if (((JSONArray) example).get(0) instanceof JSONObject) {
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) example).get(0));
                    }
                }
                items.put("items", item);
            }
        }
        schema.put("items", items);
        return schema;
    }

    private JSONObject buildJsonSchema(JSONObject requestBody, JSONArray required) {
        String type = requestBody.getString("type");

        JSONObject parsedParam = new JSONObject();
        if (required != null) {
            parsedParam.put("required", required);
        }
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.equals(type, "array")) {
                JSONObject itemProperties = new JSONObject();
                parsedParam.put("type", "array");
                JSONArray items = requestBody.getJSONArray("items");
                if (items != null) {
                    JSONObject itemsObject = new JSONObject();
                    if (items.size() > 0) {
                        items.forEach(item -> {
                            if (item instanceof JSONObject) {
                                JSONObject itemJson = buildJsonSchema((JSONObject) item, required);
                                if (itemJson != null) {
                                    Set<String> keys = itemJson.keySet();
                                    for (String key : keys) {
                                        itemProperties.put(key, itemJson.get(key));
                                    }
                                }
                            }
                        });
                    }
                    itemsObject.put("properties", itemProperties);
                    parsedParam.put("items", itemsObject.getJSONObject("properties"));
                } else {
                    parsedParam.put("items", new JSONObject());
                }

                if (StringUtils.isNotBlank(requestBody.getString("description"))) {
                    parsedParam.put("description", requestBody.getString("description"));
                }
            } else if (StringUtils.equals(type, "object")) {
                parsedParam.put("type", "object");
                JSONObject properties = requestBody.getJSONObject("properties");
                JSONObject jsonObject = buildFormDataSchema(properties);
                if (StringUtils.isNotBlank(requestBody.getString("description"))) {
                    parsedParam.put("description", requestBody.getString("description"));
                }
                parsedParam.put("properties", jsonObject.getJSONObject("properties"));
            } else if (StringUtils.equals(type, "integer")) {
                parsedParam.put("type", "integer");
                parsedParam.put("format", "int64");
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else if (StringUtils.equals(type, "boolean")) {
                parsedParam.put("type", "boolean");
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else if (StringUtils.equals(type, "number")) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put("type", "number");
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else {
                parsedParam.put("type", "string");
                setCommonJsonSchemaParam(parsedParam, requestBody);
            }
        }
        return parsedParam;
    }

    public void setCommonJsonSchemaParam(JSONObject parsedParam, JSONObject requestBody) {
        if (StringUtils.isNotBlank(requestBody.getString("description"))) {
            parsedParam.put("description", requestBody.getString("description"));
        }
        Object jsonSchemaValue = getJsonSchemaValue(requestBody);
        if (jsonSchemaValue != null) {
            parsedParam.put("example", jsonSchemaValue);
        }
    }

    public Object getJsonSchemaValue(JSONObject item) {
        JSONObject mock = item.getJSONObject("mock");
        if (mock != null) {
            Object value = mock.get("mock");
            return value;
        }
        return null;
    }

    //  设置一个 json 对象的属性在 swagger 格式中的类型、值
    private JSONObject buildSchema(JSONObject requestBody) {
        JSONObject schema = new JSONObject();
        for (String key : requestBody.keySet()) {
            Object param = requestBody.get(key);
            JSONObject parsedParam = new JSONObject();
            if (param instanceof java.lang.String) {
                parsedParam.put("type", "string");
                parsedParam.put("example", param == null ? "" : param);
            } else if (param instanceof java.lang.Integer) {
                parsedParam.put("type", "integer");
                parsedParam.put("format", "int64");
                parsedParam.put("example", param);
            } else if (param instanceof JSONObject) {
                parsedParam = buildRequestBodyJsonInfo((JSONObject) param);
            } else if (param instanceof java.lang.Boolean) {
                parsedParam.put("type", "boolean");
                parsedParam.put("example", param);
            } else if (param instanceof java.math.BigDecimal) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put("type", "double");
                parsedParam.put("example", param);
            } else {    //  JSONOArray
                parsedParam.put("type", "array");
                JSONObject item = new JSONObject();
                if (param == null) {
                    param = new JSONArray();
                }
                if (((JSONArray) param).size() > 0) {
                    if (((JSONArray) param).get(0) instanceof JSONObject) {  ///
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) param).get(0));
                    }
                }
                parsedParam.put("items", item);
            }
            schema.put(key, parsedParam);
        }
        return schema;
    }

    private static JSONObject buildRequestBodyXmlSchema(JSONObject requestBody) {
        if (requestBody == null) return null;
        JSONObject schema = new JSONObject();
        for (String key : requestBody.keySet()) {
            Object param = requestBody.get(key);
            JSONObject parsedParam = new JSONObject();
            if (param instanceof String) {
                parsedParam.put(BasicConstant.TYPE, BasicConstant.STRING);
                parsedParam.put("example", param == null ? StringUtils.EMPTY : param);
            } else if (param instanceof Integer) {
                parsedParam.put(BasicConstant.TYPE, BasicConstant.INTEGER);
                parsedParam.put("format", "int64");
                parsedParam.put("example", param);
            } else if (param instanceof JSONObject) {
                parsedParam.put(BasicConstant.TYPE, BasicConstant.OBJECT);
                Object attribute = ((JSONObject) param).get("attribute");
                //build properties
                JSONObject paramObject = buildRequestBodyXmlSchema((JSONObject) param);
                if (attribute != null && attribute instanceof JSONArray) {
                    JSONObject jsonObject = buildXmlProperties(((JSONArray) attribute).getJSONObject(0));
                    paramObject.remove("attribute");
                    for (String paramKey : paramObject.keySet()) {
                        Object paramChild = paramObject.get(paramKey);
                        if (paramChild instanceof String) {
                            JSONObject one = new JSONObject();
                            one.put(BasicConstant.TYPE, BasicConstant.OBJECT);
                            one.put("properties", jsonObject);
                            paramObject.remove("example");
                            paramObject.remove(paramKey);
                            paramObject.put(paramKey, one);
                        }
                        if (paramChild instanceof JSONObject) {
                            Object properties = ((JSONObject) paramChild).get("properties");
                            if (properties != null) {
                                for (String aa : jsonObject.keySet()) {
                                    Object value = jsonObject.get(aa);
                                    ((JSONObject) properties).putIfAbsent(aa, value);
                                }
                            } else {
                                ((JSONObject) paramChild).put("properties", jsonObject);
                            }
                            if (((JSONObject) paramChild).get("type") != null && ((JSONObject) paramChild).get("type") == "string") {
                                ((JSONObject) paramChild).put("type", "object");
                                ((JSONObject) paramChild).remove("example");
                            }
                        }
                    }
                }
                parsedParam.put("properties", paramObject);
                Object description = requestBody.get("description");
                if (description != null) {
                    if (StringUtils.isNotBlank(description.toString())) {
                        parsedParam.put("description", description);
                    }
                }

            } else if (param instanceof Boolean) {
                parsedParam.put(BasicConstant.TYPE, BasicConstant.BOOLEAN);
                parsedParam.put("example", param);
            } else if (param instanceof java.math.BigDecimal) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put(BasicConstant.TYPE, "double");
                parsedParam.put("example", param);
            } else {    //  JSONOArray
                parsedParam.put(BasicConstant.TYPE, BasicConstant.OBJECT);

                if (param == null) {
                    param = new JSONArray();
                }
                JSONObject jsonObjects = new JSONObject();
                if (((JSONArray) param).size() > 0) {
                    ((JSONArray) param).forEach(t -> {
                        JSONObject item = buildRequestBodyXmlSchema((JSONObject) t);
                        for (String s : item.keySet()) {
                            jsonObjects.put(s, item.get(s));
                        }
                    });
                }
                parsedParam.put(BasicConstant.PROPERTIES, jsonObjects);
            }
            schema.put(key, parsedParam);
        }

        return schema;
    }

    private JSONObject buildFormDataSchema(JSONObject kvs) {
        JSONObject schema = new JSONObject();
        JSONObject properties = new JSONObject();
        for (String key : kvs.keySet()) {
            JSONObject property = new JSONObject();
            JSONObject obj = ((JSONObject) kvs.get(key));
            property.put("type", StringUtils.isNotEmpty(obj.getString("type")) ? obj.getString("type") : "string");
            String value = obj.getString("value");
            if (StringUtils.isBlank(value)) {
                JSONObject mock = obj.getJSONObject("mock");
                if (mock != null) {
                    Object mockValue = mock.get("mock");
                    property.put("example", mockValue);
                } else {
                    property.put("example", value);
                }
            } else {
                property.put("example", value);
            }
            property.put("description", obj.getString("description"));
            property.put("required", obj.getString("required"));
            if (obj.getJSONObject("properties") != null) {
                JSONObject childProperties = buildFormDataSchema(obj.getJSONObject("properties"));
                property.put("properties", childProperties.getJSONObject("properties"));
            } else {
                JSONObject childProperties = buildJsonSchema(obj, new JSONArray());
                if (StringUtils.equalsIgnoreCase(obj.getString("type"), "array")) {
                    if (childProperties.getJSONObject("items") != null) {
                        property.put("items", childProperties.getJSONObject("items"));
                    }
                } else {
                    if (childProperties.getJSONObject("properties") != null) {
                        property.put("properties", childProperties.getJSONObject("properties"));
                    }
                }
            }
            properties.put(key, property);
        }
        schema.put("properties", properties);
        return schema;
    }

    private static JSONObject buildXmlProperties(JSONObject kvs) {
        JSONObject properties = new JSONObject();
        for (String key : kvs.keySet()) {
            JSONObject property = new JSONObject();
            Object param = kvs.get(key);
            if (param instanceof String) {
                property.put(BasicConstant.TYPE, BasicConstant.STRING);
                property.put("example", param);
            }
            if (param instanceof JSONObject) {
                JSONObject obj = ((JSONObject) param);
                property.put(BasicConstant.TYPE, StringUtils.isNotEmpty(obj.getString(BasicConstant.TYPE)) ? obj.getString(BasicConstant.TYPE) : BasicConstant.STRING);
                String value = obj.getString("value");
                if (StringUtils.isBlank(value)) {
                    JSONObject mock = obj.getJSONObject(BasicConstant.MOCK);
                    if (mock != null) {
                        Object mockValue = mock.get(BasicConstant.MOCK);
                        property.put("example", mockValue);
                    } else {
                        property.put("example", value);
                    }
                } else {
                    property.put("example", value);
                }
            }
            JSONObject xml = new JSONObject();
            xml.put("attribute", true);
            property.put("xml", xml);
            properties.put(key, property);
        }

        return properties;
    }


    private JSONObject getformDataProperties(JSONArray requestBody) {
        JSONObject result = new JSONObject();
        for (Object item : requestBody) {
            if (item instanceof JSONObject) {
                String name = ((JSONObject) item).getString("name");
                if (name != null) {
                    result.put(name, item);
                }
            }
        }
        return result;
    }

    /*  请求头格式：
        "headers":{
            "headerName":{
                "schema":{
                    "type":"string"
                }
            }
        }
    */
    private JSONObject buildResponseBody(JSONObject response) {
        if (response == null) {
            return new JSONObject();
        }
        JSONObject responseBody = new JSONObject();
        //  build 请求头
        JSONObject headers = new JSONObject();
        JSONArray headValueList = response.getJSONArray("headers");
        if (headValueList != null) {
            for (Object item : headValueList) {
                if (item instanceof JSONObject && ((JSONObject) item).getString("name") != null) {
                    JSONObject head = new JSONObject(), headSchema = new JSONObject();
                    head.put("description", ((JSONObject) item).getString("description"));
                    head.put("example", ((JSONObject) item).getString("value"));
                    headSchema.put("type", "string");
                    head.put("schema", headSchema);
                    headers.put(((JSONObject) item).getString("name"), head);
                }
            }
        }

        // 返回code
        JSONArray statusCode = response.getJSONArray("statusCode");
        if (statusCode != null) {
            for (int i = 0; i < statusCode.size(); i++) {
                JSONObject statusCodeInfo = new JSONObject();
                statusCodeInfo.put("headers", headers);
                statusCodeInfo.put("content", buildContent(response, null));
                statusCodeInfo.put("description", "");
                JSONObject jsonObject = statusCode.getJSONObject(i);
                if (jsonObject.get("value") != null) {
                    statusCodeInfo.put("description", jsonObject.get("value"));
                }
                if (jsonObject.get("name") != null) {
                    responseBody.put(jsonObject.get("name").toString(), statusCodeInfo);
                }
            }
        }
        return responseBody;
    }

    /*  请求体格式：
        "content":{
            "application/json":{
                "schema":{
                    "type":"xxx",
                    "xxx":{...}
                }
            }
        }
    */
    private JSONObject buildContent(JSONObject respOrReq, List<JSONObject> schemas) {
        Hashtable<String, String> typeMap = new Hashtable<String, String>() {{
            put("XML", org.springframework.http.MediaType.APPLICATION_XML_VALUE);
            put("JSON", org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            put("Raw", "application/urlencoded");
            put("BINARY", org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
            put("Form Data", org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
            put("WWW_FORM", org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }};
        Object bodyInfo = new Object();
        JSONObject body = respOrReq.getJSONObject("body");

        if (body != null) { //  将请求体转换成相应的格式导出
            String bodyType = body.getString("type");
            if (StringUtils.isNotBlank(bodyType) && bodyType.equalsIgnoreCase("JSON")) {
                try {
                    if (StringUtils.equals(body.getString("format"), "JSON-SCHEMA")) {
                        String jsonSchema = body.getString("jsonSchema");
                        if (StringUtils.isNotBlank(jsonSchema)) {
                            JSONObject jsonObject = JSONObject.parseObject(jsonSchema);
                            JSONArray required = new JSONArray();
                            if (jsonObject != null) {
                                required = jsonObject.getJSONArray("required");
                            }
                            if (required == null) {
                                JSONArray items = jsonObject.getJSONArray("items");
                                if (items != null && items.size() > 0) {
                                    JSONArray finalRequired = new JSONArray();
                                    items.forEach(item -> {
                                        if (item instanceof JSONObject) {
                                            JSONObject itemRequired = ((JSONObject) item).getJSONObject("required");
                                            finalRequired.add(itemRequired);
                                        }
                                    });
                                    required = finalRequired;
                                }
                            }
                            bodyInfo = buildJsonSchema(jsonObject, required);
                        }
                    } else {
                        try {    //  若请求体是一个 object
                            bodyInfo = buildRequestBodyJsonInfo(body.getJSONArray("raw"));
                        } catch (Exception e) {
                            bodyInfo = buildRequestBodyJsonInfo(body.getJSONObject("raw"));
                        }
                    }
                } catch (Exception e1) {    //  若请求体 json 不合法，则忽略错误，原样字符串导出/导入
                    bodyInfo = new JSONObject();
                    ((JSONObject) bodyInfo).put("type", "string");
                    if (body != null && body.get("raw") != null) {
                        ((JSONObject) bodyInfo).put("example", body.get("raw").toString());
                    }
                }
            } else if (bodyType != null && bodyType.equalsIgnoreCase("RAW")) {
                bodyInfo = new JSONObject();
                ((JSONObject) bodyInfo).put("type", "string");
                if (body != null && body.get("raw") != null) {
                    ((JSONObject) bodyInfo).put("example", body.get("raw").toString());
                }
            } else if (bodyType != null && bodyType.equalsIgnoreCase("XML")) {
                String xmlText = body.getString("raw");
                String xml = XMLUtils.delXmlHeader(xmlText);
                int startIndex = xml.indexOf("<", 0);
                int endIndex = xml.indexOf(">", 0);
                String substring = xml.substring(startIndex + 1, endIndex);
                JSONObject xmlToJson = XMLUtils.xmlConvertJson(xmlText);
                bodyInfo = buildRefSchema(substring);
                JSONObject jsonObject = buildRequestBodyXmlSchema(xmlToJson);
                if (schemas == null) {
                    schemas = new LinkedList<>();
                }
                schemas.add(jsonObject);
            } else if (bodyType != null && (bodyType.equalsIgnoreCase("WWW_FORM") || bodyType.equalsIgnoreCase("Form Data") || bodyType.equalsIgnoreCase("BINARY"))) {    //  key-value 类格式
                JSONObject formData = getformDataProperties(body.getJSONArray("kvs"));
                bodyInfo = buildFormDataSchema(formData);
            }
        }

        String type = null;
        if (respOrReq.getJSONObject("body") != null) {
            type = respOrReq.getJSONObject("body").getString("type");
        }
        JSONObject content = new JSONObject();
        Object schema = bodyInfo;   //  请求体部分
        JSONObject typeName = new JSONObject();
        if (schema != null) {
            typeName.put("schema", schema);//schema.getJSONObject("properties").size() == 0? "" :
        }
        if (StringUtils.isNotBlank(type)) {
            content.put(typeMap.get(type), typeName);
        }
        return content;
    }

    private Object buildRefSchema(String substring) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("$ref", "#/components/schemas/" + substring);
        return jsonObject;
    }

    private String getModulePath(List<String> tagTree, StringBuilder modulePath) {
        if (CollectionUtils.isEmpty(tagTree)) {
            return "/未规划接口";
        }
        for (String s : tagTree) {
            if (s.contains("/")) {
                String[] split = s.split("/");
                if (split.length > 0) {
                    getModulePath(List.of(split), modulePath);
                }
            } else {
                if (StringUtils.isNotBlank(s)) {
                    modulePath.append("/").append(s);
                }
            }
        }
        return modulePath.toString();
    }

}
