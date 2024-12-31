package io.metersphere.api.parse.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.SwaggerApiExportResult;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.JsonSchemaItem;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.parse.api.swagger.SwaggerApiInfo;
import io.metersphere.api.parse.api.swagger.SwaggerInfo;
import io.metersphere.api.parse.api.swagger.SwaggerParams;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.constants.SwaggerParameterType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtil;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Swagger3Parser extends SwaggerAbstractParser {

    private Components components;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String sourceStr = StringUtils.EMPTY;
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
            MSException.throwException(Translator.get(CollectionUtils.isEmpty(auths) ? "swagger_parse_error" : "swagger_parse_error_with_auth"));
        }
        OpenAPI openAPI = result.getOpenAPI();
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(openAPI, request));
        return definitionImport;
    }


    // 鉴权设置
    private List<AuthorizationValue> setAuths(ApiTestImportRequest request) {
        List<AuthorizationValue> auths = new ArrayList<>();
        // 如果有 BaseAuth 参数，base64 编码后转换成 headers
        if (request.getAuthManager() != null && StringUtils.isNotBlank(request.getAuthManager().getUsername()) && StringUtils.isNotBlank(request.getAuthManager().getPassword()) && MsAuthManager.mechanismMap.containsKey(request.getAuthManager().getVerification())) {
            AuthorizationValue authorizationValue = new AuthorizationValue();
            authorizationValue.setType("header");
            authorizationValue.setKeyName("Authorization");
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((request.getAuthManager().getUsername() + ":" + request.getAuthManager().getPassword()).getBytes());
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
                    authorizationValue.setUrlMatcher((url) -> true);
                    auths.add(authorizationValue);
                }
            }
        }
        // 设置 query 参数
        if (!CollectionUtils.isEmpty(request.getArguments())) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(request.getSwaggerUrl());
            if (StringUtils.isNotBlank(request.getSwaggerUrl()) && !request.getSwaggerUrl().contains("?")) {
                pathBuilder.append("?");
            }
            for (KeyValue keyValue : request.getArguments()) {
                if (StringUtils.isNotBlank(keyValue.getName())) {
                    AuthorizationValue authorizationValue = new AuthorizationValue();
                    authorizationValue.setType("query");
                    authorizationValue.setKeyName(keyValue.getName());
                    try {
                        authorizationValue.setValue(URLEncoder.encode(keyValue.getValue(), StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        LogUtil.info("swagger3 url encode error: " + e);
                    }
                    pathBuilder.append(keyValue.getName()).append("=").append(authorizationValue.getValue()).append("&");
                }
            }
            request.setSwaggerUrl(pathBuilder.substring(0, pathBuilder.length() - 1));
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
                    parseParameters(pathItem, request);
                    parseRequestBody(operation.getRequestBody(), request.getBody());
                    addBodyHeader(request);
                    if (request.getBody().getKvs().size() > 1 && request.getBody().getKvs().get(0).getName() == null) {
                        request.getBody().getKvs().remove(0);
                    }   //  有数据的话，去掉 Kvs 里初始化的第一个全 null 的数据，否则有空行
                    apiDefinition.setRequest(JSON.toJSONString(request));
                    apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation.getResponses())));
                    //buildModule(selectModule, apiDefinition, operation.getTags(), selectModulePath);
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
        String name = StringUtils.EMPTY;
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

    private void parseParameters(PathItem path, MsHTTPSamplerProxy request) {
        if (path.getParameters() == null) {
            return;
        }
        List<Parameter> parameters = path.getParameters();
        // 处理特殊格式  rest参数是和请求平级的情况

        for (Parameter parameter : parameters) {
            if (StringUtils.isNotBlank(parameter.getIn())) {
                switch (parameter.getIn()) {
                    case SwaggerParameterType.PATH -> parsePathParameters(parameter, request.getRest());
                    case SwaggerParameterType.QUERY -> parseQueryParameters(parameter, request.getArguments());
                    case SwaggerParameterType.HEADER -> parseHeaderParameters(parameter, request.getHeaders());
                    case SwaggerParameterType.COOKIE -> parseCookieParameters(parameter, request.getHeaders());
                }
            }
        }
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(),
                pathParameter.getExample() != null ? String.valueOf(pathParameter.getExample()) : null,
                getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? StringUtils.EMPTY : val;
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
        msResponse.setType(RequestTypeConstants.HTTP);
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
        //        body.setRaw(response.getDescription());
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
        Map<String, Schema> infoMap = new HashMap();
        Schema schema = getSchema(mediaType.getSchema());
        if (content.get(contentType) != null && content.get(contentType).getExample() != null && schema.getExample() == null) {
            schema.setExample(content.get(contentType).getExample());
        }
        Object bodyData = null;
        if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
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
            try {
                body.setRaw(parseXmlBody(schema, bodyData));
            } catch (Exception e) {
                body.setRaw(e.getMessage());
            }
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else {
            body.setRaw(schema.getExample().toString());
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
            if (modelByRef != null) propertiesResult = parseSchemaPropertiesToJson(modelByRef, refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        } else if (schema instanceof ArraySchema) {
            List<Object> jsonArray = new LinkedList<>();
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
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(data), Map.class);
                map.forEach((k, v) -> {
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
        KeyValue kv = new KeyValue(name, String.valueOf(schema.getExample() == null ? StringUtils.EMPTY : schema.getExample()), schema.getDescription());
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
                        return XMLUtil.jsonToPrettyXml(object);
                    }
                }
            }
            return XMLUtil.jsonToPrettyXml((JSONObject) data);
        } else {
            JSONObject object = new JSONObject();
            if (StringUtils.isNotBlank(schema.getName())) {
                object.put(schema.getName(), schema.getExample());
            }
            return XMLUtil.jsonToPrettyXml(object);
        }
    }

    private Schema getModelByRef(String ref) {
        if (StringUtils.isBlank(ref)) {
            return null;
        }
        if (ref.split("/").length > 3) {
            ref = ref.replace("#/components/schemas/", StringUtils.EMPTY);
        }
        if (this.components.getSchemas() != null) return this.components.getSchemas().get(ref);
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
            item.setType(PropertyConstant.OBJECT);
            refSet.add(schema.get$ref());
            Schema modelByRef = getModelByRef(schema.get$ref());
            if (modelByRef != null) {
                item.setProperties(parseSchemaProperties(modelByRef, refSet));
                item.setRequired(modelByRef.getRequired());
            }
        } else if (schema instanceof ArraySchema) {
            Schema items = ((ArraySchema) schema).getItems();
            item.setType(PropertyConstant.ARRAY);
            JsonSchemaItem arrayItem = parseSchema(items, refSet);
            Map<String, Object> mock = new LinkedHashMap<>();
            if (arrayItem != null && MapUtils.isNotEmpty(arrayItem.getProperties())) {
                arrayItem.getProperties().forEach((k, v) -> {
                    mock.put(k, StringUtils.isBlank(v.getMock().get(PropertyConstant.MOCK).toString()) ? v.getType() :
                            v.getMock().get(PropertyConstant.MOCK).toString());
                });
            }
            if (item.getMock() != null) {
                item.getMock().put(PropertyConstant.MOCK, JSONUtil.toJSONString(mock));
            } else {
                item.setMock(mock);
            }
            if (arrayItem != null) {
                item.getItems().add(arrayItem);
            }
        } else if (schema instanceof ObjectSchema) {
            item.setType(PropertyConstant.OBJECT);
            item.setProperties(parseSchemaProperties(schema, refSet));
        } else if (schema instanceof StringSchema) {
            item.setType(PropertyConstant.STRING);
        } else if (schema instanceof IntegerSchema) {
            item.setType(PropertyConstant.INTEGER);
        } else if (schema instanceof NumberSchema) {
            item.setType(PropertyConstant.NUMBER);
        } else if (schema instanceof BooleanSchema) {
            item.setType(PropertyConstant.BOOLEAN);
        } else {
            item.setType("null");
        }
        if (schema.getExample() != null) {
            item.getMock().put(PropertyConstant.MOCK, schema.getExample());
        } else if (StringUtils.isNotBlank(item.getMock().get(PropertyConstant.MOCK).toString())) {
            item.getMock().put(PropertyConstant.MOCK, item.getMock().get(PropertyConstant.MOCK));
        } else {
            item.getMock().put(PropertyConstant.MOCK, StringUtils.EMPTY);
        }
        item.setDescription(schema.getDescription());
        item.setPattern(schema.getPattern());
        item.setMaxLength(schema.getMaxLength());
        item.setMinLength(schema.getMinLength());
        item.setMaximum(schema.getMaximum());
        item.setMinimum(schema.getMinimum());
        item.setDefaultValue(schema.getDefault());
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
        } else if (value instanceof StringSchema || StringUtils.equals(PropertyConstant.STRING, value.getType()) || value instanceof JsonSchema) {
            if (example == null) {
                if (value.getXml() == null) {
                    return StringUtils.EMPTY;
                } else {
                    XML xml = value.getXml();
                    JSONObject jsonObject = new JSONObject();
                    if (xml.getWrapped() != null && xml.getWrapped()) {
                        jsonObject.put(xml.getName(), PropertyConstant.OBJECT);
                    } else {
                        jsonObject.put(xml.getName(), PropertyConstant.STRING);
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
        if (ObjectUtils.isNotEmpty(jsonSchemaItem)) {
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

        if (schema != null && StringUtils.isBlank(schema.get$ref()) && StringUtils.isNotBlank(schema.getType()) && StringUtils.equals(schema.getType(), "string")) {
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
                return String.valueOf(jsonSchemaItem.getMock().get(PropertyConstant.MOCK));
            }
            if (jsonSchemaItem != null && jsonSchemaItem.getDefaultValue() != null) {
                return String.valueOf(jsonSchemaItem.getDefaultValue());
            }
            return null;
        }
    }

    public SwaggerApiExportResult swagger3Export(List<ApiDefinitionWithBLOBs> apiDefinitionList, Project project) {
        SwaggerApiExportResult result = new SwaggerApiExportResult();

        result.setOpenapi("3.0.1");
        SwaggerInfo swaggerInfo = new SwaggerInfo();
        swaggerInfo.setVersion("1.0");
        swaggerInfo.setTitle("ms-" + project.getName());
        swaggerInfo.setDescription(StringUtils.EMPTY);
        swaggerInfo.setTermsOfService(StringUtils.EMPTY);
        result.setInfo(swaggerInfo);
        result.setServers(new ArrayList<>());
        result.setTags(new ArrayList<>());
        result.setComponents(JSONUtil.createObj());
        result.setExternalDocs(JSONUtil.createObj());

        JSONObject paths = new JSONObject();
        JSONObject components = new JSONObject();
        List<JSONObject> schemas = new LinkedList<>();
        for (ApiDefinitionWithBLOBs apiDefinition : apiDefinitionList) {
            SwaggerApiInfo swaggerApiInfo = new SwaggerApiInfo();   //  {tags:, summary:, description:, parameters:}
            swaggerApiInfo.setSummary(apiDefinition.getName());
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
            JSONObject requestObject = JSONUtil.parseObject(apiDefinition.getRequest());    //  将api的request属性转换成JSON对象以便获得参数
            JSONObject requestBody = buildRequestBody(requestObject, schemas);

            swaggerApiInfo.setRequestBody(JSONUtil.parseObjectNode(requestBody.toString()));
            JSONObject responseObject = new JSONObject();
            try {
                //  设置响应体
                responseObject = JSONUtil.parseObject(apiDefinition.getResponse());
            } catch (Exception e) {
                responseObject = new JSONObject(new ApiResponse());
            }
            JSONObject jsonObject = buildResponseBody(responseObject, schemas);
            swaggerApiInfo.setResponses(JSONUtil.parseObjectNode(jsonObject.toString()));
            //  设置请求参数列表
            List<JSONObject> paramsList = buildParameters(requestObject);
            List<JsonNode> nodes = new LinkedList<>();
            paramsList.forEach(item -> {
                nodes.add(JSONUtil.parseObjectNode(item.toString()));
            });
            swaggerApiInfo.setParameters(nodes);
            swaggerApiInfo.setDescription(apiDefinition.getDescription());
            JSONObject methodDetail = JSONUtil.parseObject(JSON.toJSONString(swaggerApiInfo));
            if (paths.optJSONObject(apiDefinition.getPath()) == null) {
                paths.put(apiDefinition.getPath(), new JSONObject());
            }   //  一个路径下有多个发方法，如post，get，因此是一个 JSONObject 类型
            paths.optJSONObject(apiDefinition.getPath()).put(apiDefinition.getMethod().toLowerCase(), methodDetail);
        }
        result.setPaths(JSONUtil.parseObjectNode(paths.toString()));
        if (CollectionUtils.isNotEmpty(schemas)) {
            components.put("schemas", schemas.get(0));
        }
        result.setComponents(JSONUtil.parseObjectNode(components.toString()));
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
            JSONArray params = request.optJSONArray(type);  //  获得请求参数列表
            if (params != null) {
                for (int i = 0; i < params.length(); ++i) {
                    JSONObject param = params.optJSONObject(i); //  对于每个参数:
                    if (StringUtils.isEmpty(param.optString("name"))) {
                        continue;
                    }   //  否则无参数的情况，可能多出一行空行
                    SwaggerParams swaggerParam = new SwaggerParams();
                    swaggerParam.setIn(typeMap.get(type));  //  利用 map，根据 request 的 key 设置对应的参数类型
                    swaggerParam.setDescription(param.optString("description"));
                    swaggerParam.setName(param.optString("name"));
                    swaggerParam.setRequired(param.optBoolean(PropertyConstant.REQUIRED));
                    swaggerParam.setExample(param.optString("value"));
                    JSONObject schema = new JSONObject();
                    schema.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                    swaggerParam.setSchema(JSONUtil.parseObjectNode(schema.toString()));
                    paramsList.add(JSONUtil.parseObject(JSON.toJSONString(swaggerParam)));
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
        if (requestBody == null) return null;
        JSONObject schema = new JSONObject();
        schema.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
        JSONObject properties = buildSchema(requestBody);
        schema.put(PropertyConstant.PROPERTIES, properties);
        return schema;
    }

    private JSONObject buildRequestBodyJsonInfo(JSONArray requestBody) {
        if (requestBody == null) return null;
        JSONObject schema = new JSONObject();
        schema.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
        JSONObject items = new JSONObject();

        if (requestBody.length() > 0) {
            Object example = requestBody.get(0);
            if (example instanceof JSONObject) {
                items.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                items.put(PropertyConstant.PROPERTIES, buildSchema((JSONObject) example));
            } else if (example instanceof String) {
                items.put(PropertyConstant.TYPE, PropertyConstant.STRING);
            } else if (example instanceof Integer) {
                items.put(PropertyConstant.TYPE, PropertyConstant.INTEGER);
                items.put("format", "int64");
            } else if (example instanceof Boolean) {
                items.put(PropertyConstant.TYPE, PropertyConstant.BOOLEAN);
            } else if (example instanceof java.math.BigDecimal) {
                items.put(PropertyConstant.TYPE, "double");
            } else {    //  JSONOArray
                items.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
                JSONObject item = new JSONObject();
                if (((JSONArray) example).length() > 0) {
                    if (((JSONArray) example).get(0) instanceof JSONObject) {
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) example).get(0));
                    }
                }
                items.put(PropertyConstant.ITEMS, item);
            }
        }
        schema.put(PropertyConstant.ITEMS, items);
        return schema;
    }

    private JSONObject buildJsonSchema(JSONObject requestBody, JSONArray required) {
        String type = requestBody.optString(PropertyConstant.TYPE);

        JSONObject parsedParam = new JSONObject();
        if (required != null) {
            parsedParam.put(PropertyConstant.REQUIRED, required);
        }
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.equals(type, PropertyConstant.ARRAY)) {
                JSONArray items = requestBody.optJSONArray(PropertyConstant.ITEMS);
                JSONObject itemProperties = new JSONObject();
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
                if (items != null) {
                    JSONObject itemsObject = new JSONObject();
                    if (items.length() > 0) {
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
                    itemsObject.put(PropertyConstant.PROPERTIES, itemProperties);
                    parsedParam.put(PropertyConstant.ITEMS, itemsObject.optJSONObject(PropertyConstant.PROPERTIES));
                } else {
                    parsedParam.put(PropertyConstant.ITEMS, new JSONObject());
                }
                if (StringUtils.isNotBlank(requestBody.optString("description"))) {
                    parsedParam.put("description", requestBody.optString("description"));
                }
            } else if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                if (requestBody.optJSONArray(PropertyConstant.REQUIRED) != null) {
                    parsedParam.put(PropertyConstant.REQUIRED, requestBody.optJSONArray(PropertyConstant.REQUIRED));
                }
                JSONObject properties = requestBody.optJSONObject(PropertyConstant.PROPERTIES);
                if (properties != null) {
                    JSONObject jsonObject = buildFormDataSchema(properties);
                    if (StringUtils.isNotBlank(requestBody.optString("description"))) {
                        parsedParam.put("description", requestBody.optString("description"));
                    }
                    parsedParam.put(PropertyConstant.PROPERTIES, jsonObject.optJSONObject(PropertyConstant.PROPERTIES));
                }
            } else if (StringUtils.equals(type, PropertyConstant.INTEGER)) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.INTEGER);
                parsedParam.put("format", "int64");
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else if (StringUtils.equals(type, PropertyConstant.BOOLEAN)) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.BOOLEAN);
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else if (StringUtils.equals(type, PropertyConstant.NUMBER)) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.NUMBER);
                setCommonJsonSchemaParam(parsedParam, requestBody);
            } else {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                setCommonJsonSchemaParam(parsedParam, requestBody);
            }
        }
        return parsedParam;
    }

    public void setCommonJsonSchemaParam(JSONObject parsedParam, JSONObject requestBody) {
        if (StringUtils.isNotBlank(requestBody.optString("description"))) {
            parsedParam.put("description", requestBody.optString("description"));
        }
        Object jsonSchemaValue = getJsonSchemaValue(requestBody);
        if (jsonSchemaValue != null) {
            parsedParam.put("example", jsonSchemaValue);
        }
    }

    public Object getJsonSchemaValue(JSONObject item) {
        JSONObject mock = item.optJSONObject(PropertyConstant.MOCK);
        if (mock != null) {
            if (StringUtils.isNotBlank(mock.optString("mock"))) {
                Object value = mock.get(PropertyConstant.MOCK);
                return value;
            }
        }
        return null;
    }

    //  设置一个 json 对象的属性在 swagger 格式中的类型、值
    private JSONObject buildSchema(JSONObject requestBody) {
        JSONObject schema = new JSONObject();
        for (String key : requestBody.keySet()) {
            Object param = requestBody.get(key);
            JSONObject parsedParam = new JSONObject();
            if (param instanceof String) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                parsedParam.put("example", param == null ? StringUtils.EMPTY : param);
            } else if (param instanceof Integer) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.INTEGER);
                parsedParam.put("format", "int64");
                parsedParam.put("example", param);
            } else if (param instanceof JSONObject) {
                parsedParam = buildRequestBodyJsonInfo((JSONObject) param);
            } else if (param instanceof Boolean) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.BOOLEAN);
                parsedParam.put("example", param);
            } else if (param instanceof java.math.BigDecimal) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put(PropertyConstant.TYPE, "double");
                parsedParam.put("example", param);
            } else {    //  JSONOArray
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
                JSONObject item = new JSONObject();
                if (param == null) {
                    param = new JSONArray();
                }
                if (((JSONArray) param).length() > 0) {
                    if (((JSONArray) param).get(0) instanceof JSONObject) {  ///
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) param).get(0));
                    }
                }
                parsedParam.put(PropertyConstant.ITEMS, item);
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
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                parsedParam.put("example", param == null ? StringUtils.EMPTY : param);
            } else if (param instanceof Integer) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.INTEGER);
                parsedParam.put("format", "int64");
                parsedParam.put("example", param);
            } else if (param instanceof JSONObject) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                Object attribute = ((JSONObject) param).opt("attribute");
                //build properties
                JSONObject paramObject = buildRequestBodyXmlSchema((JSONObject) param);
                if (attribute != null && attribute instanceof JSONArray) {
                    JSONObject jsonObject = buildXmlProperties(((JSONArray) attribute).getJSONObject(0));
                    paramObject.remove("attribute");
                    for (String paramKey : paramObject.keySet()) {
                        Object paramChild = paramObject.get(paramKey);
                        if (paramChild instanceof String) {
                            JSONObject one = new JSONObject();
                            one.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                            one.put("properties", jsonObject);
                            paramObject.remove("example");
                            paramObject.remove(paramKey);
                            paramObject.put(paramKey, one);
                        }
                        if (paramChild instanceof JSONObject) {
                            Object properties = ((JSONObject) paramChild).opt("properties");
                            if (properties != null) {
                                for (String aa : jsonObject.keySet()) {
                                    Object value = jsonObject.get(aa);
                                    if (((JSONObject) properties).opt(aa) == null) {
                                        ((JSONObject) properties).put(aa, value);
                                    }
                                }
                            } else {
                                ((JSONObject) paramChild).put("properties", jsonObject);
                            }
                            if (((JSONObject) paramChild).opt("type") == "string") {
                                ((JSONObject) paramChild).put("type", "object");
                                ((JSONObject) paramChild).remove("example");
                            }
                        }
                    }
                }
                parsedParam.put("properties", paramObject);
                if (StringUtils.isNotBlank(requestBody.optString("description"))) {
                    parsedParam.put("description", requestBody.optString("description"));
                }
            } else if (param instanceof Boolean) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.BOOLEAN);
                parsedParam.put("example", param);
            } else if (param instanceof java.math.BigDecimal) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put(PropertyConstant.TYPE, "double");
                parsedParam.put("example", param);
            } else {    //  JSONOArray
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);

                if (param == null) {
                    param = new JSONArray();
                }
                JSONObject jsonObjects = new JSONObject();
                if (((JSONArray) param).length() > 0) {
                    ((JSONArray) param).forEach(t -> {
                        JSONObject item = buildRequestBodyXmlSchema((JSONObject) t);
                        for (String s : item.keySet()) {
                            jsonObjects.put(s, item.get(s));
                        }
                    });
                }
                parsedParam.put(PropertyConstant.PROPERTIES, jsonObjects);
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
            property.put(PropertyConstant.TYPE, StringUtils.isNotEmpty(obj.optString(PropertyConstant.TYPE)) ? obj.optString(PropertyConstant.TYPE) : PropertyConstant.STRING);
            String value = obj.optString("value");
            if (StringUtils.isBlank(value)) {
                JSONObject mock = obj.optJSONObject(PropertyConstant.MOCK);
                if (mock != null && StringUtils.isNotBlank(mock.optString("mock"))) {
                    Object mockValue = mock.get(PropertyConstant.MOCK);
                    property.put("example", mockValue);
                } else {
                    property.put("example", value);
                }
            } else {
                property.put("example", value);
            }
            property.put("description", obj.optString("description"));
            property.put(PropertyConstant.REQUIRED, obj.optJSONArray(PropertyConstant.REQUIRED));
            if (obj.optJSONObject(PropertyConstant.PROPERTIES) != null) {
                JSONObject childProperties = buildFormDataSchema(obj.optJSONObject(PropertyConstant.PROPERTIES));
                property.put(PropertyConstant.PROPERTIES, childProperties.optJSONObject(PropertyConstant.PROPERTIES));
            } else {
                JSONObject childProperties = buildJsonSchema(obj, new JSONArray());
                if (StringUtils.equalsIgnoreCase(obj.optString(PropertyConstant.TYPE), PropertyConstant.ARRAY)) {
                    if (childProperties.optJSONObject(PropertyConstant.ITEMS) != null) {
                        property.put(PropertyConstant.ITEMS, childProperties.optJSONObject(PropertyConstant.ITEMS));
                    }
                } else {
                    if (childProperties.optJSONObject(PropertyConstant.PROPERTIES) != null) {
                        property.put(PropertyConstant.PROPERTIES, childProperties.optJSONObject(PropertyConstant.PROPERTIES));
                    }
                }
            }
            properties.put(key, property);
        }
        schema.put(PropertyConstant.PROPERTIES, properties);
        return schema;
    }

    private static JSONObject buildXmlProperties(JSONObject kvs) {
        JSONObject properties = new JSONObject();
        for (String key : kvs.keySet()) {
            JSONObject property = new JSONObject();
            Object param = kvs.opt(key);
            if (param instanceof String) {
                property.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                property.put("example", param == null ? StringUtils.EMPTY : param);
            }
            if (param instanceof JSONObject) {
                JSONObject obj = ((JSONObject) param);
                property.put(PropertyConstant.TYPE, StringUtils.isNotEmpty(obj.optString(PropertyConstant.TYPE)) ? obj.optString(PropertyConstant.TYPE) : PropertyConstant.STRING);
                String value = obj.optString("value");
                if (StringUtils.isBlank(value)) {
                    JSONObject mock = obj.optJSONObject(PropertyConstant.MOCK);
                    if (mock != null) {
                        Object mockValue = mock.get(PropertyConstant.MOCK);
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
                String name = ((JSONObject) item).optString("name");
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
                    PropertyConstant.TYPE:PropertyConstant.STRING
                }
            }
        }
    */
    private JSONObject buildResponseBody(JSONObject response, List<JSONObject> schemas) {
        if (response == null) {
            return new JSONObject();
        }
        JSONObject responseBody = new JSONObject();

        //  build 请求头
        JSONObject headers = new JSONObject();
        JSONArray headValueList = response.optJSONArray("headers");
        if (headValueList != null) {
            for (Object item : headValueList) {
                if (item instanceof JSONObject && ((JSONObject) item).optString("name") != null) {
                    JSONObject head = new JSONObject(), headSchema = new JSONObject();
                    head.put("description", ((JSONObject) item).optString("description"));
                    head.put("example", ((JSONObject) item).optString("value"));
                    headSchema.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                    head.put("schema", headSchema);
                    headers.put(((JSONObject) item).optString("name"), head);
                }
            }
        }

        JSONObject statusCodeInfo = new JSONObject();
        statusCodeInfo.put("headers", headers);
        statusCodeInfo.put("content", buildContent(response, schemas));
        statusCodeInfo.put("description", StringUtils.EMPTY);

        // 返回code
        JSONArray statusCode = response.optJSONArray("statusCode");
        if (statusCode != null && statusCode.length() > 0) {
            JSONObject jsonObject = statusCode.getJSONObject(0);
            if (StringUtils.isNotBlank(jsonObject.optString("value"))) {
                statusCodeInfo.put("description", jsonObject.optString("value"));
            }
            if (StringUtils.isNotBlank(jsonObject.optString("name"))) {
                responseBody.put(jsonObject.optString("name"), statusCodeInfo);
            } else {
                responseBody.put("200", statusCodeInfo);
            }
        } else {
            responseBody.put("200", statusCodeInfo);
        }
        return responseBody;
    }

    /*  请求体格式：
        "content":{
            "application/json":{
                "schema":{
                    PropertyConstant.TYPE:"xxx",
                    "xxx":{...}
                }
            }
        }
    */
    private JSONObject buildContent(JSONObject respOrReq, List<JSONObject> schemas) {
        Hashtable<String, String> typeMap = new Hashtable<String, String>() {{
            put("XML", org.springframework.http.MediaType.APPLICATION_XML_VALUE);
            put("JSON", org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            put("Raw", "text/plain");
            put("BINARY", org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
            put("Form Data", org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
            put("WWW_FORM", org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }};
        Object bodyInfo = new Object();
        JSONObject body = respOrReq.optJSONObject("body");

        if (body != null) { //  将请求体转换成相应的格式导出
            String bodyType = body.optString(PropertyConstant.TYPE);
            if (StringUtils.isNotBlank(bodyType) && bodyType.equalsIgnoreCase("JSON")) {
                try {
                    if (StringUtils.equals(body.optString("format"), "JSON-SCHEMA")) {
                        String jsonSchema = body.optString("jsonSchema");
                        if (StringUtils.isNotBlank(jsonSchema)) {
                            JSONObject jsonObject = JSONUtil.parseObject(jsonSchema);
                            JSONArray required = new JSONArray();
                            if (jsonObject != null) {
                                required = jsonObject.optJSONArray(PropertyConstant.REQUIRED);
                            }
                            if (required == null) {
                                JSONArray items = jsonObject.optJSONArray(PropertyConstant.ITEMS);
                                if (items != null && items.length() > 0) {
                                    JSONArray finalRequired = new JSONArray();
                                    items.forEach(item -> {
                                        if (item instanceof JSONObject) {
                                            JSONObject itemRequired = ((JSONObject) item).optJSONObject(PropertyConstant.REQUIRED);
                                            if (itemRequired != null) {
                                                finalRequired.put(itemRequired);
                                            }
                                            finalRequired.putAll(((JSONObject) item).optJSONArray(PropertyConstant.REQUIRED));
                                        }
                                    });
                                    required = finalRequired;
                                }
                            }
                            bodyInfo = buildJsonSchema(jsonObject, required);
                        }
                    } else {
                        String raw = body.optString("raw");
                        if (StringUtils.isNotBlank(raw)) {
                            if (StringUtils.startsWith(raw, "[") && StringUtils.endsWith(raw, "]")) {
                                bodyInfo = buildRequestBodyJsonInfo(JSONUtil.parseArray(raw));
                            } else {
                                bodyInfo = buildRequestBodyJsonInfo(JSONUtil.parseObject(raw));
                            }
                        }
                    }
                } catch (Exception e1) {    //  若请求体 json 不合法，则忽略错误，原样字符串导出/导入
                    bodyInfo = new JSONObject();
                    ((JSONObject) bodyInfo).put(PropertyConstant.TYPE, PropertyConstant.STRING);
                    if (body != null && body.optString("raw") != null) {
                        ((JSONObject) bodyInfo).put("example", body.optString("raw"));
                    }
                }
            } else if (bodyType != null && bodyType.equalsIgnoreCase("RAW")) {
                bodyInfo = new JSONObject();
                ((JSONObject) bodyInfo).put(PropertyConstant.TYPE, PropertyConstant.STRING);
                if (body != null && body.optString("raw") != null) {
                    ((JSONObject) bodyInfo).put("example", body.optString("raw"));
                }
            } else if (bodyType != null && bodyType.equalsIgnoreCase("XML")) {
                String xmlText = body.optString("raw");
                String xml = XMLUtil.delXmlHeader(xmlText);
                int startIndex = xml.indexOf("<", 0);
                int endIndex = xml.indexOf(">", 0);
                if (endIndex > startIndex + 1) {
                    String substring = xml.substring(startIndex + 1, endIndex);
                    bodyInfo = buildRefSchema(substring);
                }
                JSONObject xmlToJson = XMLUtil.xmlConvertJson(xmlText);
                JSONObject jsonObject = buildRequestBodyXmlSchema(xmlToJson);
                if (schemas == null) {
                    schemas = new LinkedList<>();
                }
                schemas.add(jsonObject);
            } else if (bodyType != null && (bodyType.equalsIgnoreCase("WWW_FORM") || bodyType.equalsIgnoreCase("Form Data") || bodyType.equalsIgnoreCase("BINARY"))) {    //  key-value 类格式
                if (body.optJSONArray("kvs") != null) {
                    JSONObject formData = getformDataProperties(body.optJSONArray("kvs"));
                    bodyInfo = buildFormDataSchema(formData);
                }
            }
        }

        String type = null;
        if (respOrReq.optJSONObject("body") != null) {
            type = respOrReq.optJSONObject("body").optString(PropertyConstant.TYPE);
        }
        JSONObject content = new JSONObject();
        Object schema = bodyInfo;   //  请求体部分
        JSONObject typeName = new JSONObject();
        if (schema != null) {
            typeName.put("schema", schema);//schema.optJSONObject(PropertyConstant.REQUIRED).size() == 0? "" :
        }
        if (StringUtils.isNotBlank(type) && typeMap.containsKey(type)) {
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
