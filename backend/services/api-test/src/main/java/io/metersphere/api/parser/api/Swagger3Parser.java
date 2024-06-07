package io.metersphere.api.parser.api;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.converter.ApiDefinitionImport;
import io.metersphere.api.dto.converter.ApiDefinitionImportDetail;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.JsonSchemaBuilder;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.project.dto.environment.auth.NoAuth;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.*;


public class Swagger3Parser<T> extends ApiImportAbstractParser<ApiDefinitionImport> {

    protected String projectId;
    private Components components;

    public static final String PATH = "path";
    public static final String HEADER = "header";
    public static final String COOKIE = "cookie";
    public static final String QUERY = "query";

    public ApiDefinitionImport parse(InputStream source, ImportRequest request) throws Exception {
        LogUtils.info("Swagger3Parser parse");
        List<AuthorizationValue> auths = setAuths(request);
        SwaggerParseResult result = null;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            result = new OpenAPIParser().readLocation(request.getSwaggerUrl(), auths, null);
            if (result == null || result.getOpenAPI() == null || !result.getOpenAPI().getOpenapi().startsWith("3.0") || result.isOpenapi31()) {
                throw new MSException(Translator.get("swagger_parse_error_with_auth"));
            }
        } else {
            String apiTestStr = getApiTestStr(source);
            Map<String, Object> o = ApiDataUtils.parseObject(apiTestStr, Map.class);
            // 判断属性 swagger的值是不是3.0开头
            if (o instanceof Map map) {
                if (map.containsKey("swagger") && !map.get("swagger").toString().startsWith("3.0")) {
                    throw new MSException(Translator.get("swagger_version_error"));
                }
            }
            result = new OpenAPIParser().readContents(apiTestStr, null, null);
            if (result == null || result.getOpenAPI() == null || !result.getOpenAPI().getOpenapi().startsWith("3.0") || result.isOpenapi31()) {
                throw new MSException(Translator.get("swagger_parse_error"));
            }
        }
        ApiDefinitionImport apiDefinitionImport = new ApiDefinitionImport();
        OpenAPI openAPI = result.getOpenAPI();
        apiDefinitionImport.setData(parseRequests(openAPI, request));
        return apiDefinitionImport;
    }

    private List<AuthorizationValue> setAuths(ImportRequest request) {
        List<AuthorizationValue> auths = new ArrayList<>();
        // TODO 如果有 BaseAuth 参数，base64 编码后转换成 headers
        if (request.isAuthSwitch()) {
            AuthorizationValue authorizationValue = new AuthorizationValue();
            authorizationValue.setType(HEADER);
            authorizationValue.setKeyName("Authorization");
            String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((request.getAuthUsername() + ":" + request.getAuthPassword()).getBytes());
            authorizationValue.setValue(authValue);
            auths.add(authorizationValue);
        }
        return CollectionUtils.size(auths) == 0 ? null : auths;
    }

    private List<ApiDefinitionImportDetail> parseRequests(OpenAPI openAPI, ImportRequest importRequest) {

        Paths paths = openAPI.getPaths();

        Set<String> pathNames = paths.keySet();

        this.components = openAPI.getComponents();

        List<ApiDefinitionImportDetail> results = new ArrayList<>();

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
                    //构建基本请求
                    ApiDefinitionImportDetail apiDefinitionDTO = buildSwaggerApiDefinition(operation, pathName, method, importRequest);
                    //构建请求参数
                    MsHTTPElement request = buildHttpRequest(apiDefinitionDTO.getName(), pathName, method);
                    parseParameters(operation, request);
                    parseParameters(pathItem, request);
                    //构建请求体
                    parseRequestBody(operation.getRequestBody(), request);
                    //构造 children
                    LinkedList<AbstractMsTestElement> children = new LinkedList<>();
                    children.add(new MsCommonElement());
                    request.setChildren(children);
                    //认证
                    request.setAuthConfig(new NoAuth());


                    //解析请求内容
                    parseResponse(operation.getResponses(), apiDefinitionDTO.getResponse(), request.getHeaders());
                    apiDefinitionDTO.setRequest(request);
                    results.add(apiDefinitionDTO);
                }
            }
        }

        return results;
    }

    private void parseRequestBody(RequestBody requestBody, MsHTTPElement request) {
        if (requestBody != null) {
            Content content = requestBody.getContent();
            if (content != null) {
                List<MsHeader> headers = request.getHeaders();
                content.forEach((key, value) -> {
                    setRequestBodyData(key, value, request.getBody());
                    // 如果key不包含Content-Type  则默认添加Content-Type
                    if (headers.stream().noneMatch(header -> StringUtils.equals(header.getKey(), ApiConstants.CONTENT_TYPE))) {
                        MsHeader header = new MsHeader();
                        header.setKey(ApiConstants.CONTENT_TYPE);
                        header.setValue(key);
                        headers.add(header);
                    }
                });
            } else {
                request.getBody().setBodyType(Body.BodyType.NONE.name());
                request.getBody().setNoneBody(new NoneBody());
            }
        } else {
            request.getBody().setBodyType(Body.BodyType.NONE.name());
            request.getBody().setNoneBody(new NoneBody());
        }
    }

    private void parseWWWFormBody(JsonSchemaItem item, Body body) {
        WWWFormBody wwwFormBody = new WWWFormBody();
        if (item == null) {
            body.setWwwFormBody(wwwFormBody);
            return;
        }
        List<String> required = item.getRequired();
        List<WWWFormKV> formDataKVS = new ArrayList<>();
        item.getProperties().forEach((key, value) -> {
            if (value != null && !StringUtils.equals(PropertyConstant.OBJECT, value.getType())) {
                FormDataKV formDataKV = new FormDataKV();
                formDataKV.setKey(key);
                formDataKV.setValue(String.valueOf(value.getExample()));
                formDataKV.setRequired(CollectionUtils.isNotEmpty(required) && required.contains(key));
                formDataKV.setDescription(value.getDescription());
                formDataKV.setParamType(value.getType());
                formDataKV.setMinLength(value.getMinLength());
                formDataKV.setMaxLength(value.getMaxLength());
                formDataKVS.add(formDataKV);
            }
        });
        wwwFormBody.setFormValues(formDataKVS);
        body.setWwwFormBody(wwwFormBody);
    }

    private void parseFormBody(JsonSchemaItem item, Body body) {
        FormDataBody formDataBody = new FormDataBody();
        if (item == null) {
            body.setFormDataBody(formDataBody);
            return;
        }
        List<String> required = item.getRequired();
        List<FormDataKV> formDataKVS = new ArrayList<>();
        item.getProperties().forEach((key, value) -> {
            if (value != null && !StringUtils.equals(PropertyConstant.OBJECT, value.getType())) {
                FormDataKV formDataKV = new FormDataKV();
                formDataKV.setKey(key);
                formDataKV.setValue(String.valueOf(value.getExample()));
                formDataKV.setRequired(CollectionUtils.isNotEmpty(required) && required.contains(key));
                formDataKV.setDescription(value.getDescription());
                formDataKV.setParamType(value.getType());
                formDataKV.setMinLength(value.getMinLength());
                formDataKV.setMaxLength(value.getMaxLength());
                formDataKVS.add(formDataKV);
            }
        });
        formDataBody.setFormValues(formDataKVS);
        body.setFormDataBody(formDataBody);
    }

    private void parseResponse(ApiResponses responseBody, List<HttpResponse> response, List<MsHeader> requestHeaders) {
        if (responseBody != null) {
            responseBody.forEach((key, value) -> {
                HttpResponse httpResponse = new HttpResponse();
                //TODO headers
                httpResponse.setStatusCode(StringUtils.equals("default", key) ? "200" : key);
                ResponseBody body = new ResponseBody();
                Map<String, io.swagger.v3.oas.models.headers.Header> headers = value.getHeaders();
                if (MapUtils.isNotEmpty(headers)) {
                    List<MsHeader> headerList = new ArrayList<>();
                    headers.forEach((k, v) -> {
                        MsHeader header = new MsHeader();
                        header.setKey(k);
                        header.setValue(getDefaultObjectValue(v.getExample()));
                        header.setDescription(getDefaultStringValue(v.getDescription()));
                        headerList.add(header);
                    });
                    httpResponse.setHeaders(headerList);
                }
                if (value.getContent() != null) {
                    value.getContent().forEach((k, v) -> {
                        setResponseBodyData(k, v, body);
                        if (requestHeaders.stream().noneMatch(header -> StringUtils.equals(header.getKey(), ApiConstants.ACCEPT))) {
                            MsHeader header = new MsHeader();
                            header.setKey(ApiConstants.ACCEPT);
                            header.setValue(k);
                            requestHeaders.add(header);
                        }
                    });
                } else {
                    body.setBodyType(Body.BodyType.NONE.name());
                }
                httpResponse.setBody(body);
                response.add(httpResponse);
            });
            // 判断  如果是200  默认defaultFlag为true 否则的话  随机挑一个为true
            if (CollectionUtils.isNotEmpty(response)) {
                response.forEach(httpResponse -> {
                    if (StringUtils.equals("200", httpResponse.getStatusCode())) {
                        httpResponse.setDefaultFlag(true);
                    }
                });
                if (response.stream().noneMatch(httpResponse -> StringUtils.equals("200", httpResponse.getStatusCode()))) {
                    response.getFirst().setDefaultFlag(true);
                }
            }
        }

    }

    private void setResponseBodyData(String k, io.swagger.v3.oas.models.media.MediaType value, ResponseBody body) {
        //TODO body  默认如果json格式
        JsonSchemaItem jsonSchemaItem = parseSchema(value.getSchema());
        switch (k) {
            case MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE -> {
                body.setBodyType(Body.BodyType.JSON.name());
                JsonBody jsonBody = new JsonBody();
                jsonBody.setJsonSchema(jsonSchemaItem);
                jsonBody.setEnableJsonSchema(false);
                if (ObjectUtils.isNotEmpty(value.getExample())) {
                    jsonBody.setJsonValue(ApiDataUtils.toJSONString(value.getExample()));
                }
                String jsonString = JSON.toJSONString(jsonSchemaItem);
                if (StringUtils.isNotBlank(jsonString)) {
                    jsonBody.setJsonValue(JsonSchemaBuilder.jsonSchemaToJson(jsonString));
                }
                body.setJsonBody(jsonBody);
            }
            case MediaType.APPLICATION_XML_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.XML.name());
                }
                XmlBody xml = new XmlBody();
                //xml.setValue(XMLUtils.jsonToXmlStr(jsonValue));
                body.setXmlBody(xml);
            }
            case MediaType.MULTIPART_FORM_DATA_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.FORM_DATA.name());
                }
            }
            case MediaType.APPLICATION_OCTET_STREAM_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.BINARY.name());
                }
            }
            case MediaType.TEXT_PLAIN_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.RAW.name());
                }
                RawBody rawBody = new RawBody();
                body.setRawBody(rawBody);
            }
            default -> body.setBodyType(Body.BodyType.NONE.name());
        }
    }

    private void setRequestBodyData(String k, io.swagger.v3.oas.models.media.MediaType value, Body body) {
        //TODO body  默认如果json格式
        JsonSchemaItem jsonSchemaItem = parseSchema(value.getSchema());
        switch (k) {
            case MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE -> {
                body.setBodyType(Body.BodyType.JSON.name());
                JsonBody jsonBody = new JsonBody();
                jsonBody.setJsonSchema(jsonSchemaItem);
                jsonBody.setEnableJsonSchema(false);
                if (ObjectUtils.isNotEmpty(value.getExample())) {
                    jsonBody.setJsonValue(ApiDataUtils.toJSONString(value.getExample()));
                }
                String jsonString = JSON.toJSONString(jsonSchemaItem);
                if (StringUtils.isNotBlank(jsonString)) {
                    jsonBody.setJsonValue(JsonSchemaBuilder.jsonSchemaToJson(jsonString));
                }
                body.setJsonBody(jsonBody);
            }
            case MediaType.APPLICATION_XML_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.XML.name());
                }
                XmlBody xml = new XmlBody();
                //xml.setValue(XMLUtils.jsonToXmlStr(jsonValue));
                body.setXmlBody(xml);
            }
            case MediaType.APPLICATION_FORM_URLENCODED_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.WWW_FORM.name());
                }
                parseWWWFormBody(jsonSchemaItem, body);
            }
            case MediaType.MULTIPART_FORM_DATA_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.FORM_DATA.name());
                }
                parseFormBody(jsonSchemaItem, body);
            }
            case MediaType.APPLICATION_OCTET_STREAM_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.BINARY.name());
                }
            }
            case MediaType.TEXT_PLAIN_VALUE -> {
                if (StringUtils.isBlank(body.getBodyType())) {
                    body.setBodyType(Body.BodyType.RAW.name());
                }
                RawBody rawBody = new RawBody();
                body.setRawBody(rawBody);
            }
            default -> body.setBodyType(Body.BodyType.NONE.name());
        }
    }

    private ApiDefinitionImportDetail buildSwaggerApiDefinition(Operation operation, String path, String
            method, ImportRequest importRequest) {
        String name;
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        String modulePath = CollectionUtils.isNotEmpty(operation.getTags()) ? StringUtils.join("/", operation.getTags().getFirst()) : StringUtils.EMPTY;
        return buildApiDefinition(name, path, method, modulePath, importRequest);
    }

    private void parseParameters(Operation operation, MsHTTPElement request) {

        List<Parameter> parameters = operation.getParameters();

        if (CollectionUtils.isEmpty(parameters)) {
            return;
        }
        parameters.forEach(parameter -> {
            switch (parameter) {
                case QueryParameter queryParameter -> parseQueryParameters(queryParameter, request.getQuery());
                case PathParameter pathParameter -> parsePathParameters(pathParameter, request.getRest());
                case HeaderParameter headerParameter -> parseHeaderParameters(headerParameter, request.getHeaders());
                case CookieParameter cookieParameter -> parseCookieParameters(cookieParameter, request.getHeaders());
                default -> {
                }
            }
        });
    }

    private void parseParameters(PathItem path, MsHTTPElement request) {
        if (path.getParameters() == null) {
            return;
        }
        List<Parameter> parameters = path.getParameters();
        // 处理特殊格式  rest参数是和请求平级的情况

        for (Parameter parameter : parameters) {
            if (StringUtils.isNotBlank(parameter.getIn())) {
                switch (parameter.getIn()) {
                    case PATH -> parsePathParameters((PathParameter) parameter, request.getRest());
                    case QUERY -> parseQueryParameters((QueryParameter) parameter, request.getQuery());
                    case HEADER -> parseHeaderParameters((HeaderParameter) parameter, request.getHeaders());
                    case COOKIE -> parseCookieParameters((CookieParameter) parameter, request.getHeaders());
                    default -> {
                        return;
                    }
                }
            }
        }
    }


    private void parseQueryParameters(QueryParameter queryParameter, List<QueryParam> arguments) {
        QueryParam queryParam = new QueryParam();
        queryParam.setKey(getDefaultStringValue(queryParameter.getName()));
        queryParam.setRequired(queryParameter.getRequired());
        queryParam.setDescription(getDefaultStringValue(queryParameter.getDescription()));
        if (queryParameter.getSchema() != null) {
            queryParam.setParamType(queryParameter.getSchema().getType());
            queryParam.setValue(getDefaultObjectValue(queryParameter.getSchema().getExample()));
            queryParam.setMinLength(queryParameter.getSchema().getMinLength());
            queryParam.setMaxLength(queryParameter.getSchema().getMaxLength());
        }
        if (queryParameter.getExample() != null) {
            queryParam.setValue(getDefaultObjectValue(queryParameter.getExample()));
        }
        arguments.add(queryParam);
    }


    private void parseCookieParameters(CookieParameter cookieParameter, List<MsHeader> headers) {
        MsHeader headerParams = new MsHeader();
        headerParams.setKey(getDefaultStringValue(cookieParameter.getName()));
        headerParams.setDescription(getDefaultStringValue(cookieParameter.getDescription()));
        if (cookieParameter.getSchema() != null) {
            headerParams.setValue(getDefaultObjectValue(cookieParameter.getSchema().getExample()));
        }
        if (cookieParameter.getExample() != null) {
            headerParams.setValue(getDefaultObjectValue(cookieParameter.getExample()));
        }
        headers.add(headerParams);
    }

    private void parseHeaderParameters(HeaderParameter headerParameter, List<MsHeader> headers) {
        MsHeader headerParams = new MsHeader();
        headerParams.setKey(getDefaultStringValue(headerParameter.getName()));
        headerParams.setDescription(getDefaultStringValue(headerParameter.getDescription()));
        if (headerParameter.getSchema() != null) {
            headerParams.setValue(getDefaultObjectValue(headerParameter.getSchema().getExample()));
        }
        if (headerParameter.getExample() != null) {
            headerParams.setValue(getDefaultObjectValue(headerParameter.getExample()));
        }
        headers.add(headerParams);
    }


    private void parsePathParameters(PathParameter parameter, List<RestParam> rest) {
        RestParam restParam = new RestParam();
        restParam.setKey(getDefaultStringValue(parameter.getName()));
        restParam.setRequired(parameter.getRequired());
        restParam.setDescription(getDefaultStringValue(parameter.getDescription()));
        if (parameter.getSchema() != null) {
            restParam.setParamType(parameter.getSchema().getType());
            restParam.setValue(getDefaultObjectValue(parameter.getSchema().getExample()));
            restParam.setMinLength(parameter.getSchema().getMinLength());
            restParam.setMaxLength(parameter.getSchema().getMaxLength());
        }
        if (parameter.getExample() != null) {
            restParam.setValue(getDefaultObjectValue(parameter.getExample()));
        }
        rest.add(restParam);
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? StringUtils.EMPTY : val;
    }

    private String getDefaultObjectValue(Object val) {
        return val == null ? StringUtils.EMPTY : val.toString();
    }

    private Schema<?> getModelByRef(String ref) {
        if (StringUtils.isBlank(ref)) {
            return null;
        }
        if (ref.split("/").length > 3) {
            ref = ref.replace("#/components/schemas/", StringUtils.EMPTY);
        }
        if (this.components.getSchemas() != null) return this.components.getSchemas().get(ref);
        return null;
    }


    private JsonSchemaItem parseSchema(Schema<?> schema) {
        if (schema != null) {
            String refName = schema.get$ref();
            Schema<?> modelByRef;
            if (StringUtils.isNotBlank(refName)) {
                modelByRef = getModelByRef(refName);
            } else {
                modelByRef = schema;
            }

            if (modelByRef != null) {
                return switch (modelByRef) {
                    case ArraySchema arraySchema -> parseArraySchema(arraySchema.getItems(), false);
                    case ObjectSchema objectSchema -> parseObject(objectSchema, false);
                    default -> {
                        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
                        Map<String, JsonSchemaItem> jsonSchemaProperties = new LinkedHashMap<>();
                        if (MapUtils.isNotEmpty(modelByRef.getProperties())) {
                            modelByRef.getProperties().forEach((key, value) -> {
                                JsonSchemaItem item = parseProperty(value, false);
                                jsonSchemaProperties.put(key, item);
                            });
                        }
                        jsonSchemaItem.setProperties(jsonSchemaProperties);
                        yield jsonSchemaItem;
                    }
                };
            }
        }
        return null;
    }

    private JsonSchemaItem parseMapObject(MapSchema mapSchema) {
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setType(PropertyConstant.OBJECT);
        jsonSchemaItem.setRequired(mapSchema.getRequired());
        jsonSchemaItem.setDescription(mapSchema.getDescription());
        Object value = mapSchema.getAdditionalProperties();
        Map<String, JsonSchemaItem> jsonSchemaProperties = new LinkedHashMap<>();
        if (ObjectUtils.isEmpty(value)) {
            return jsonSchemaItem;
        }
        JsonSchemaItem item = new JsonSchemaItem();
        switch (value) {
            case IntegerSchema integerSchema -> item = parseInteger(integerSchema);
            case StringSchema stringSchema -> item = parseString(stringSchema);
            case NumberSchema numberSchema -> item = parseNumber(numberSchema);
            case BooleanSchema booleanSchema -> item = parseBoolean(booleanSchema);
            case ArraySchema arraySchema -> item = parseArraySchema(arraySchema.getItems(), false);
            case ObjectSchema objectSchemaItem -> item = parseObject(objectSchemaItem, false);
            default -> {
            }
        }
        jsonSchemaProperties.put(StringUtils.EMPTY, item);
        jsonSchemaItem.setProperties(jsonSchemaProperties);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseObject(ObjectSchema objectSchema, boolean onlyOnce) {
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setType(PropertyConstant.OBJECT);
        jsonSchemaItem.setRequired(objectSchema.getRequired());
        jsonSchemaItem.setId(IDGenerator.nextStr());
        jsonSchemaItem.setDescription(objectSchema.getDescription());
        Map<String, JsonSchemaItem> jsonSchemaProperties = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(objectSchema.getProperties())) {
            objectSchema.getProperties().forEach((key, value) -> {
                JsonSchemaItem item = parseProperty(value, onlyOnce);
                jsonSchemaProperties.put(key, item);
            });
        }
        jsonSchemaItem.setProperties(jsonSchemaProperties);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseProperty(Schema<?> value, boolean onlyOnce) {
        if (StringUtils.equals(value.getType(), PropertyConstant.NULL)) {
            return parseNull();
        }

        return switch (value) {
            case IntegerSchema integerSchema -> parseInteger(integerSchema);
            case StringSchema stringSchema -> parseString(stringSchema);
            case NumberSchema numberSchema -> parseNumber(numberSchema);
            case BooleanSchema booleanSchema -> parseBoolean(booleanSchema);
            case ArraySchema arraySchema -> {
                if (onlyOnce) {
                    JsonSchemaItem arrayItem = new JsonSchemaItem();
                    arrayItem.setId(IDGenerator.nextStr());
                    arrayItem.setType(PropertyConstant.ARRAY);
                    arrayItem.setItems(new JsonSchemaItem());
                    yield arrayItem;
                }
                yield isRef(arraySchema.getItems(), 0) ? parseArraySchema(arraySchema.getItems(), true) :
                        parseArraySchema(arraySchema.getItems(), false);
            }
            case ObjectSchema objectSchema -> {
                if (onlyOnce) {
                    JsonSchemaItem objectItem = new JsonSchemaItem();
                    objectItem.setId(IDGenerator.nextStr());
                    objectItem.setType(PropertyConstant.OBJECT);
                    objectItem.setProperties(new LinkedHashMap<>());
                    yield objectItem;
                }
                yield isRef(objectSchema, 0) ? parseObject(objectSchema, true) :
                        parseObject(objectSchema, false);
            }
            case MapSchema mapSchema -> parseMapObject(mapSchema);
            case Schema<?> items -> {
                if (isRef(items, 0)) {
                    JsonSchemaItem arrayItem = new JsonSchemaItem();
                    arrayItem.setId(IDGenerator.nextStr());
                    arrayItem.setType(PropertyConstant.OBJECT);
                    yield arrayItem;
                }
                yield parseSchema(items);
            }
        };

    }


    //判断对象是否存在一直引用
    private boolean isRef(Schema<?> schema, int level) {
        if (level > 20 || schema == null) {
            return true;
        }

        if (StringUtils.isNotBlank(schema.get$ref())) {
            schema = getModelByRef(schema.get$ref());
        }

        assert schema != null;

        return switch (schema) {
            case ArraySchema arraySchema -> isRef(arraySchema.getItems(), level + 1);
            case ObjectSchema objectSchema -> hasRefInObjectSchema(objectSchema, level + 1);
            default -> false;
        };
    }


    private boolean hasRefInObjectSchema(ObjectSchema objectSchema, int level) {
        if (level > 20) {
            return true;
        }

        if (MapUtils.isNotEmpty(objectSchema.getProperties())) {
            for (Schema<?> value : objectSchema.getProperties().values()) {
                if (isRef(value, level + 1)) {
                    return true;
                }
            }
        }
        return false;
    }


    private JsonSchemaItem parseString(StringSchema stringSchema) {
        JsonSchemaItem jsonSchemaString = new JsonSchemaItem();
        jsonSchemaString.setType(PropertyConstant.STRING);
        jsonSchemaString.setId(IDGenerator.nextStr());
        jsonSchemaString.setFormat(StringUtils.isNotBlank(stringSchema.getFormat()) ? stringSchema.getFormat() : StringUtils.EMPTY);
        jsonSchemaString.setDescription(getDefaultStringValue(stringSchema.getDescription()));
        jsonSchemaString.setExample(stringSchema.getExample());
        if (stringSchema.getMaxLength() != null) {
            jsonSchemaString.setMaxLength(stringSchema.getMaxLength());
        }
        if (stringSchema.getMinLength() != null) {
            jsonSchemaString.setMinLength(stringSchema.getMinLength());
        }
        jsonSchemaString.setPattern(stringSchema.getPattern());
        jsonSchemaString.setEnumString(stringSchema.getEnum());
        if (stringSchema.getExample() == null && CollectionUtils.isNotEmpty(stringSchema.getEnum())) {
            jsonSchemaString.setExample(stringSchema.getEnum().getFirst());
        }
        return jsonSchemaString;
    }

    private JsonSchemaItem parseInteger(IntegerSchema integerSchema) {
        JsonSchemaItem jsonSchemaInteger = new JsonSchemaItem();
        jsonSchemaInteger.setType(PropertyConstant.INTEGER);
        jsonSchemaInteger.setId(IDGenerator.nextStr());
        jsonSchemaInteger.setFormat(StringUtils.isNotBlank(integerSchema.getFormat()) ? integerSchema.getFormat() : StringUtils.EMPTY);
        jsonSchemaInteger.setDescription(StringUtils.isNotBlank(integerSchema.getDescription()) ? integerSchema.getDescription() : StringUtils.EMPTY);
        jsonSchemaInteger.setExample(integerSchema.getExample());
        jsonSchemaInteger.setMaximum(integerSchema.getMaximum());
        jsonSchemaInteger.setMinimum(integerSchema.getMinimum());
        jsonSchemaInteger.setEnumInteger(integerSchema.getEnum());
        return jsonSchemaInteger;
    }

    private JsonSchemaItem parseNumber(NumberSchema numberSchema) {
        JsonSchemaItem jsonSchemaNumber = new JsonSchemaItem();
        jsonSchemaNumber.setType(PropertyConstant.NUMBER);
        jsonSchemaNumber.setId(IDGenerator.nextStr());
        jsonSchemaNumber.setDescription(StringUtils.isNotBlank(numberSchema.getDescription()) ? numberSchema.getDescription() : StringUtils.EMPTY);
        jsonSchemaNumber.setExample(numberSchema.getExample());
        jsonSchemaNumber.setEnumNumber(numberSchema.getEnum());
        return jsonSchemaNumber;
    }

    private JsonSchemaItem parseBoolean(BooleanSchema booleanSchema) {
        JsonSchemaItem jsonSchemaBoolean = new JsonSchemaItem();
        jsonSchemaBoolean.setType(PropertyConstant.BOOLEAN);
        jsonSchemaBoolean.setId(IDGenerator.nextStr());
        jsonSchemaBoolean.setDescription(getDefaultStringValue(booleanSchema.getDescription()));
        jsonSchemaBoolean.setExample(booleanSchema.getExample());
        return jsonSchemaBoolean;
    }

    private JsonSchemaItem parseNull() {
        JsonSchemaItem jsonSchemaNull = new JsonSchemaItem();
        jsonSchemaNull.setId(IDGenerator.nextStr());
        jsonSchemaNull.setType(PropertyConstant.NULL);
        return jsonSchemaNull;
    }

    private JsonSchemaItem parseArraySchema(Schema<?> items, boolean onlyOnce) {
        JsonSchemaItem jsonSchemaArray = new JsonSchemaItem();
        jsonSchemaArray.setType(PropertyConstant.ARRAY);
        jsonSchemaArray.setId(IDGenerator.nextStr());
        Schema<?> itemsSchema;
        if (StringUtils.isNotBlank(items.get$ref())) {
            itemsSchema = getModelByRef(items.get$ref());
        } else {
            itemsSchema = items;
        }
        if (itemsSchema == null) {
            return jsonSchemaArray;
        }

        JsonSchemaItem itemsJsonSchema = parseProperty(itemsSchema, onlyOnce);
        jsonSchemaArray.setItems(itemsJsonSchema);

        return jsonSchemaArray;
    }

}
