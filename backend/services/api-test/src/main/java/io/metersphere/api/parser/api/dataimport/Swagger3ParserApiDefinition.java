package io.metersphere.api.parser.api.dataimport;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
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
import io.metersphere.api.utils.JsonSchemaBuilder;
import io.metersphere.api.utils.XMLUtil;
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
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;


public class Swagger3ParserApiDefinition extends HttpApiDefinitionImportAbstractParser<ApiDefinitionImportFileParseResult> {

    protected String projectId;
    private Components components;

    public static final String PATH = "path";
    public static final String HEADER = "header";
    public static final String COOKIE = "cookie";
    public static final String QUERY = "query";

    private void testUrlTimeout(String swaggerUrl) {
        HttpURLConnection connection = null;
        try {
            URI uriObj = new URI(swaggerUrl);
            connection = (HttpURLConnection) uriObj.toURL().openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000); // 设置超时时间
            connection.connect(); // 建立连接
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("url_format_error"));
        } finally {
            if (connection != null) {
                connection.disconnect(); // 关闭连接
            }
        }
    }

    public ApiDefinitionImportFileParseResult parse(InputStream source, ImportRequest request) throws Exception {

        //将之前在service中的swagger地址判断放在这里。
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            this.testUrlTimeout(request.getSwaggerUrl());
        }

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
            Map<String, Object> o = JSON.parseMap(apiTestStr);
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
        ApiDefinitionImportFileParseResult apiDefinitionImportFileParseResult = new ApiDefinitionImportFileParseResult();
        OpenAPI openAPI = result.getOpenAPI();
        apiDefinitionImportFileParseResult.setData(parseRequests(openAPI, request));
        return apiDefinitionImportFileParseResult;
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

        // 设置 headers
        if (StringUtils.isNotBlank(request.getSwaggerToken())) {
            String[] tokenRows = StringUtils.split(request.getSwaggerToken(), StringUtils.LF);
            for (String row : tokenRows) {
                String[] tokenArr = StringUtils.split(row, ":");
                if (tokenArr.length == 2) {
                    AuthorizationValue authorizationValue = new AuthorizationValue();
                    authorizationValue.setType(HEADER);
                    authorizationValue.setKeyName(tokenArr[0]);
                    authorizationValue.setValue(tokenArr[1]);
                    auths.add(authorizationValue);
                }
            }
        }

        return CollectionUtils.size(auths) == 0 ? null : auths;
    }

    private List<ApiDefinitionDetail> parseRequests(OpenAPI openAPI, ImportRequest importRequest) {

        Paths paths = openAPI.getPaths();

        Set<String> pathNames = paths.keySet();

        this.components = openAPI.getComponents();

        List<ApiDefinitionDetail> results = new ArrayList<>();

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
                    ApiDefinitionDetail apiDefinitionDTO = buildSwaggerApiDefinition(operation, pathName, method, importRequest);
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
                Iterator<Map.Entry<String, io.swagger.v3.oas.models.media.MediaType>> iterator = content.entrySet().iterator();
                if (iterator.hasNext()) {
                    // 优先获取第一个
                    Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> mediaType = iterator.next();
                    setRequestBodyData(mediaType.getKey(), mediaType.getValue(), request.getBody());
                    // 如果key不包含Content-Type  则默认添加Content-Type
                    if (headers.stream().noneMatch(header -> StringUtils.equals(header.getKey(), ApiConstants.CONTENT_TYPE))) {
                        MsHeader header = new MsHeader();
                        header.setKey(ApiConstants.CONTENT_TYPE);
                        header.setValue(mediaType.getKey());
                        headers.add(header);
                    }
                }
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
                formDataKV.setValue(value.getExample());
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
                formDataKV.setValue(value.getExample());
                formDataKV.setRequired(CollectionUtils.isNotEmpty(required) && required.contains(key));
                formDataKV.setDescription(value.getDescription());
                formDataKV.setParamType(value.getType());
                formDataKV.setMinLength(value.getMinLength());
                formDataKV.setMaxLength(value.getMaxLength());
                if (StringUtils.equals(value.getType(), PropertyConstant.FILE)) {
                    formDataKV.setFiles(new ArrayList<>());
                }
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
                httpResponse.setId(IDGenerator.nextStr());
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
        JsonSchemaItem jsonSchemaItem = parseSchema(value.getSchema(), new HashSet<>());
        switch (k) {
            case MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE -> {
                body.setBodyType(Body.BodyType.JSON.name());
                body.setJsonBody(getJsonBody(value, jsonSchemaItem));
            }
            case MediaType.APPLICATION_XML_VALUE -> {
                body.setBodyType(Body.BodyType.XML.name());
                XmlBody xml = new XmlBody();
                try {
                    String xmlBody = parseXmlBody(value, jsonSchemaItem);
                    xml.setValue(xmlBody);
                } catch (Exception e) {
                    xml.setValue(e.getMessage());
                }
                body.setXmlBody(xml);
            }
            case MediaType.MULTIPART_FORM_DATA_VALUE -> {
                body.setBodyType(Body.BodyType.FORM_DATA.name());
            }
            case MediaType.APPLICATION_OCTET_STREAM_VALUE -> {
                body.setBodyType(Body.BodyType.BINARY.name());
            }
            case MediaType.TEXT_PLAIN_VALUE -> {
                body.setBodyType(Body.BodyType.RAW.name());
                RawBody rawBody = new RawBody();
                if (ObjectUtils.isNotEmpty(value.getSchema().getExample())) {
                    rawBody.setValue(value.getSchema().getExample().toString());
                }
                body.setRawBody(rawBody);
            }
            default -> body.setBodyType(Body.BodyType.NONE.name());
        }
    }

    private JsonBody getJsonBody(io.swagger.v3.oas.models.media.MediaType value, JsonSchemaItem jsonSchemaItem) {
        JsonBody jsonBody = new JsonBody();
        jsonBody.setJsonSchema(jsonSchemaItem);
        if (ObjectUtils.isNotEmpty(value.getExample())) {
            jsonBody.setJsonValue(JSON.toJSONString(value.getExample()));
        } else {
            String jsonString = JSON.toJSONString(jsonSchemaItem);
            if (StringUtils.isNotBlank(jsonString)) {
                jsonBody.setJsonValue(JsonSchemaBuilder.jsonSchemaToJson(jsonString, true));
            }
        }
        return jsonBody;
    }

    private void setRequestBodyData(String k, io.swagger.v3.oas.models.media.MediaType value, Body body) {
        JsonSchemaItem jsonSchemaItem = parseSchema(value.getSchema(), new HashSet<>());
        switch (k) {
            case MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE -> {
                body.setBodyType(Body.BodyType.JSON.name());
                body.setJsonBody(getJsonBody(value, jsonSchemaItem));
            }
            case MediaType.APPLICATION_XML_VALUE -> {
                body.setBodyType(Body.BodyType.XML.name());
                XmlBody xml = new XmlBody();
                String xmlBody = parseXmlBody(value, jsonSchemaItem);
                xml.setValue(xmlBody);
                body.setXmlBody(xml);
            }
            case MediaType.APPLICATION_FORM_URLENCODED_VALUE -> {
                body.setBodyType(Body.BodyType.WWW_FORM.name());
                parseWWWFormBody(jsonSchemaItem, body);
            }
            case MediaType.MULTIPART_FORM_DATA_VALUE -> {
                body.setBodyType(Body.BodyType.FORM_DATA.name());
                parseFormBody(jsonSchemaItem, body);
            }
            case MediaType.APPLICATION_OCTET_STREAM_VALUE -> {
                body.setBodyType(Body.BodyType.BINARY.name());
            }
            case MediaType.TEXT_PLAIN_VALUE -> {
                body.setBodyType(Body.BodyType.RAW.name());
                RawBody rawBody = new RawBody();
                if (ObjectUtils.isNotEmpty(value.getSchema().getExample())) {
                    rawBody.setValue(value.getSchema().getExample().toString());
                }
                body.setRawBody(rawBody);
            }
            default -> body.setBodyType(Body.BodyType.NONE.name());
        }
    }

    private String parseXmlBody(io.swagger.v3.oas.models.media.MediaType value, JsonSchemaItem jsonSchemaItem) {
        Schema schema = value.getSchema();
        JSONObject object = new JSONObject();
        if (value.getExample() != null) {
            return value.getExample().toString();
        }

        if (jsonSchemaItem != null && MapUtils.isNotEmpty(jsonSchemaItem.getProperties())) {
            if (StringUtils.isNotBlank(schema.get$ref()) && schema.get$ref().split("/").length > 3) {
                String ref = schema.get$ref().replace("#/components/schemas/", StringUtils.EMPTY);
                object.put(ref, jsonSchemaItem.getProperties());
                return XMLUtil.jsonToPrettyXml(object);
            }
        } else {
            if (schema != null && StringUtils.isNotBlank(schema.getName())) {
                object.put(schema.getName(), schema.getExample());
            }
        }

        return XMLUtil.jsonToPrettyXml(object);
    }

    private ApiDefinitionDetail buildSwaggerApiDefinition(Operation operation, String path, String
            method, ImportRequest importRequest) {
        String name;
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        String modulePath = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(operation.getTags())) {
            modulePath = operation.getTags().getFirst();
            if (!StringUtils.startsWith(modulePath, "/")) {
                modulePath = "/" + modulePath;
            }
        }
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

    private Schema<?> getRefSchema(Schema<?> schema) {
        String refName = schema.get$ref();
        if (StringUtils.isNotBlank(refName)) {
            return getModelByRef(refName);
        }
        return null;
    }

    private JsonSchemaItem parseSchema(Schema<?> schema, Set refModelSet) {
        if (schema != null) {
            if (StringUtils.equals(schema.getType(), PropertyConstant.NULL)) {
                return parseNull();
            }
            return switch (schema) {
                case ArraySchema arraySchema -> parseArraySchema(arraySchema, refModelSet);
                case ObjectSchema objectSchema -> parseObject(objectSchema, refModelSet);
                case MapSchema mapSchema -> parseMapObject(mapSchema, refModelSet);
                case IntegerSchema integerSchema -> parseInteger(integerSchema);
                case StringSchema stringSchema -> parseString(stringSchema);
                case NumberSchema numberSchema -> parseNumber(numberSchema);
                case BooleanSchema booleanSchema -> parseBoolean(booleanSchema);
                default -> {
                    if (StringUtils.isNotBlank(schema.get$ref())) {
                        yield parseObject(schema, refModelSet);
                    }
                    yield parseSchemaByType(schema, refModelSet);
                }
            };
        }
        return null;
    }

