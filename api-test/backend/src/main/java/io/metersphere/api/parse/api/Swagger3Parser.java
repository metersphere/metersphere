package io.metersphere.api.parse.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.SwaggerApiExportResult;
import io.metersphere.api.parse.api.swagger.SwaggerApiInfo;
import io.metersphere.api.parse.api.swagger.SwaggerInfo;
import io.metersphere.api.parse.api.swagger.SwaggerParams;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.JsonSchemaItem;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.XMLUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
            MSException.throwException(Translator.get(CollectionUtils.isEmpty(auths) ? "swagger_parse_error" : "swagger_parse_error_with_auth"));
        }
        OpenAPI openAPI = result.getOpenAPI();
        if (result.getMessages() != null) {
            result.getMessages().forEach(msg -> LogUtil.error(msg)); // validation errors and warnings
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
        rests.add(new KeyValue(pathParameter.getName(), String.valueOf(pathParameter.getExample()), getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(), String.valueOf(cookieParameter.getExample()), getDefaultStringValue(cookieParameter.getDescription()), parameter.getRequired());
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(), String.valueOf(headerParameter.getExample()), getDefaultStringValue(headerParameter.getDescription()), "", parameter.getRequired());
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
        Map<String, Schema> infoMap = new HashMap();
        Schema schema = mediaType.getSchema();
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
            body.setJsonSchema(parseSchema(schema, refSet));
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
            if (modelByRef != null) propertiesResult = parseSchemaPropertiesToJson(modelByRef, refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        } else if (schema instanceof ArraySchema) {
            List<Object> jsonArray = new LinkedList<>();
            Schema items = ((ArraySchema) schema).getItems();
            parseSchemaToJson(items, refSet, infoMap);
            jsonArray.add(parseSchemaToJson(items, refSet, infoMap));
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
            return XMLUtil.jsonToXmlStr((JSONObject) data);
        } else {
            JSONObject object = new JSONObject();
            object.put(schema.getName(), schema.getExample());
            return XMLUtil.jsonToXmlStr(object);
        }
    }

    private Schema getModelByRef(String ref) {
        if (StringUtils.isBlank(ref)) {
            return null;
        }
        if (ref.split("/").length > 3) {
            ref = ref.replace("#/components/schemas/", "");
        }
        if (this.components.getSchemas() != null) return this.components.getSchemas().get(ref);
        return null;
    }

    private JsonSchemaItem parseSchema(Schema schema, Set<String> refSet) {
        if (schema == null) return null;
        if (StringUtils.isBlank(schema.get$ref()) && schema.getProperties() == null && refSet.isEmpty() && schema.getExample() == null) {
            return null;
        }
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
            item.setItems(new ArrayList<>());
            JsonSchemaItem arrayItem = parseSchema(items, refSet);
            if (arrayItem != null) item.getItems().add(arrayItem);
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
            return null;
        }
        if (schema.getExample() != null) {
            item.getMock().put(PropertyConstant.MOCK, schema.getExample());
        } else {
            item.getMock().put(PropertyConstant.MOCK, "");
        }

        item.setDescription(schema.getDescription());
        item.setPattern(schema.getPattern());
        item.setMaxLength(schema.getMaxLength());
        item.setMinLength(schema.getMinLength());

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
        } else if (value instanceof StringSchema || StringUtils.equals(PropertyConstant.STRING, value.getType())) {
            return example == null ? "" : example;
        } else {// todo 其他类型?
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), String.valueOf(queryParameter.getExample()), getDefaultStringValue(queryParameter.getDescription()), parameter.getRequired()));
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
                            PropertyConstant.REQUIRED:true	//	是否必填参数
                        }
                    ],
                    "requestBody":{
                        "content":{
                            "application/octet-stream":{    //  type
                                "schema":{
                                    PropertyConstant.TYPE:null,
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
        result.setInfo(new SwaggerInfo());
        result.setServers(new ArrayList<>());
        result.setTags(new ArrayList<>());
        result.setComponents(JSONUtil.createObj());
        result.setExternalDocs(JSONUtil.createObj());

        JSONObject paths = new JSONObject();
        for (ApiDefinitionWithBLOBs apiDefinition : apiDefinitionList) {
            SwaggerApiInfo swaggerApiInfo = new SwaggerApiInfo();   //  {tags:, summary:, description:, parameters:}
            swaggerApiInfo.setSummary(apiDefinition.getName());
            //  设置导入后的模块名 （根据 api 的 moduleID 查库获得所属模块，作为导出的模块名）
            /*ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
            String moduleName = "";
            if (apiDefinition.getModuleId() != null) {   //  module_id 可能为空
                ApiModuleDTO node = apiModuleService.getNode(apiDefinition.getModuleId());
                if (node != null) {
                    moduleName = node.getName();
                }
            }*/
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
            JSONObject requestBody = buildRequestBody(requestObject);

            swaggerApiInfo.setRequestBody(JSONUtil.parseObjectNode(requestBody.toString()));
            //  设置响应体
            JSONObject responseObject = JSONUtil.parseObject(apiDefinition.getResponse());
            JSONObject jsonObject = buildResponseBody(responseObject);

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
                for (int i = 0; i < params.length(); ++i) {
                    JSONObject param = params.optJSONObject(i); //  对于每个参数:
                    if (StringUtils.isEmpty(param.optString("name"))) {
                        continue;
                    }   //  否则无参数的情况，可能多出一行空行
                    SwaggerParams swaggerParam = new SwaggerParams();
                    swaggerParam.setIn(typeMap.get(type));  //  利用 map，根据 request 的 key 设置对应的参数类型
                    swaggerParam.setDescription(param.optString("description"));
                    swaggerParam.setName(param.optString("name"));
                    swaggerParam.setRequired( param.optBoolean(PropertyConstant.REQUIRED));
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

    private JSONObject buildRequestBody(JSONObject request) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("content", buildContent(request));
        return requestBody;
    }

    //  将请求体中的一个 json 对象转换成 swagger 格式的 json 对象返回
    private JSONObject buildRequestBodyJsonInfo(JSONObject requestBody) {
        if (requestBody == null) return null;
        JSONObject schema = new JSONObject();
        schema.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
        JSONObject properties = buildSchema(requestBody);
        schema.put(PropertyConstant.REQUIRED, properties);
        return schema;
    }

    //  请求体是 array 类型的情况
/* 例子："schema":{
        PropertyConstant.TYPE:PropertyConstant.ARRAY,
            PropertyConstant.ITEMS:{
            PropertyConstant.TYPE:PropertyConstant.OBJECT,
            PropertyConstant.REQUIRED:{
                "ids":{
                    PropertyConstant.TYPE:PropertyConstant.STRING
                }
            }
        }
    }   */
    private JSONObject buildRequestBodyJsonInfo(JSONArray requestBody) {
        if (requestBody == null) return null;
        JSONObject schema = new JSONObject();
        schema.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
        JSONObject items = new JSONObject();

        if (requestBody.length() > 0) {
            Object example = requestBody.get(0);
            if (example instanceof JSONObject) {
                items.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                items.put(PropertyConstant.REQUIRED, buildSchema((JSONObject) example));
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
                JSONObject items = requestBody.optJSONObject(PropertyConstant.ITEMS);
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.ARRAY);
                JSONObject item = buildJsonSchema(items, required);
                if (StringUtils.isNotBlank(requestBody.optString("description"))) {
                    parsedParam.put("description", requestBody.optString("description"));
                }
                parsedParam.put(PropertyConstant.ITEMS, item);
            } else if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.OBJECT);
                JSONObject properties = requestBody.optJSONObject(PropertyConstant.REQUIRED);
                JSONObject jsonObject = buildFormDataSchema(properties);
                if (StringUtils.isNotBlank(requestBody.optString("description"))) {
                    parsedParam.put("description", requestBody.optString("description"));
                }
                parsedParam.put(PropertyConstant.REQUIRED, jsonObject.optJSONObject(PropertyConstant.REQUIRED));
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
            Object value = mock.get(PropertyConstant.MOCK);
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
            if (param instanceof String) {
                parsedParam.put(PropertyConstant.TYPE, PropertyConstant.STRING);
                parsedParam.put("example", param == null ? "" : param);
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
                if (mock != null) {
                    Object mockValue = mock.get(PropertyConstant.MOCK);
                    property.put("example", mockValue);
                } else {
                    property.put("example", value);
                }
            } else {
                property.put("example", value);
            }
            property.put("description", obj.optString("description"));
            property.put(PropertyConstant.REQUIRED, obj.optString(PropertyConstant.REQUIRED));
            if (obj.optJSONObject(PropertyConstant.REQUIRED) != null) {
                JSONObject properties1 = buildFormDataSchema(obj.optJSONObject(PropertyConstant.REQUIRED));
                property.put(PropertyConstant.REQUIRED, properties1.optJSONObject(PropertyConstant.REQUIRED));
            }
            properties.put(key, property);
        }
        schema.put(PropertyConstant.REQUIRED, properties);
        return schema;
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
    private JSONObject buildResponseBody(JSONObject response) {
        if (response == null) {
            return new JSONObject();
        }
        JSONObject responseBody = new JSONObject();
        JSONObject statusCodeInfo = new JSONObject();
        //  build 请求头
        JSONObject headers = new JSONObject();
        JSONArray headValueList = response.getJSONArray("headers");
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
        statusCodeInfo.put("headers", headers);

        statusCodeInfo.put("content", buildContent(response));
        statusCodeInfo.put("description", "");
        // 返回code
        JSONArray statusCode = response.getJSONArray("statusCode");
        responseBody.put(statusCode.toString(), statusCodeInfo);
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
    private JSONObject buildContent(JSONObject respOrReq) {
        Hashtable<String, String> typeMap = new Hashtable<String, String>() {{
            put("XML", org.springframework.http.MediaType.APPLICATION_XML_VALUE);
            put("JSON", org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            put("Raw", "application/urlencoded");
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
                                required = jsonObject.getJSONArray(PropertyConstant.REQUIRED);
                            }
                            if (required == null) {
                                JSONObject items = jsonObject.optJSONObject(PropertyConstant.ITEMS);
                                if (items != null) {
                                    required = items.getJSONArray(PropertyConstant.REQUIRED);
                                }
                            }
                            bodyInfo = buildJsonSchema(jsonObject, required);
                        }
                    } else {
                        try {    //  若请求体是一个 object
                            bodyInfo = buildRequestBodyJsonInfo(body.getJSONArray("raw"));
                        } catch (Exception e) {
                            bodyInfo = buildRequestBodyJsonInfo(body.optJSONObject("raw"));
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
                JSONObject xmlToJson = XMLUtil.XmlToJson(xmlText);
                bodyInfo = buildRequestBodyJsonInfo(xmlToJson);
            } else if (bodyType != null && (bodyType.equalsIgnoreCase("WWW_FORM") || bodyType.equalsIgnoreCase("Form Data") || bodyType.equalsIgnoreCase("BINARY"))) {    //  key-value 类格式
                JSONObject formData = getformDataProperties(body.getJSONArray("kvs"));
                bodyInfo = buildFormDataSchema(formData);
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
