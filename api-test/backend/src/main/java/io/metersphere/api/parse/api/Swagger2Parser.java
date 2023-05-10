package io.metersphere.api.parse.api;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.JsonSchemaItem;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.constants.SwaggerParameterType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.utils.LoggerUtil;
import io.swagger.models.*;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

public class Swagger2Parser extends SwaggerAbstractParser {

    private Map<String, Model> definitions = null;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        Swagger swagger = null;
        String sourceStr = StringUtils.EMPTY;
        List<AuthorizationValue> auths = setAuths(request);
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            try {
                //  使用 url 导入 swagger
                swagger = new SwaggerParser().read(request.getSwaggerUrl(), auths, false);
            } catch (Exception e) {
                LoggerUtil.error(e);
                MSException.throwException(e.getMessage());
            }

        } else {
            sourceStr = getApiTestStr(source);  //  导入的二进制文件转换为 String
            //注：有一特殊情况，swagger2.0 文件里如果在response的parameter参数下的properties的参数里存在 required 为string类型，
            //swagger2.0不会导入，需替换一下
            sourceStr = replaceStr(sourceStr);

            JSONObject jsonObject = JSONUtil.parseObject(sourceStr);
            if (jsonObject.opt("swagger") == null || jsonObject.opt("swagger") == "null" || jsonObject.opt("swagger") == StringUtils.SPACE) {
                if (jsonObject.opt("openapi") == null || jsonObject.opt("openapi") == "null" || jsonObject.opt("openapi") == StringUtils.SPACE) {
                    MSException.throwException("wrong format");
                }
            }
            swagger = new SwaggerParser().readWithInfo(sourceStr, false).getSwagger();
        }
        if (swagger == null || swagger.getSwagger() == null) {
            //  不是 2.0 版本，则尝试转换 3.0
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            return swagger3Parser.parse(sourceStr, request);
        }
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(swagger, request));
        return definitionImport;
    }

    public static String replaceStr(String sourceStr) {
        return sourceStr.replaceAll("\"required\": \"(.*?)\"", "\"required\": []");
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


    private List<ApiDefinitionWithBLOBs> parseRequests(Swagger swagger, ApiTestImportRequest importRequest) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();

        this.definitions = swagger.getDefinitions();
        if (this.definitions == null)
            this.definitions = new HashMap<>();

        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        String basePath = swagger.getBasePath();
        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                MsHTTPSamplerProxy request = buildRequest(operation, pathName, method.name());
                request.setFollowRedirects(true);
                request.setResponseTimeout("60000");
                request.setConnectTimeout("60000");
                ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method.name(), importRequest);
                apiDefinition.setDescription(operation.getDescription());
                parseParameters(operation, request);
                addBodyHeader(request);
                if (StringUtils.isNotBlank(basePath)) {
                    String pathStr = (basePath + apiDefinition.getPath()).replaceAll("//", "/");
                    apiDefinition.setPath(pathStr);
                    request.setPath(pathStr);
                }
                apiDefinition.setRequest(JSON.toJSONString(request));
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation, operation.getResponses())));

                buildModulePath(apiDefinition, operation.getTags());
                if (operation.isDeprecated() != null && operation.isDeprecated()) {
                    apiDefinition.setTags("[\"Deleted\"]");
                }
                results.add(apiDefinition);
            }
        }

        this.definitions = null;
        return results;
    }

    private void buildModulePath(ApiDefinitionWithBLOBs apiDefinition, List<String> tags) {
        StringBuilder modulePathBuilder = new StringBuilder();
        String modulePath = getModulePath(tags, modulePathBuilder);
        apiDefinition.setModulePath(modulePath);
    }

    private String getModulePath(List<String> tagTree, StringBuilder modulePath) {
        if (tagTree == null) {
            return StringUtils.EMPTY;
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

    private ApiDefinitionWithBLOBs buildApiDefinition(String id, Operation operation, String path, String method, ApiTestImportRequest importRequest) {
        String name = StringUtils.EMPTY;
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
        request.getBody().setType(getBodyType(operation));

        // todo 路径变量 {xxx} 是否要转换

        for (Parameter parameter : parameters) {
            if (StringUtils.isNotBlank(parameter.getIn())) {
                switch (parameter.getIn()) {
                    case SwaggerParameterType.PATH:
                        parsePathParameters(parameter, request.getRest());
                        break;
                    case SwaggerParameterType.QUERY:
                        parseQueryParameters(parameter, request.getArguments());
                        break;
                    case SwaggerParameterType.FORM_DATA:
                        parseFormDataParameters((FormParameter) parameter, request.getBody());
                        break;
                    case SwaggerParameterType.BODY:
                        parseRequestBodyParameters(parameter, request.getBody());
                        break;
                    case SwaggerParameterType.HEADER:
                        parseHeaderParameters(parameter, request.getHeaders());
                        break;
                    case SwaggerParameterType.COOKIE:
                        parseCookieParameters(parameter, request.getHeaders());
                        break;
//                case SwaggerParameterType.FILE:
//                    parsePathParameters(parameter, request);
//                    break;
                }
            }
        }
    }

    private String getBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getConsumes())) {
            return Body.JSON_STR;
        }
        String contentType = operation.getConsumes().get(0);
        return getBodyType(contentType);
    }

    private String getResponseBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getProduces())) {
            return Body.JSON_STR;
        }
        String contentType = operation.getProduces().get(0);
        return getBodyType(contentType);
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(),
                pathParameter.getExample() != null ? String.valueOf(pathParameter.getExample()) : null,
                getDefaultStringValue(parameter.getDescription()), pathParameter.getRequired()));
    }

    private String getDefaultValue(AbstractSerializableParameter parameter) {
        if (parameter.getDefault() != null) {
            return getDefaultStringValue(parameter.getDefault().toString());
        }
        return StringUtils.EMPTY;
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
                getDefaultStringValue(headerParameter.getDescription()),
                StringUtils.EMPTY, parameter.getRequired());
    }

    private HttpResponse parseResponse(Operation operation, Map<String, Response> responses) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.getBody().setKvs(new ArrayList<>());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestTypeConstants.HTTP);
        msResponse.getBody().setType(getResponseBodyType(operation));
        // todo 状态码要调整？
        msResponse.setStatusCode(new ArrayList<>());
        Response response = null;
        String responseCode = null;
        if (responses != null && responses.size() > 0) {
            Map.Entry<String, Response> next = responses.entrySet().iterator().next();
            responseCode = next.getKey();
            response = next.getValue();
        }
        if (response != null && responseCode != null) {
            if (StringUtils.isNotBlank(response.getDescription())) {
                msResponse.getStatusCode().add(new KeyValue(responseCode, response.getDescription()));
            } else {
                msResponse.getStatusCode().add(new KeyValue(responseCode, responseCode));
            }
            if (response.getResponseSchema() != null) {
                parseResponseBody(response.getResponseSchema(), msResponse.getBody());
                msResponse.getBody().setFormat("JSON-SCHEMA");
                String body = parseSchema(response.getResponseSchema());
                if (StringUtils.isNotBlank(body)) {
                    msResponse.getBody().setRaw(body);
                }
            } else {
                String body = parseSchema(response.getResponseSchema());
                if (StringUtils.isNotBlank(body)) {
                    msResponse.getBody().setRaw(body);
                }
            }
            parseResponseHeader(response, msResponse.getHeaders());
        }

        return msResponse;
    }

    private void parseResponseBody(Model schema, Body body) {
        HashSet<String> refSet = new HashSet<>();
        body.setJsonSchema(parseJsonSchema(schema, refSet));
    }

    private JsonSchemaItem parseJsonSchema(Model schema, HashSet<String> refSet) {
        if (schema == null) return null;
        JsonSchemaItem item = new JsonSchemaItem();
        if (schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) schema;
            item.setType(PropertyConstant.ARRAY);
            item.setItems(new ArrayList<>());
            JsonSchemaItem proItem = parseProperty(arrayModel.getItems(), refSet);
            if (proItem != null) item.getItems().add(proItem);
        } else if (schema instanceof ModelImpl) {
            item.setType(PropertyConstant.OBJECT);
            ModelImpl model = (ModelImpl) schema;
            item.setProperties(parseNewSchemaProperties(model, refSet));
            if (model.getAdditionalProperties() != null) {
                item.setAdditionalProperties(parseProperty(model.getAdditionalProperties(), refSet));
            }
        } else if (schema instanceof AbstractModel) {
            AbstractModel abstractModel = (AbstractModel) schema;
            item.setType(PropertyConstant.OBJECT);
            item.setProperties(parseNewSchemaProperties(abstractModel, refSet));
        } else if (schema instanceof RefModel) {
            Model model = getRefModelType(schema, refSet);
            item.setType(PropertyConstant.OBJECT);
            item.setProperties(parseNewSchemaProperties(model, refSet));
        } else {
            return null;
        }

        return item;
    }

    private Map<String, JsonSchemaItem> parseNewSchemaProperties(Model schema, HashSet<String> refSet) {
        if (schema == null) return null;
        Map<String, Property> properties = schema.getProperties();
        if (MapUtils.isEmpty(properties)) return null;
        Map<String, JsonSchemaItem> JsonSchemaProperties = new LinkedHashMap<>();
        properties.forEach((key, value) -> {
            JsonSchemaItem item = new JsonSchemaItem();
            item.setDescription(schema.getDescription());
            JsonSchemaItem proItem = parseProperty(value, refSet);
            if (proItem != null) JsonSchemaProperties.put(key, proItem);
        });
        return JsonSchemaProperties;
    }


    private JsonSchemaItem parseProperty(Property property, HashSet<String> refSet) {
        JsonSchemaItem item = new JsonSchemaItem();
        item.setDescription(property.getDescription());
        if (property instanceof ObjectProperty) {
            ObjectProperty objectProperty = (ObjectProperty) property;
            item.setType(PropertyConstant.OBJECT);
            item.setProperties(parseSchemaProperties(objectProperty.getProperties(), refSet));
        } else if (property instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) property;
            handleArrayItemProperties(item, arrayProperty.getItems(), refSet);
        } else if (property instanceof RefProperty) {
            item.setType(PropertyConstant.OBJECT);
            handleRefProperties(item, property, refSet);
        } else {
            handleBaseProperties(item, property);
        }
        if (property.getExample() != null) {
            item.getMock().put(PropertyConstant.MOCK, property.getExample());
        } else {
            item.getMock().put(PropertyConstant.MOCK, StringUtils.EMPTY);
        }
        return item;
    }

    private void parseResponseHeader(Response response, List<KeyValue> msHeaders) {
        Map<String, Property> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> {
                msHeaders.add(new KeyValue(k, StringUtils.EMPTY, v.getDescription()));
            });
        }
    }

    private void parseRequestBodyParameters(Parameter parameter, Body body) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        if (body.getType().equals(Body.JSON_STR)) {
            body.setJsonSchema(parseSchema2JsonSchema(bodyParameter.getSchema()));
            body.setFormat("JSON-SCHEMA");
        } else if (body.getType().equals(Body.WWW_FROM) || body.getType().equals(Body.FORM_DATA)) {
            String parameterStr = parseSchema(bodyParameter.getSchema());
            if (StringUtils.isBlank(parameterStr)) {
                return;
            }
            if (bodyParameter.getSchema() instanceof ArrayModel) {
                JSONArray objects = JSONUtil.parseArray(parameterStr);
                if (objects.isEmpty()) {
                    return;
                }
                objects.forEach(item -> {
                    setBodyKvs(body, (JSONObject) item);
                });

            } else {
                JSONObject jsonObject = JSONUtil.parseObject(parameterStr);
                setBodyKvs(body, jsonObject);
            }

        } else {
            body.setRaw(parseSchema(bodyParameter.getSchema()));
        }
    }

    private static void setBodyKvs(Body body, JSONObject jsonObject) {
        Set<String> strings = jsonObject.keySet();
        List<KeyValue> kvs = new ArrayList<>();
        for (String key : strings) {
            KeyValue keyValue = new KeyValue(key, jsonObject.get(key).toString());
            kvs.add(keyValue);
        }
        body.setKvs(kvs);
    }

    private JsonSchemaItem parseSchema2JsonSchema(Model schema) {
        if (schema == null) return null;
        JsonSchemaItem item = new JsonSchemaItem();
        item.setDescription(schema.getDescription());
        // 引用模型
        if (schema instanceof RefModel) {
            HashSet<String> refSet = new HashSet<>();
            Model model = getRefModelType(schema, refSet);
            item.setType(PropertyConstant.OBJECT);
            if (model != null) {
                item.setProperties(parseSchemaProperties(model.getProperties(), refSet));
                if (((AbstractModel) model).getRequired() != null) {
                    item.setRequired(((AbstractModel) model).getRequired());
                }
            }
        } else if (schema instanceof ArrayModel) {
            //模型数组
            ArrayModel arrayModel = (ArrayModel) schema;
            HashSet<String> refSet = new HashSet<>();
            handleArrayItemProperties(item, arrayModel.getItems(), refSet);
        } else if (schema instanceof ModelImpl) {
            ModelImpl model = (ModelImpl) schema;
            item.setType(PropertyConstant.OBJECT);
            if (model != null) {
                item.setProperties(parseSchemaProperties(model.getProperties(), new HashSet<>()));
                if (model.getRequired() != null) {
                    item.setRequired(model.getRequired());
                }
                if (model.getAdditionalProperties() != null) {
                    item.setAdditionalProperties(parseProperty(model.getAdditionalProperties(), new HashSet<>()));
                }
            }
        } else if (schema instanceof AbstractModel) {
            AbstractModel abstractModel = (AbstractModel) schema;
            HashSet<String> refSet = new HashSet<>();
            item.setType(PropertyConstant.OBJECT);
            if (abstractModel != null)
                item.setProperties(parseSchemaProperties(abstractModel.getProperties(), refSet));
            if (abstractModel.getRequired() != null) {
                item.setRequired(abstractModel.getRequired());
            }
        }
        if (schema.getExample() != null) {
            item.getMock().put(PropertyConstant.MOCK, schema.getExample());
        } else {
            item.getMock().put(PropertyConstant.MOCK, StringUtils.EMPTY);
        }
        return item;
    }

    private Model getRefModelType(Model schema, HashSet<String> refSet) {
        String simpleRef;
        RefModel refModel = (RefModel) schema;
        String originalRef = refModel.getOriginalRef();
        if (refModel.getOriginalRef().split("/").length > 3) {
            simpleRef = originalRef.replace("#/definitions/", StringUtils.EMPTY);
        } else {
            simpleRef = refModel.getSimpleRef();
        }
        refSet.add(simpleRef);
        return this.definitions.get(simpleRef);
    }

    private Map<String, JsonSchemaItem> parseSchemaProperties(Map<String, Property> properties, HashSet<String> refSet) {
        if (MapUtils.isEmpty(properties)) return null;

        Map<String, JsonSchemaItem> JsonSchemaProperties = new LinkedHashMap<>();

        properties.forEach((key, value) -> {
            JsonSchemaItem item = new JsonSchemaItem();
            item.setDescription(value.getDescription());
            if (value instanceof ObjectProperty) {
                ObjectProperty objectProperty = (ObjectProperty) value;
                item.setType(PropertyConstant.OBJECT);
                item.setProperties(parseSchemaProperties(objectProperty.getProperties(), refSet));
            } else if (value instanceof ArrayProperty) {
                ArrayProperty arrayProperty = (ArrayProperty) value;
                handleArrayItemProperties(item, arrayProperty.getItems(), refSet);
            } else if (value instanceof RefProperty) {
                item.setType(PropertyConstant.OBJECT);
                handleRefProperties(item, value, refSet);
            } else {
                handleBaseProperties(item, value);
            }
            if (value.getExample() != null) {
                item.getMock().put(PropertyConstant.MOCK, value.getExample());
            } else {
                item.getMock().put(PropertyConstant.MOCK, StringUtils.EMPTY);
            }
            JsonSchemaProperties.put(key, item);
        });

        return JsonSchemaProperties;
    }

    private void handleArrayItemProperties(JsonSchemaItem item, Property value, HashSet<String> refSet) {
        if (value == null) return;
        item.setType(PropertyConstant.ARRAY);
        JsonSchemaItem subItem = new JsonSchemaItem(PropertyConstant.OBJECT);
        if (value instanceof RefProperty) {
            subItem.setType(PropertyConstant.OBJECT);
            handleRefProperties(subItem, value, refSet);
        } else if (value instanceof ObjectProperty) {
            subItem.setType(PropertyConstant.OBJECT);
            subItem.setProperties(parseSchemaProperties(((ObjectProperty) value).getProperties(), refSet));
        } else {
            handleBaseProperties(subItem, value);
        }
        item.getItems().add(subItem);
    }

    private void handleBaseProperties(JsonSchemaItem item, Property value) {
        if (value instanceof StringProperty || value instanceof DateProperty || value instanceof DateTimeProperty) {
            item.setType(PropertyConstant.STRING);
        } else if (value instanceof IntegerProperty || value instanceof BaseIntegerProperty) {
            item.setType(PropertyConstant.INTEGER);
        } else if (value instanceof BooleanProperty) {
            item.setType(PropertyConstant.BOOLEAN);
        } else if (value instanceof LongProperty || value instanceof FloatProperty
                || value instanceof DecimalProperty || value instanceof DoubleProperty) {
            item.setType(PropertyConstant.NUMBER);
        } else {
            item.setType(PropertyConstant.STRING);
        }
    }

    private void handleRefProperties(JsonSchemaItem item, Property value, HashSet<String> refSet) {
        RefProperty refProperty = (RefProperty) value;
        String simpleRef = refProperty.getSimpleRef();
        if (isContainRef(refSet, simpleRef)) {
            return;
        }
        Model model = this.definitions.get(simpleRef);
        if (model != null) {
            item.setProperties(parseSchemaProperties(model.getProperties(), refSet));
            if (((AbstractModel) model).getRequired() != null) {
                item.setRequired(((AbstractModel) model).getRequired());
            }
        }
    }

    private boolean isContainRef(HashSet<String> refSet, String ref) {
        if (refSet.contains(ref)) {
            //避免嵌套死循环
            return true;
        } else {
            refSet.add(ref);
            return false;
        }
    }


    private String parseSchema(Model schema) {
        // 引用模型
        if (schema instanceof RefModel) {
            HashSet<String> refSet = new HashSet<>();
            Model model = getRefModelType(schema, refSet);
            if (model != null) {
                JSONObject bodyParameters = getBodyParameters(model.getProperties(), refSet);
                return bodyParameters.toString();
            }
        } else if (schema instanceof ArrayModel) {
            //模型数组
            ArrayModel arrayModel = (ArrayModel) schema;
            Property items = arrayModel.getItems();
            List<JSONObject> propertyList = new LinkedList<>();
            if (items instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) items;
                String simpleRef = refProperty.getSimpleRef();
                HashSet<String> refSet = new HashSet<>();
                refSet.add(simpleRef);
                Model model = definitions.get(simpleRef);
                if (model != null) {
                    propertyList.add(getBodyParameters(model.getProperties(), refSet));
                } else {
                    propertyList.add(new JSONObject(true));
                }
            }
            return propertyList.toString();
        } else if (schema instanceof ModelImpl) {
            ModelImpl model = (ModelImpl) schema;
            Map<String, Property> properties = model.getProperties();
            if (model != null && properties != null) {
                JSONObject bodyParameters = getBodyParameters(properties, new HashSet<>());
                return bodyParameters.toString();
            }
        }
        return StringUtils.EMPTY;
    }

    private JSONObject getBodyParameters(Map<String, Property> properties, HashSet<String> refSet) {
        JSONObject jsonObject = new JSONObject(true);
        if (properties != null) {
            properties.forEach((key, value) -> {
                if (value instanceof ObjectProperty) {
                    ObjectProperty objectProperty = (ObjectProperty) value;
                    jsonObject.put(key, getBodyParameters(objectProperty.getProperties(), refSet));
                } else if (value instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) value;
                    Property items = arrayProperty.getItems();
                    if (items instanceof RefProperty) {
                        RefProperty refProperty = (RefProperty) items;
                        String simpleRef = refProperty.getSimpleRef();
                        if (refSet.contains(simpleRef)) {
                            //避免嵌套死循环
                            jsonObject.put(key, new JSONArray());
                            return;
                        }
                        refSet.add(simpleRef);
                        Model model = this.definitions.get(simpleRef);
                        List<JSONObject> propertyList = new LinkedList<>();
                        if (model != null) {
                            propertyList.add(getBodyParameters(model.getProperties(), refSet));
                        } else {
                            propertyList.add(new JSONObject(true));
                        }
                        jsonObject.put(key, propertyList);
                    } else if (items instanceof ObjectProperty) {
                        List<JSONObject> propertyList = new LinkedList<>();
                        if (items != null) {
                            propertyList.add(getBodyParameters(((ObjectProperty) items).getProperties(), refSet));
                        }
                        jsonObject.put(key, propertyList);
                    } else {
                        jsonObject.put(key, new ArrayList<>());
                    }
                } else if (value instanceof RefProperty) {
                    RefProperty refProperty = (RefProperty) value;
                    String simpleRef = refProperty.getSimpleRef();
                    if (refSet.contains(simpleRef)) {
                        //避免嵌套死循环
                        jsonObject.put(key, new JSONArray());
                        return;
                    }
                    refSet.add(simpleRef);
                    Model model = definitions.get(simpleRef);
                    if (model != null) {
                        jsonObject.put(key, getBodyParameters(model.getProperties(), refSet));
                    }
                } else {
                    jsonObject.put(key, getDefaultValueByPropertyType(value));
                }
            });
        }
        return jsonObject;
    }

    private Object getDefaultValueByPropertyType(Property value) {
        if (value instanceof LongProperty || value instanceof IntegerProperty
                || value instanceof BaseIntegerProperty) {
            return 0;
        } else if (value instanceof FloatProperty || value instanceof DoubleProperty
                || value instanceof DecimalProperty) {
            return 0.0;
        } else {// todo 其他类型?
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseFormDataParameters(FormParameter parameter, Body body) {
        List<KeyValue> keyValues = Optional.ofNullable(body.getKvs()).orElse(new ArrayList<>());
        KeyValue kv = new KeyValue(parameter.getName(), getDefaultValue(parameter), getDefaultStringValue(parameter.getDescription()), parameter.getRequired());
        if (StringUtils.equals(parameter.getType(), "file")) {
            kv.setType("file");
        }
        keyValues.add(kv);
        body.setKvs(keyValues);
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), getDefaultValue(queryParameter), getDefaultStringValue(queryParameter.getDescription()), queryParameter.getRequired()));
    }
}
