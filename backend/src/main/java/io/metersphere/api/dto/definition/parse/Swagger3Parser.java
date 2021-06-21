package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiModuleDTO;
import io.metersphere.api.dto.definition.SwaggerApiExportResult;
import io.metersphere.api.dto.definition.parse.swagger.*;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.JSONSchemaGenerator;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
        SwaggerParseResult result;
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            result = new OpenAPIParser().readLocation(request.getSwaggerUrl(), null, null);
        } else {
            result = new OpenAPIParser().readContents(sourceStr, null, null);
        }

        if (result == null) {
            MSException.throwException("解析失败，请确认选择的是 swagger 格式！");
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

    private List<ApiDefinitionWithBLOBs> parseRequests(OpenAPI openAPI, ApiTestImportRequest importRequest) {
        Paths paths = openAPI.getPaths();

        Set<String> pathNames = paths.keySet();

        this.components = openAPI.getComponents();

        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        ApiModule selectModule = null;
        String selectModulePath = null;
        if (StringUtils.isNotBlank(importRequest.getModuleId())) {
            selectModule = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
            if (selectModule != null) {
                selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
            }
        }

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
                    ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method,importRequest);
                    parseParameters(operation, request);
                    parseRequestBody(operation.getRequestBody(), request.getBody());
                    addBodyHeader(request);
                    if(request.getBody().getKvs().size() > 1 && request.getBody().getKvs().get(0).getName() == null) {
                        request.getBody().getKvs().remove(0);
                    }   //  有数据的话，去掉 Kvs 里初始化的第一个全 null 的数据，否则有空行
                    apiDefinition.setRequest(JSON.toJSONString(request));
                    apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation.getResponses())));
                    buildModule(selectModule, apiDefinition, operation.getTags(), selectModulePath);
                    results.add(apiDefinition);
                }
            }
        }

        return results;
    }

    private ApiDefinitionWithBLOBs buildApiDefinition(String id, Operation operation, String path, String method,ApiTestImportRequest importRequest) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        return buildApiDefinition(id, name, path, method,importRequest);
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
        rests.add(new KeyValue(pathParameter.getName(), "", getDefaultStringValue(parameter.getDescription())));
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(), "", getDefaultStringValue(cookieParameter.getDescription()), parameter.getRequired());
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(), "", getDefaultStringValue(headerParameter.getDescription()), "", parameter.getRequired());
    }

    private HttpResponse parseResponse(ApiResponses responses) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestType.HTTP);
        // todo 状态码要调整？
        msResponse.setStatusCode(new ArrayList<>());
        if (responses != null) {
            responses.forEach((responseCode, response) -> {
                msResponse.getStatusCode().add(new KeyValue(responseCode, responseCode));
                parseResponseHeader(response, msResponse.getHeaders());
                parseResponseBody(response, msResponse.getBody());
            });
        }
        return msResponse;
    }

    private void parseResponseHeader(ApiResponse response, List<KeyValue> msHeaders) {
        Map<String, Header> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> {
                msHeaders.add(new KeyValue(k, "", v.getDescription()));
            });
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
        String contentType = "";
        MediaType mediaType = content.get(contentType);
        if (mediaType == null) {
            Set<String> contentTypes = content.keySet();
            if(contentTypes.size() == 0) {  //  防止空指针
                return;
            }
            contentType = contentTypes.iterator().next();
            if (StringUtils.isBlank(contentType)) {
                return;
            }
            mediaType = content.get(contentType);
        } else {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
        }

        Set<String> refSet = new HashSet<>();
        Map<String, Schema> infoMap = new HashMap();
        Schema schema = mediaType.getSchema();
        Object bodyData = parseSchema(schema, refSet, infoMap);

        if (bodyData == null) {
            return;
        }

        body.setType(getBodyType(contentType));

        if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)) {
            body.setRaw(bodyData.toString());
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_XML_VALUE)) {
            body.setRaw(parseXmlBody(schema, bodyData));
        } else if (StringUtils.equals(contentType, org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            parseKvBody(schema, body, bodyData, infoMap);
        } else {
            body.setRaw(bodyData.toString());
        }
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
                if(body.getKvs() == null) {  //  防止空指针
                    body.setKvs(new ArrayList<>());
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
            if (body != null) {
                body.getKvs().add(kv);
            }
        }
    }

    private String parseXmlBody(Schema schema, Object data) {
        if (data instanceof JSONObject) {
            return XMLUtils.jsonToXmlStr((JSONObject) data);
        } else {
            JSONObject object = new JSONObject();
            object.put(schema.getName(), getDefaultValueByPropertyType(schema));
            return XMLUtils.jsonToXmlStr(object);
        }
    }

    private Schema getModelByRef(String ref) {
        if (StringUtils.isBlank(ref)) {
            return null;
        }
        if (ref.split("/").length > 3) {
            ref = ref.replace("#/components/schemas/", "");
        }
        return this.components.getSchemas().get(ref);
    }

    private Object parseSchema(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
        if (schema == null) {
            return new JSONObject();
        }
        infoMap.put(schema.getName(), schema);
        if (StringUtils.isNotBlank(schema.get$ref())) {
            if (refSet.contains(schema.get$ref())) {
                return new JSONObject();
            }
            refSet.add(schema.get$ref());
            Object propertiesResult = parseSchemaProperties(getModelByRef(schema.get$ref()), refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        } else if (schema instanceof ArraySchema) {
            JSONArray jsonArray = new JSONArray();
            Schema items = ((ArraySchema) schema).getItems();
            parseSchema(items, refSet, infoMap);
            jsonArray.add(parseSchema(items, refSet, infoMap));
            return jsonArray;
        } else if (schema instanceof BinarySchema) {
            return getDefaultValueByPropertyType(schema);
        } else {
            if(schema.getType() != null) {  //  特判属性不是对象的情况，直接将基本类型赋值进去
                if(StringUtils.equals(schema.getType(), "string")) {
                    Object exampleObj = schema.getExample();
                    String example = null;
                    if (exampleObj != null) {
                        example = exampleObj.toString();
                    }
                    return example == null ? "" : example;
                } else if(StringUtils.equals(schema.getType(), "boolean")) {
                    return schema.getExample();
                } else if(StringUtils.equals(schema.getType(), "double")) {
                    return schema.getExample();
                }
            }
            Object propertiesResult = parseSchemaProperties(schema, refSet, infoMap);
            return propertiesResult == null ? getDefaultValueByPropertyType(schema) : propertiesResult;
        }
    }

    private Object parseSchemaProperties(Schema schema, Set<String> refSet, Map<String, Schema> infoMap) {
        if (schema == null) {
            return null;
        }
        Map<String, Schema> properties = schema.getProperties();
        if (MapUtils.isEmpty(properties)) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        properties.forEach((key, value) -> {
            jsonObject.put(key,  parseSchema(value, refSet, infoMap));
        });
        return jsonObject;
    }

    private Object getDefaultValueByPropertyType(Schema value) {
        Object example = value.getExample();
        if (value instanceof IntegerSchema) {
            return example == null ? 0 : example;
        } else if (value instanceof NumberSchema) {
            return example == null ? 0.0 : example;
        } else {// todo 其他类型?
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), "", getDefaultStringValue(queryParameter.getDescription()), parameter.getRequired()));
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
    public SwaggerApiExportResult swagger3Export(List<ApiDefinitionWithBLOBs> apiDefinitionList) {
        SwaggerApiExportResult result = new SwaggerApiExportResult();

        result.setOpenapi("3.0.1");
        result.setInfo(new SwaggerInfo());
        result.setServers(new ArrayList<>());
        result.setTags(new ArrayList<>());
        result.setComponents(new JSONObject());
        result.setExternalDocs(new JSONObject());

        JSONObject paths = new JSONObject();
        for(ApiDefinitionWithBLOBs apiDefinition : apiDefinitionList) {
            SwaggerApiInfo swaggerApiInfo = new SwaggerApiInfo();   //  {tags:, summary:, description:, parameters:}
            swaggerApiInfo.setSummary(apiDefinition.getName());
            //  设置导入后的模块名 （根据 api 的 moduleID 查库获得所属模块，作为导出的模块名）
            ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
            String moduleName = "";
            if(apiDefinition.getModuleId() != null) {   //  module_id 可能为空
                ApiModuleDTO node = apiModuleService.getNode(apiDefinition.getModuleId());
                if (node != null) {
                    moduleName = node.getName();
                }
            }
            swaggerApiInfo.setTags(Arrays.asList(moduleName));
            //  设置请求体
            JSONObject requestObject = JSON.parseObject(apiDefinition.getRequest());    //  将api的request属性转换成JSON对象以便获得参数
            JSONObject requestBody = buildRequestBody(requestObject);
            swaggerApiInfo.setRequestBody(requestBody);
            //  设置响应体
            JSONObject responseObject = JSON.parseObject(apiDefinition.getResponse());
            swaggerApiInfo.setResponses(buildResponseBody(responseObject));
            //  设置请求参数列表
            List<JSONObject> paramsList = buildParameters(requestObject);
            swaggerApiInfo.setParameters(paramsList);
            JSONObject methodDetail = JSON.parseObject(JSON.toJSONString(swaggerApiInfo));
            if(paths.getJSONObject(apiDefinition.getPath()) == null) {
                paths.put(apiDefinition.getPath(), new JSONObject());
            }   //  一个路径下有多个发方法，如post，get，因此是一个 JSONObject 类型
            paths.getJSONObject(apiDefinition.getPath()).put(apiDefinition.getMethod().toLowerCase(), methodDetail);
        }
        result.setPaths(paths);
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
        for(String type : typeKeys) {
            JSONArray params = request.getJSONArray(type);  //  获得请求参数列表
            if(params != null) {
                for(int i = 0; i < params.size(); ++i) {
                    JSONObject param = params.getJSONObject(i); //  对于每个参数:
                    if(param.get("name") == null || StringUtils.isEmpty(((String) param.get("name")))) {
                        continue;
                    }   //  否则无参数的情况，可能多出一行空行
                    SwaggerParams swaggerParam = new SwaggerParams();
                    swaggerParam.setIn(typeMap.get(type));  //  利用 map，根据 request 的 key 设置对应的参数类型
                    swaggerParam.setDescription((String) param.get("description"));
                    swaggerParam.setName((String) param.get("name"));
                    swaggerParam.setRequired((boolean) param.get("required"));
                    //  请求头 value 没有导出
//                    JSONObject schema = new JSONObject();
//                    swaggerParam.setSchema(schema);
//                    if(type.equals("headers")) {
//                        schema.put("type", "string");
//                        schema.put("example", param.getString("value"));
//                        swaggerParam.setSchema(schema);
//                    }
                    paramsList.add(JSON.parseObject(JSON.toJSONString(swaggerParam)));
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
        if(requestBody == null)
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
        if(requestBody == null)
            return null;
        JSONObject schema = new JSONObject();
        schema.put("type", "array");
        JSONObject items = new JSONObject();

        if(requestBody.size() > 0) {
            Object example = requestBody.get(0);
            if(example instanceof JSONObject) {
                items.put("type", "object");
                items.put("properties", buildSchema((JSONObject) example));
            } else if(example instanceof java.lang.String) {
                items.put("type", "string");
            } else if(example instanceof java.lang.Integer) {
                items.put("type", "integer");
                items.put("format", "int64");
            } else if(example instanceof java.lang.Boolean) {
                items.put("type", "boolean");
            } else if(example instanceof java.math.BigDecimal) {
                items.put("type", "double");
            }
            else {    //  JSONOArray
                items.put("type", "array");
                JSONObject item = new JSONObject();
                if(((JSONArray) example).size() > 0) {
                    if(((JSONArray) example).get(0) instanceof JSONObject) {
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) example).get(0));
                    }
                }
                items.put("items", item);
            }
        }
        schema.put("items", items);
        return schema;
    }

    //  设置一个 json 对象的属性在 swagger 格式中的类型、值
    private JSONObject buildSchema(JSONObject requestBody) {
        JSONObject schema = new JSONObject();
        for(String key : requestBody.keySet()) {
            Object param = requestBody.get(key);
            JSONObject parsedParam = new JSONObject();
            if(param instanceof java.lang.String) {
                parsedParam.put("type", "string");
                parsedParam.put("example", param == null? "" : param);
            } else if(param instanceof java.lang.Integer) {
                parsedParam.put("type", "integer");
                parsedParam.put("format", "int64");
                parsedParam.put("example", param);
            } else if(param instanceof JSONObject) {
                parsedParam = buildRequestBodyJsonInfo((JSONObject) param);
            } else if(param instanceof java.lang.Boolean) {
                parsedParam.put("type", "boolean");
                parsedParam.put("example", param);
            } else if(param instanceof java.math.BigDecimal) {  //  double 类型会被 fastJson 转换为 BigDecimal
                parsedParam.put("type", "double");
                parsedParam.put("example", param);
            } else {    //  JSONOArray
                parsedParam.put("type", "array");
                JSONObject item = new JSONObject();
                if (param == null) {
                    param = new JSONArray();
                }
                if(((JSONArray) param).size() > 0) {
                    if(((JSONArray) param).get(0) instanceof JSONObject) {  ///
                        item = buildRequestBodyJsonInfo((JSONObject) ((JSONArray) param).get(0));
                    }
                }
                parsedParam.put("items", item);
            }
            schema.put(key, parsedParam);
        }
        return schema;
    }

    private JSONObject buildformDataSchema(JSONObject kvs) {
        JSONObject schema = new JSONObject();
        JSONObject properties = new JSONObject();
        for(String key : kvs.keySet()) {
            JSONObject property = new JSONObject();
            property.put("type", "string");
            property.put("example", kvs.getString(key));
            properties.put(key, property);
        }
        schema.put("properties", properties);
        return schema;
    }

    private JSONObject getformDataProperties(JSONArray requestBody) {
        JSONObject result = new JSONObject();
        for(Object item : requestBody) {
            if(item instanceof JSONObject) {
                String name = ((JSONObject) item).getString("name");
                String value = ((JSONObject) item).getString("value");
                if(name != null) {
                    result.put(name, value);
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
        JSONObject statusCodeInfo = new JSONObject();
        //  build 请求头
        JSONObject headers = new JSONObject();
        JSONArray headValueList = response.getJSONArray("headers");
        if(headValueList != null) {
            for(Object item : headValueList) {
                if(item instanceof JSONObject && ((JSONObject) item).getString("name") != null) {
                    JSONObject head = new JSONObject(), headSchema = new JSONObject();
                    head.put("description", "");
                    headSchema.put("type", "string");
                    head.put("schema", headSchema);
                    headers.put(((JSONObject) item).getString("name"), head);
                }
            }
        }
        statusCodeInfo.put("headers", headers);

        JSONArray statusCode = response.getJSONArray("statusCode");

        //  build 请求体
        if (statusCode == null || statusCode.size() < 1 || statusCode.getJSONObject(0).getString("name") == null) {
            return response;
        }
        statusCodeInfo.put("content", buildContent(response));
        statusCodeInfo.put("description", "");
        responseBody.put(((JSONObject) response.getJSONArray("statusCode").get(0)).getString("name"), statusCodeInfo);
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
        JSONObject body = respOrReq.getJSONObject("body");

        if(body != null) { //  将请求体转换成相应的格式导出
            String bodyType = body.getString("type");
            if(bodyType == null) {

            } else if(bodyType.equals("JSON")) {
                try {
                    if (StringUtils.equals(body.getString("format"), "JSON-SCHEMA")) {
                        String jsonSchema = JSONSchemaGenerator.getJson(body.getString("jsonSchema"));
                        try {
                            bodyInfo = buildRequestBodyJsonInfo(JSONObject.parseObject(jsonSchema));
                        } catch (Exception e) {
                            bodyInfo = buildRequestBodyJsonInfo(JSONObject.parseArray(jsonSchema));
                        }
                    } else {
                        try{    //  若请求体是一个 object
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
            } else if(bodyType.equals("XML")) {
                String xmlText = body.getString("raw");
                JSONObject xmlToJson = XMLUtils.XmlToJson(xmlText);
                bodyInfo = buildRequestBodyJsonInfo(xmlToJson);
            } else if(bodyType.equals("WWW_FORM") || bodyType.equals("Form Data") || bodyType.equals("BINARY")) {    //  key-value 类格式
                JSONObject formData = getformDataProperties(body.getJSONArray("kvs"));
                bodyInfo = buildformDataSchema(formData);
            }
        }

        String type = respOrReq.getJSONObject("body").getString("type");
        JSONObject content = new JSONObject();
        Object schema = bodyInfo;   //  请求体部分
        JSONObject typeName = new JSONObject();
        if (schema != null) {
            typeName.put("schema", schema);//schema.getJSONObject("properties").size() == 0? "" :
        }
        if (type != null && StringUtils.isNotBlank(type)) {
            content.put(typeMap.get(type), typeName);
        }
        return content;
    }
}
