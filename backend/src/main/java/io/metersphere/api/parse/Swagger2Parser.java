package io.metersphere.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Request;
import io.metersphere.api.dto.scenario.Scenario;
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
        Swagger swagger = null;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            swagger = new SwaggerParser().read(request.getSwaggerUrl());
        } else {
            swagger = new SwaggerParser().readWithInfo(getApiTestStr(source)).getSwagger();
        }
        ApiImport apiImport = new ApiImport();
        apiImport.setScenarios(parseRequests(swagger));
        apiImport.getScenarios().forEach(scenario -> {
            scenario.setEnvironmentId(request.getEnvironmentId());
        });
        return apiImport;
    }

    private List<Scenario> parseRequests(Swagger swagger) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();
        Map<String, Scenario> scenarioMap = new HashMap<>();
        List<Scenario> scenarios = new ArrayList<>();
        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                Request request = new Request();
                request.setName(operation.getOperationId());
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
                        scenario.setRequests(requests);scenario.setName(tag);
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
        scenarioMap.values().forEach(scenario -> {
            scenarios.add(scenario);
        });
        return scenarios;
    }

    private void parseParameters(Operation operation, Map<String, Model> definitions, Request request) {

        List<Parameter> parameters = operation.getParameters();

        for (Parameter parameter : parameters) {
            switch (parameter.getIn()){
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

    private void parseCookieParameters(Parameter parameter, Request request) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(request, cookieParameter.getName(), cookieParameter.getDescription());
    }

    private void parseHeaderParameters(Parameter parameter, Request request) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(request, headerParameter.getName(), headerParameter.getDescription());
    }

    private void parseBodyParameters(Parameter parameter, Request request, Map<String, Model> definitions) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        Body body = Optional.ofNullable(request.getBody()).orElse(new Body());
        body.setType(MsRequestBodyType.RAW.value());
        Model schema = bodyParameter.getSchema();

        if (schema instanceof RefModel) {
            RefModel refModel = (RefModel) bodyParameter.getSchema();
            Model model = definitions.get(refModel.getSimpleRef());
            JSONObject bodyParameters = getBodyJSONObjectParameters(model.getProperties(), definitions);
            body.setRaw(bodyParameters.toJSONString());
        } else if (schema instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) bodyParameter.getSchema();
            Property items = arrayModel.getItems();
            if (items instanceof RefProperty) {
                RefProperty refProperty =  (RefProperty) items;
                Model model = definitions.get(refProperty.getSimpleRef());
                JSONArray propertyList = new JSONArray();
                propertyList.add(getBodyJSONObjectParameters(model.getProperties(), definitions));
                body.setRaw(propertyList.toString());
            }
        }
        request.setBody(body);
        addContentType(request, "application/json");

    }

    private JSONObject getBodyJSONObjectParameters(Map<String, Property> properties, Map<String, Model> definitions) {
        JSONObject jsonObject = new JSONObject();
        properties.forEach((key, value) -> {
            if (value instanceof ObjectProperty) {
                ObjectProperty objectProperty = (ObjectProperty) value;
                jsonObject.put(key, getBodyJSONObjectParameters(objectProperty.getProperties(), definitions));
            } else if (value instanceof ArrayProperty)  {
                ArrayProperty arrayProperty = (ArrayProperty) value;
                Property items = arrayProperty.getItems();
                if (items instanceof RefProperty) {
                    RefProperty refProperty =  (RefProperty) items;
                    Model model = definitions.get(refProperty.getSimpleRef());
                    JSONArray propertyList = new JSONArray();
                    propertyList.add(getBodyJSONObjectParameters(model.getProperties(), definitions));
                    jsonObject.put(key, propertyList);
                } else {
                    jsonObject.put(key, new ArrayList<>());
                }
            } else {
                jsonObject.put(key, Optional.ofNullable(value.getDescription()).orElse(""));
            }
        });
        return jsonObject;
    }

    private void parseFormDataParameters(Parameter parameter, Request request) {
        Body body = Optional.ofNullable(request.getBody()).orElse(new Body());
        body.setType(MsRequestBodyType.FORM_DATA.value());
        List<KeyValue> keyValues = Optional.ofNullable(body.getKvs()).orElse(new ArrayList<>());
        keyValues.add(new KeyValue(parameter.getName(), "", parameter.getDescription()));
        body.setKvs(keyValues);
        request.setBody(body);
    }

    private void parseQueryParameters(Parameter parameter, Request request) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        List<KeyValue> parameters = Optional.ofNullable(request.getParameters()).orElse(new ArrayList<>());
        parameters.add(new KeyValue(queryParameter.getName(), "", queryParameter.getDescription()));
        request.setParameters(parameters);
    }
}
