package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.SwaggerParameterType;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

public class Swagger2Parser extends ApiImportAbstractParser {

    private Map<String, Model> definitions = null;

    @Override
    public ApiDefinitionImport parseApi(InputStream source, ApiTestImportRequest request) {
        Swagger swagger;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            swagger = new SwaggerParser().read(request.getSwaggerUrl());
        } else {
            swagger = new SwaggerParser().readWithInfo(getApiTestStr(source)).getSwagger();
        }
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        definitionImport.setData(parseRequests(swagger));
        return definitionImport;
    }

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {
        return null;
    }

    private List<ApiDefinitionResult> parseRequests(Swagger swagger) {
        List<ApiDefinitionResult> results = new LinkedList<>();
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();

        this.definitions = swagger.getDefinitions();

        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);

                ApiDefinitionResult apiDefinition = buildApiDefinition(operation, pathName, method.name());
                MsHTTPSamplerProxy request = buildRequest(operation, pathName, method.name());
                parseParameters(operation, request);
                apiDefinition.setRequest(JSON.toJSONString(request));
                results.add(apiDefinition);


//                List<String> tags = operation.getTags();
//                if (tags != null) {
//                    tags.forEach(tag -> {
//                        Scenario scenario = Optional.ofNullable(scenarioMap.get(tag)).orElse(new Scenario());
//                        List<Request> requests = Optional.ofNullable(scenario.getRequests()).orElse(new ArrayList<>());
//                        requests.add(request);
//                        scenario.setRequests(requests);
//                        scenario.setName(tag);
//                        scenarioMap.put(tag, scenario);
//                    });
//                } else {
//                    Scenario scenario = Optional.ofNullable(scenarioMap.get("default")).orElse(new Scenario());
//                    List<Request> requests = Optional.ofNullable(scenario.getRequests()).orElse(new ArrayList<>());
//                    requests.add(request);
//                    scenario.setRequests(requests);
//                    scenarioMap.put("default", scenario);
//                }

            }
        }

        this.definitions = null;

        return results;
    }

    private ApiDefinitionResult buildApiDefinition(Operation operation, String path, String method) {
        ApiDefinitionResult apiDefinition = new ApiDefinitionResult();
        if (StringUtils.isNotBlank(operation.getSummary())) {
            apiDefinition.setName(operation.getSummary());
        } else {
            apiDefinition.setName(operation.getOperationId());
        }
        apiDefinition.setPath(path);
        apiDefinition.setProtocol(RequestType.HTTP);
        apiDefinition.setMethod(method);
        return apiDefinition;
    }
    private MsHTTPSamplerProxy buildRequest(Operation operation, String path, String method) {
        MsHTTPSamplerProxy request = new MsHTTPSamplerProxy();
        if (StringUtils.isNotBlank(operation.getSummary())) {
            request.setName(operation.getSummary());
        } else {
            request.setName(operation.getOperationId());
        }
        request.setPath(path);
        request.setMethod(method);
        request.setProtocol(RequestType.HTTP);
        return request;
    }

    private void parseParameters(Operation operation, MsHTTPSamplerProxy request) {

        List<Parameter> parameters = operation.getParameters();
        request.setId(UUID.randomUUID().toString());
        request.setHeaders(new ArrayList<>());
        request.setArguments(new ArrayList<>());
        request.setRest(new ArrayList<>());
        request.setBody(new Body());
        request.getBody().setType(getBodyType(operation));

        // todo 路径变量 {xxx} 是否要转换

        for (Parameter parameter : parameters) {
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
                    parseBodyParameters(parameter, request.getBody());
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


//        List<String> responseContentTypes = operation.getProduces();
    }

    private String getBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getConsumes())) {
            return Body.RAW;
        }
        String contentType = operation.getConsumes().get(0);
        String bodyType = "";
        switch (contentType) {
            case "application/x-www-form-urlencoded":
                bodyType = Body.WWW_FROM;
                break;
            case "multipart/form-data":
                bodyType = Body.FORM_DATA;
                break;
            case "application/json":
                bodyType = Body.JSON;
                break;
            case "application/xml":
                bodyType = Body.XML;
                break;
//            case "": //todo binary 啥类型
//                bodyType = Body.BINARY;
//                break;
            default:
                bodyType = Body.RAW;
        }
        return bodyType;
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(), "", getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(), "", getDefaultStringValue(cookieParameter.getDescription()));
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(), "", getDefaultStringValue(headerParameter.getDescription()));
    }

    private void parseBodyParameters(Parameter parameter, Body body) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        Model schema = bodyParameter.getSchema();

        // 引用模型
        if (schema instanceof RefModel) {
            String simpleRef = "";
            RefModel refModel = (RefModel) bodyParameter.getSchema();
            String originalRef = refModel.getOriginalRef();
            if (refModel.getOriginalRef().split("/").length > 3) {
                simpleRef = originalRef.replace("#/definitions/", "");
            } else {
                simpleRef = refModel.getSimpleRef();
            }
            Model model = this.definitions.get(simpleRef);
            HashSet<String> refSet = new HashSet<>();
            refSet.add(simpleRef);
            if (model != null) {
                JSONObject bodyParameters = getBodyParameters(model.getProperties(), refSet);
                body.setRaw(bodyParameters.toJSONString());
            }
        } else if (schema instanceof ArrayModel) {
            //模型数组
            ArrayModel arrayModel = (ArrayModel) bodyParameter.getSchema();
            Property items = arrayModel.getItems();
            if (items instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) items;
                String simpleRef = refProperty.getSimpleRef();
                HashSet<String> refSet = new HashSet<>();
                refSet.add(simpleRef);
                Model model = definitions.get(simpleRef);
                JSONArray propertyList = new JSONArray();
                propertyList.add(getBodyParameters(model.getProperties(), refSet));
                body.setRaw(propertyList.toString());
            }
        }
        body.setFormat("json");
    }

    private JSONObject getBodyParameters(Map<String, Property> properties, HashSet<String> refSet) {
        JSONObject jsonObject = new JSONObject();
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
                        JSONArray propertyList = new JSONArray();
                        propertyList.add(getBodyParameters(model.getProperties(), refSet));
                        jsonObject.put(key, propertyList);
                    } else {
                        jsonObject.put(key, new ArrayList<>());
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
        KeyValue kv = new KeyValue(parameter.getName(), "", getDefaultStringValue(parameter.getDescription()));
        if (StringUtils.equals(parameter.getType(), "file") ) {
            kv.setType("file");
        }
        keyValues.add(kv);
        body.setKvs(keyValues);
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), "", getDefaultStringValue(queryParameter.getDescription())));
    }
}