private JsonSchemaItem parseSchemaByType(Schema<?> schema, Set refModelSet) {
        String type = schema.getType();

        // 处理type为null的情况
        if (type == null) {
            return MapUtils.isNotEmpty(schema.getProperties())
                ? parseObject(schema, refModelSet)
                : parseNull();
        }

        // 使用switch表达式根据类型匹配相应处理方法
        return switch (type) {
            case PropertyConstant.STRING, PropertyConstant.TEXT -> parseString(schema);
            case PropertyConstant.INTEGER -> parseInteger(schema);
            case PropertyConstant.NUMBER -> parseNumber(schema);
            case PropertyConstant.BOOLEAN -> parseBoolean(schema);
            case PropertyConstant.OBJECT -> parseObject(schema, refModelSet);
            case PropertyConstant.ARRAY -> parseArraySchema(schema, refModelSet);
            default -> createDefaultJsonSchemaItem(schema);
        };
    }

    private JsonSchemaItem createDefaultJsonSchemaItem(Schema<?> schema) {
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setId(IDGenerator.nextStr());
        if (StringUtils.isNotBlank(schema.getType())) {
            jsonSchemaItem.setType(schema.getType());
        }
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseMapObject(MapSchema mapSchema, Set refModelSet) {
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
        if (value instanceof Schema<?> schema) {
            item = parseSchema(schema, refModelSet);
        }
        jsonSchemaProperties.put(StringUtils.EMPTY, item);
        jsonSchemaItem.setProperties(jsonSchemaProperties);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseObject(Schema objectSchema, Set refModelSet) {
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setType(PropertyConstant.OBJECT);
        jsonSchemaItem.setRequired(objectSchema.getRequired());
        jsonSchemaItem.setId(IDGenerator.nextStr());
        jsonSchemaItem.setDescription(objectSchema.getDescription());
        Map<String, JsonSchemaItem> jsonSchemaProperties = new LinkedHashMap<>();
        Map<String, Schema> properties = objectSchema.getProperties();
        Schema<?> refSchema = getRefSchema(objectSchema);
        if (refSchema != null) {
            if (refModelSet.contains(objectSchema.get$ref())) {
                // 如果存在循环引用，则直接返回
                return jsonSchemaItem;
            }
            properties = refSchema.getProperties();
            // 记录引用的对象
            refModelSet.add(objectSchema.get$ref());
        }
        if (MapUtils.isNotEmpty(properties)) {
            properties.forEach((key, value) -> {
                JsonSchemaItem item = parseSchema(value, refModelSet);
                jsonSchemaProperties.put(key, item);
            });
        }
        jsonSchemaItem.setProperties(jsonSchemaProperties);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseString(Schema stringSchema) {
        JsonSchemaItem jsonSchemaItem = parseSchemaItem(stringSchema);
        jsonSchemaItem.setType(PropertyConstant.STRING);
        jsonSchemaItem.setFormat(getDefaultStringValue(stringSchema.getFormat()));
        jsonSchemaItem.setDescription(getDefaultStringValue(stringSchema.getDescription()));
        jsonSchemaItem.setMaxLength(stringSchema.getMaxLength());
        jsonSchemaItem.setMinLength(stringSchema.getMinLength());
        jsonSchemaItem.setPattern(stringSchema.getPattern());
        jsonSchemaItem.setEnumValues(stringSchema.getEnum());
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseInteger(Schema integerSchema) {
        JsonSchemaItem jsonSchemaItem = parseSchemaItem(integerSchema);
        jsonSchemaItem.setType(PropertyConstant.INTEGER);
        jsonSchemaItem.setFormat(StringUtils.isNotBlank(integerSchema.getFormat()) ? integerSchema.getFormat() : StringUtils.EMPTY);
        jsonSchemaItem.setMaximum(integerSchema.getMaximum());
        jsonSchemaItem.setMinimum(integerSchema.getMinimum());
        List<Number> enumValues = integerSchema.getEnum();
        if (CollectionUtils.isNotEmpty(enumValues)) {
            jsonSchemaItem.setEnumValues(enumValues.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .toList());
        }
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseNumber(Schema numberSchema) {
        JsonSchemaItem jsonSchemaItem = parseSchemaItem(numberSchema);
        jsonSchemaItem.setType(PropertyConstant.NUMBER);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseBoolean(Schema booleanSchema) {
        JsonSchemaItem jsonSchemaItem = parseSchemaItem(booleanSchema);
        jsonSchemaItem.setType(PropertyConstant.BOOLEAN);
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseSchemaItem(Schema schema) {
        JsonSchemaItem jsonSchemaItem = new JsonSchemaItem();
        jsonSchemaItem.setId(IDGenerator.nextStr());
        jsonSchemaItem.setDescription(getDefaultStringValue(schema.getDescription()));
        Optional.ofNullable(schema.getExample()).ifPresent(example -> jsonSchemaItem.setExample(example.toString()));
        jsonSchemaItem.setEnumValues(schema.getEnum());
        jsonSchemaItem.setDefaultValue(schema.getDefault());
        return jsonSchemaItem;
    }

    private JsonSchemaItem parseNull() {
        JsonSchemaItem jsonSchemaNull = new JsonSchemaItem();
        jsonSchemaNull.setId(IDGenerator.nextStr());
        jsonSchemaNull.setType(PropertyConstant.NULL);
        return jsonSchemaNull;
    }

    private JsonSchemaItem parseArraySchema(Schema arraySchema, Set refModelSet) {
        JsonSchemaItem jsonSchemaArray = new JsonSchemaItem();
        jsonSchemaArray.setType(PropertyConstant.ARRAY);
        jsonSchemaArray.setId(IDGenerator.nextStr());
        jsonSchemaArray.setMaxItems(arraySchema.getMaxItems());
        jsonSchemaArray.setMinItems(arraySchema.getMinItems());

        JsonSchemaItem itemsJsonSchema = parseSchema(arraySchema.getItems(), refModelSet);
        if (itemsJsonSchema != null) {
            jsonSchemaArray.setItems(List.of(itemsJsonSchema));
        }
        return jsonSchemaArray;
    }

}
