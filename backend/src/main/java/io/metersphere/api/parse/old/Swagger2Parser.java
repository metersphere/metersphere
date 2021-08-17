package io.metersphere.api.parse.old;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.api.dto.definition.request.configurations.MsHeaderManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.request.HttpRequest;
import io.metersphere.api.dto.scenario.request.Request;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.SwaggerParameterType;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class Swagger2Parser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {
        Swagger swagger;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            swagger = new SwaggerParser().read(request.getSwaggerUrl());
        } else {
            swagger = new SwaggerParser().readWithInfo(getApiTestStr(source)).getSwagger();
        }
        ApiImport apiImport = new ApiImport();
        apiImport.setScenarios(parseRequests(swagger));
        apiImport.getScenarios().forEach(scenario -> scenario.setEnvironmentId(request.getEnvironmentId()));
        return apiImport;
    }

    @Override
    public ApiDefinitionImport parseApi(InputStream source, ApiTestImportRequest request) {
        ApiImport apiImport = this.parse(source, request);
        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        definitionImport.setData(parseSwagger(apiImport));
        return definitionImport;
    }

    private List<ApiDefinitionWithBLOBs> parseSwagger(ApiImport apiImport) {
        List<ApiDefinitionWithBLOBs> results = new LinkedList<>();
        apiImport.getScenarios().forEach(item -> {
            item.getRequests().forEach(childItem -> {
                if (childItem instanceof HttpRequest) {
                    HttpRequest res = (HttpRequest) childItem;
                    ApiDefinitionWithBLOBs request = new ApiDefinitionWithBLOBs();
                    request.setName(res.getName());
                    request.setPath(res.getPath());
                    request.setMethod(res.getMethod());
                    request.setProtocol(RequestType.HTTP);
                    MsHTTPSamplerProxy requestElement = new MsHTTPSamplerProxy();
                    requestElement.setName(res.getName() + "Postman MHTTPSamplerProxy");
                    requestElement.setBody(res.getBody());
                    requestElement.setArguments(res.getParameters());
                    requestElement.setProtocol(RequestType.HTTP);
                    requestElement.setPath(res.getPath());
                    requestElement.setMethod(res.getMethod());
                    requestElement.setId(UUID.randomUUID().toString());
                    requestElement.setRest(new ArrayList<KeyValue>());
                    MsHeaderManager headerManager = new MsHeaderManager();
                    headerManager.setId(UUID.randomUUID().toString());
                    headerManager.setName(res.getName() + "Postman MsHeaderManager");
                    headerManager.setHeaders(res.getHeaders());
//                    HashTree tree = new HashTree();
//                    tree.save(headerManager);
                    LinkedList<MsTestElement> list = new LinkedList<>();
                    list.add(headerManager);
                    requestElement.setHashTree(list);
                    request.setRequest(JSON.toJSONString(requestElement));
                    results.add(request);
                }

            });
        });
        return results;
    }

    private List<Scenario> parseRequests(Swagger swagger) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();
        Map<String, Scenario> scenarioMap = new HashMap<>();
        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                HttpRequest request = new HttpRequest();
                if (StringUtils.isNotBlank(operation.getSummary())) {
                    request.setName(operation.getSummary());
                } else {
                    request.setName(operation.getOperationId());
                }
                request.setPath(pathName);
                request.setUseEnvironment(true);
                request.setMethod(method.name());
                parseParameters(operation, swagger.getDefinitions(), request);
                List<String> tags = operation.getTags();
                if (tags != null) {
                    tags.forEach(tag -> {
                        Scenario scenario = Optional.ofNullable(scenarioMap.get(tag)).orElse(new Scenario());
                        List<Request> requests = Optional.ofNullable(scenario.getRequests()).orElse(new ArrayList<>());
                        requests.add(request);
                        scenario.setRequests(requests);
                        scenario.setName(tag);
                        scenarioMap.put(tag, scenario);
                    });
                } else {
                    Scenario scenario = Optional.ofNullable(scenarioMap.get("default")).orElse(new Scenario());
                    List<Request> requests = Optional.ofNullable(scenario.getRequests()).orElse(new ArrayList<>());
                    requests.add(request);
                    scenario.setRequests(requests);
                    scenarioMap.put("default", scenario);
                }

            }
        }
        return new ArrayList<>(scenarioMap.values());
    }

    private void parseParameters(Operation operation, Map<String, Model> definitions, HttpRequest request) {

        List<Parameter> parameters = operation.getParameters();

        for (Parameter parameter : parameters) {
            switch (parameter.getIn()) {
//                case SwaggerParameterType.PATH:
//                   parsePathParameters(parameter, request);
//                   break;
                case SwaggerParameterType.QUERY:
                    parseQueryParameters(parameter, request);
                    break;
                case SwaggerParameterType.FORM_DATA:
                    parseFormDataParameters(parameter, request);
                    break;
                case SwaggerParameterType.BODY:
                    parseBodyParameters(parameter, request, definitions);
                    break;
                case SwaggerParameterType.HEADER:
                    parseHeaderParameters(parameter, request);
                    break;
                case SwaggerParameterType.COOKIE:
                    parseCookieParameters(parameter, request);
                    break;
//                case SwaggerParameterType.FILE:
//                    parsePathParameters(parameter, request);
//                    break;
            }
        }
    }

    private void parseCookieParameters(Parameter parameter, HttpRequest request) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(request, cookieParameter.getName(), cookieParameter.getDescription());
    }

    private void parseHeaderParameters(Parameter parameter, HttpRequest request) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(request, headerParameter.getName(), headerParameter.getDescription());
    }

    private void parseBodyParameters(Parameter parameter, HttpRequest request, Map<String, Model> definitions) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        Body body = Optional.ofNullable(request.getBody()).orElse(new Body());
        body.setType(MsRequestBodyType.RAW.value());
        Model schema = bodyParameter.getSchema();

        if (schema instanceof RefModel) {
            String simpleRef = "";
            RefModel refModel = (RefModel) bodyParameter.getSchema();
            String originalRef = refModel.getOriginalRef();
            if (refModel.getOriginalRef().split("/").length > 3) {
                simpleRef = originalRef.replace("#/definitions/", "");
            } else {
                simpleRef = refModel.getSimpleRef();
            }
            Model model = definitions.get(simpleRef);
            HashSet<String> refSet = new HashSet<>();
            refSet.add(simpleRef);
            if (model != null) {
                JSONObject bodyParameters = getBodyJSONObjectParameters(model.getProperties(), definitions, refSet);
                body.setRaw(bodyParameters.toJSONString());
            }
        } else if (schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) bodyParameter.getSchema();
            Property items = arrayModel.getItems();
            if (items instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) items;
                String simpleRef = refProperty.getSimpleRef();
                HashSet<String> refSet = new HashSet<>();
                refSet.add(simpleRef);
                Model model = definitions.get(simpleRef);
                JSONArray propertyList = new JSONArray();
                propertyList.add(getBodyJSONObjectParameters(model.getProperties(), definitions, refSet));
                body.setRaw(propertyList.toString());
            }
        }
        request.setBody(body);
        body.setFormat("json");
    }

    private JSONObject getBodyJSONObjectParameters(Map<String, Property> properties, Map<String, Model> definitions, HashSet<String> refSet) {
        JSONObject jsonObject = new JSONObject();
        if (properties != null) {
            properties.forEach((key, value) -> {
                if (value instanceof ObjectProperty) {
                    ObjectProperty objectProperty = (ObjectProperty) value;
                    jsonObject.put(key, getBodyJSONObjectParameters(objectProperty.getProperties(), definitions, refSet));
                } else if (value instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) value;
                    Property items = arrayProperty.getItems();
                    if (items instanceof RefProperty) {
                        RefProperty refProperty = (RefProperty) items;
                        String simpleRef = refProperty.getSimpleRef();
                        if (refSet.contains(simpleRef)) {
                            jsonObject.put(key, new JSONArray());
                            return;
                        }
                        refSet.add(simpleRef);
                        Model model = definitions.get(simpleRef);
                        JSONArray propertyList = new JSONArray();
                        propertyList.add(getBodyJSONObjectParameters(model.getProperties(), definitions, refSet));
                        jsonObject.put(key, propertyList);
                    } else {
                        jsonObject.put(key, new ArrayList<>());
                    }
                } else {
                    jsonObject.put(key, Optional.ofNullable(value.getDescription()).orElse(""));
                }
            });
        }
        return jsonObject;
    }

    private void parseFormDataParameters(Parameter parameter, HttpRequest request) {
        Body body = Optional.ofNullable(request.getBody()).orElse(new Body());
        body.setType(MsRequestBodyType.FORM_DATA.value());
        List<KeyValue> keyValues = Optional.ofNullable(body.getKvs()).orElse(new ArrayList<>());
        keyValues.add(new KeyValue(parameter.getName(), "", parameter.getDescription()));
        body.setKvs(keyValues);
        request.setBody(body);
    }

    private void parseQueryParameters(Parameter parameter, HttpRequest request) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        List<KeyValue> parameters = Optional.ofNullable(request.getParameters()).orElse(new ArrayList<>());
        parameters.add(new KeyValue(queryParameter.getName(), "", queryParameter.getDescription()));
        request.setParameters(parameters);
    }
}
