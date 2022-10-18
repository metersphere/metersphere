package io.metersphere.commons.utils.mock;

import io.metersphere.api.dto.mock.ApiDefinitionResponseDTO;
import io.metersphere.api.dto.mock.MockConfigRequestParams;
import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.api.exec.generator.JSONSchemaGenerator;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.enums.MockParamConditionEnums;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author song.tianyang
 * @Date 2021/10/14 3:00 下午
 */
public class MockApiUtils {
    public static Map<String, String> getHttpRequestHeader(HttpServletRequest request) {
        Map<String, String> returnMap = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String headerValue = request.getHeader(header);
            returnMap.put(header, headerValue);
        }
        return returnMap;
    }

    public static boolean matchRequestHeader(JSONArray mockExpectHeaderArray, Map<String, String> requestHeaderMap) {
        Map<String, String> mockExpectHeaders = new HashMap<>();
        for (int i = 0; i < mockExpectHeaderArray.length(); i++) {
            JSONObject obj = mockExpectHeaderArray.optJSONObject(i);
            if (obj.has("name") && obj.has("value")) {
                mockExpectHeaders.put(obj.optString("name"), obj.optString("value"));
            }
        }
        if (MapUtils.isEmpty(requestHeaderMap) && MapUtils.isNotEmpty(mockExpectHeaders)) {
            return false;
        } else {
            for (Map.Entry<String, String> entry : mockExpectHeaders.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!requestHeaderMap.containsKey(key)) {
                    return false;
                } else {
                    if (!StringUtils.equals(value, requestHeaderMap.get(key))) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static Object getExpectBodyParams(JSONObject bodyObj) {
        Object returnJson = new JSONArray();
        try {
            String type = bodyObj.optString(PropertyConstant.TYPE);
            if (StringUtils.equalsIgnoreCase(type, "JSON")) {
                //判断是否是JsonSchema
                boolean isJsonSchema = false;
                if (bodyObj.has("format")) {
                    String foramtValue = String.valueOf(bodyObj.get("format"));
                    if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                        isJsonSchema = true;
                    }
                }

                String jsonString = StringUtils.EMPTY;
                if (isJsonSchema) {
                    if (bodyObj.has("jsonSchema")) {
                        String bodyRetunStr = bodyObj.optJSONObject("jsonSchema").toString();
                        jsonString = JSONSchemaGenerator.getJson(bodyRetunStr);
                    }
                } else {
                    if (bodyObj.has("raw")) {
                        jsonString = bodyObj.optString("raw");
                    }
                }
                JSONValidator jsonValidator = JSONValidator.from(jsonString);
                if (StringUtils.equalsIgnoreCase(PropertyConstant.ARRAY, jsonValidator.getType().name())) {
                    returnJson = JSONUtil.parseArray(jsonString);
                } else if (StringUtils.equalsIgnoreCase(PropertyConstant.OBJECT, jsonValidator.getType().name())) {
                    returnJson = JSONUtil.parseObject(jsonString);
                }

            } else if (StringUtils.equalsIgnoreCase(type, "XML")) {
                if (bodyObj.has("raw")) {
                    String xmlStr = bodyObj.optString("raw");
                    JSONObject matchObj = XMLUtil.xmlStringToJSONObject(xmlStr);
                    returnJson = matchObj;
                }
            } else if (StringUtils.equalsIgnoreCase(type, "Raw")) {
                if (bodyObj.has("raw")) {
                    String raw = bodyObj.optString("raw");
                    if (StringUtils.isNotEmpty(raw)) {
                        JSONObject rawObject = new JSONObject();
                        rawObject.put("raw", raw);
                        returnJson = rawObject;
                    }
                }
            } else if (StringUtils.equalsAnyIgnoreCase(type, "Form Data", "WWW_FORM")) {
                if (bodyObj.has("kvs")) {
                    JSONObject bodyParamArr = new JSONObject();
                    JSONArray kvsArr = bodyObj.optJSONArray("kvs");
                    for (int i = 0; i < kvsArr.length(); i++) {
                        JSONObject kv = kvsArr.optJSONObject(i);
                        if (kv.has("name")) {
                            String values = kv.optString("value");
                            if (StringUtils.isEmpty(values)) {
                                values = StringUtils.EMPTY;
                            } else {
                                try {
                                    values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                } catch (Exception e) {
                                }
                            }
                            bodyParamArr.put(kv.optString("name"), values);
                        }
                    }
                    returnJson = bodyParamArr;
                }
            }
        } catch (Exception e) {
        }


        return returnJson;
    }


    public static List<MockConfigRequestParams> getParamsByJSONArray(JSONArray array) {
        List<MockConfigRequestParams> requestParamsList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj.has("name") && obj.has("value")) {
                String condition = null;
                if (obj.has("rangeType")) {
                    condition = obj.optString("rangeType");
                }
                MockConfigRequestParams requestParams = new MockConfigRequestParams();
                requestParams.setKey(obj.optString("name"));
                requestParams.setValue(obj.optString("value"));
                requestParams.setCondition(parseCondition(condition));
                requestParamsList.add(requestParams);
            }
        }
        return requestParamsList;
    }

    public static ApiDefinitionResponseDTO getApiResponse(String response) {
        ApiDefinitionResponseDTO responseDTO = new ApiDefinitionResponseDTO();
        if (StringUtils.isNotEmpty(response)) {
            try {
                JSONObject respObj = JSONUtil.parseObject(response);
                if (respObj != null) {
                    if (respObj.has("body")) {
                        String returnStr = StringUtils.EMPTY;
                        JSONObject bodyObj = respObj.optJSONObject("body");
                        if (bodyObj.has(PropertyConstant.TYPE)) {
                            String type = bodyObj.optString(PropertyConstant.TYPE);
                            if (StringUtils.equals(type, "JSON")) {
                                //判断是否是JsonSchema
                                boolean isJsonSchema = false;
                                if (bodyObj.has("format")) {
                                    String foramtValue = String.valueOf(bodyObj.get("format"));
                                    if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                                        isJsonSchema = true;
                                    }
                                }
                                if (isJsonSchema && bodyObj.has("jsonSchema")) {
                                    String json = JSONSchemaGenerator.getJson(bodyObj.optJSONObject("jsonSchema").toString());
                                    if (StringUtils.isNotEmpty(json)) {
                                        returnStr = json;
                                    }
                                } else {
                                    if (bodyObj.has("raw")) {
                                        returnStr = bodyObj.optString("raw");
                                    }
                                }
                            } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                                if (bodyObj.has("raw")) {
                                    String raw = bodyObj.optString("raw");
                                    returnStr = raw;
                                }
                            } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                Map<String, String> paramMap = new LinkedHashMap<>();
                                if (bodyObj.has("kvs")) {
                                    JSONArray kvsArr = bodyObj.optJSONArray("kvs");
                                    for (int i = 0; i < kvsArr.length(); i++) {
                                        JSONObject kv = kvsArr.optJSONObject(i);
                                        if (kv.has("name")) {
                                            String values = kv.optString("value");
                                            if (StringUtils.isEmpty(values)) {
                                                values = StringUtils.EMPTY;
                                            } else {
                                                try {
                                                    values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                                } catch (Exception e) {
                                                }
                                            }
                                            paramMap.put(kv.optString("name"), values);
                                        }
                                    }
                                }
                                returnStr = JSON.toJSONString(paramMap);
                            }
                        }
                        responseDTO.setReturnData(returnStr);
                    }
                    if (respObj.has("statusCode")) {
                        JSONArray statusCodeArray = respObj.optJSONArray("statusCode");
                        int code = 200;
                        if (statusCodeArray != null) {
                            for (int i = 0; i < statusCodeArray.length(); i++) {
                                JSONObject object = statusCodeArray.optJSONObject(i);
                                if (object.has("name") && StringUtils.isNotEmpty(object.optString("name"))) {
                                    try {
                                        code = Integer.parseInt(object.optString("name"));
                                        break;
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
                                }
                            }
                        }
                        responseDTO.setReturnCode(code);
                    }
                    if (respObj.has("headers")) {
                        JSONArray jsonArray = respObj.optJSONArray("headers");
                        Map<String, String> headMap = new HashMap<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject headObj = jsonArray.optJSONObject(i);
                            if (headObj.has("name") && headObj.has("value") && headObj.has("enable")) {
                                boolean enable = headObj.getBoolean("enable");
                                if (enable) {
                                    headMap.put(headObj.optString("name"), headObj.optString("value"));
                                }
                            }
                        }
                        responseDTO.setHeaders(headMap);
                    }
                }
            } catch (Exception e) {
                MSException.throwException(e);
            }
        }
        return responseDTO;
    }

    public String getResultByResponseResult(String projectId, JSONObject bodyObj, String url, Map<String, String> headerMap, RequestMockParams requestMockParams, boolean useScript) {
        MockScriptEngineUtils scriptEngineUtils = new MockScriptEngineUtils();
        ScriptEngine scriptEngine = null;
        String scriptLanguage = "beanshell";
        String script = null;
        if (useScript) {
            if (bodyObj.has("scriptObject")) {
                try {
                    JSONObject scriptObj = bodyObj.optJSONObject("scriptObject");
                    scriptLanguage = scriptObj.optString("scriptLanguage");
                    script = scriptObj.optString("script");
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
            ScriptFilter.verify(scriptLanguage, "Mock后置脚本", script);
            scriptEngine = scriptEngineUtils.getBaseScriptEngine(projectId, scriptLanguage, url, headerMap, requestMockParams);
            if (StringUtils.isNotEmpty(script) && scriptEngine != null) {
                scriptEngineUtils.runScript(scriptEngine, script);
            }
        }

        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        if (requestMockParams == null) {
            requestMockParams = new RequestMockParams();
        }
        if (bodyObj == null && bodyObj == null) {
            return StringUtils.EMPTY;
        } else {
            String returnStr = StringUtils.EMPTY;
            if (bodyObj.has(PropertyConstant.TYPE)) {
                String type = bodyObj.optString(PropertyConstant.TYPE);
                if (StringUtils.equals(type, "JSON")) {
                    //判断是否是JsonSchema
                    boolean isJsonSchema = false;
                    if (bodyObj.has("format")) {
                        String foramtValue = String.valueOf(bodyObj.get("format"));
                        if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                            isJsonSchema = true;
                        }
                    }
                    if (isJsonSchema && bodyObj.has("jsonSchema")) {
                        String json = JSONSchemaGenerator.getJson(bodyObj.optJSONObject("jsonSchema").toString());
                        if (StringUtils.isNotEmpty(json)) {
                            returnStr = json;
                        }
                    } else {
                        if (bodyObj.has("raw")) {
                            returnStr = bodyObj.optString("raw");
                        }
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "Raw")) {
                    if (bodyObj.has("raw")) {
                        String raw = bodyObj.optString("raw");
                        returnStr = raw;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "XML")) {
                    if (bodyObj.has("xmlHeader")) {
                        String xmlHeader = bodyObj.optString("xmlHeader");
                        if (!StringUtils.startsWith(xmlHeader, "<?") && !StringUtils.endsWith(xmlHeader, "?>")) {
                            returnStr = "<?xml " + xmlHeader + "?>\r\n";
                        } else {
                            returnStr = xmlHeader;
                        }
                    }
                    if (bodyObj.has("xmlRaw")) {
                        String raw = bodyObj.optString("xmlRaw");
                        returnStr = returnStr + raw;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "fromApi")) {
                    if (bodyObj.has("apiRspRaw")) {
                        String raw = bodyObj.optString("apiRspRaw");
                        returnStr = raw;
                    }
                }
            }
            if (scriptEngine != null) {
                returnStr = scriptEngineUtils.parseReportString(scriptEngine, returnStr);
            }
            return returnStr;
        }
    }

    public static RequestMockParams getParams(String urlParams, String apiPath, JSONObject queryParamsObject, Object paramJson, boolean isPostRequest) {
        RequestMockParams returnParams = getGetParamMap(urlParams, apiPath, queryParamsObject, isPostRequest);
        if (paramJson != null) {
            if (paramJson instanceof JSONObject) {
                if (!((JSONObject) paramJson).keySet().isEmpty()) {
                    JSONArray bodyParams = returnParams.getBodyParams();
                    if (bodyParams == null) {
                        bodyParams = new JSONArray();
                        bodyParams.put(paramJson);
                    } else {
                        bodyParams.put(((JSONObject) paramJson));
                    }
                    returnParams.setBodyParams(bodyParams);
                }
            } else if (paramJson instanceof JSONArray) {
                JSONArray paramArray = (JSONArray) paramJson;
                if (paramArray != null) {
                    returnParams.setBodyParams(paramArray);
                }
            }
        }
        return returnParams;
    }

    public static JSONObject getParameterJsonObject(HttpServletRequest request) {
        JSONObject queryParamsObject = new JSONObject();
        Enumeration<String> paramNameItor = request.getParameterNames();
        while (paramNameItor.hasMoreElements()) {
            String key = paramNameItor.nextElement();
            String value = request.getParameter(key);
            queryParamsObject.put(key, value);
        }
        return queryParamsObject;
    }

    private static RequestMockParams getGetParamMap(String urlParams, String apiPath, JSONObject queryParamsObject, boolean isPostRequest) {
        RequestMockParams requestMockParams = new RequestMockParams();

        JSONObject urlParamsObject = getSendRestParamMapByIdAndUrl(apiPath, urlParams);

        requestMockParams.setRestParamsObj(urlParamsObject);
        requestMockParams.setQueryParamsObj(queryParamsObject);

        if (isPostRequest && !queryParamsObject.keySet().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(queryParamsObject);
            requestMockParams.setBodyParams(jsonArray);
        }
        return requestMockParams;
    }

    public static Object getPostParamMap(HttpServletRequest request) {
        if (StringUtils.startsWithIgnoreCase(request.getContentType(), "application/JSON")) {
            Object returnJson = null;
            try {
                String param = getRequestPostStr(request);
                if (StringUtils.isNotEmpty(param)) {
                    JSONValidator jsonValidator = JSONValidator.from(param);
                    if (StringUtils.equalsIgnoreCase(PropertyConstant.ARRAY, jsonValidator.getType().name())) {
                        returnJson = JSONUtil.parseArray(param);
                    } else if (StringUtils.equalsIgnoreCase(PropertyConstant.OBJECT, jsonValidator.getType().name())) {
                        returnJson = JSONUtil.parseObject(param);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
            return returnJson;
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), "text/xml")) {
            String xmlString = readXml(request);
            JSONObject object = XMLUtil.xmlStringToJSONObject(xmlString);
            return object;
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), "application/x-www-form-urlencoded")) {
            JSONObject object = new JSONObject();
            Enumeration<String> paramNameItor = request.getParameterNames();
            while (paramNameItor.hasMoreElements()) {
                String key = paramNameItor.nextElement();
                String value = request.getParameter(key);
                object.put(key, value);
            }
            return object;
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), "text/plain")) {
            JSONObject object = new JSONObject();
            String bodyParam = readBody(request);
            if (StringUtils.isNotEmpty(bodyParam)) {
                object.put("raw", bodyParam);
            }
            return object;

        } else {
            JSONObject object = new JSONObject();
            String bodyParam = readBody(request);
            if (StringUtils.isNotEmpty(bodyParam)) {
                object.put("raw", bodyParam);
            }
            return object;
        }
    }

    private static JSONObject getSendRestParamMapByIdAndUrl(String path, String urlParams) {
        JSONObject returnJson = new JSONObject();
        if (StringUtils.isNotEmpty(path)) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String[] pathArr = path.split("/");
            String[] sendParamArr = urlParams.split("/");

            //获取 url的<参数名-参数值>，通过匹配api的接口设置和实际发送的url
            for (int i = 0; i < pathArr.length; i++) {
                String param = pathArr[i];
                if (param.startsWith("{") && param.endsWith("}")) {
                    param = param.substring(1, param.length() - 1);
                    String value = StringUtils.EMPTY;
                    if (sendParamArr.length > i) {
                        value = sendParamArr[i];
                    }
                    returnJson.put(param, value);
                }
            }

        }
        return returnJson;
    }

    private static String readBody(HttpServletRequest request) {
        String result = StringUtils.EMPTY;
        try {
            InputStream inputStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inputStream.close();
            result = new String(outSteam.toByteArray(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return result;
    }

    /**
     * 描述:获取 post 请求内容
     * <pre>
     * 举例：
     * </pre>
     *
     * @param request
     * @return
     * @throws IOException
     */
    private static String getRequestPostStr(HttpServletRequest request) throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = StandardCharsets.UTF_8.name();
        }
        if (buffer == null) {
            return StringUtils.EMPTY;
        } else {
            return new String(buffer, charEncoding);
        }
    }

    private static String readXml(HttpServletRequest request) {
        String inputLine = null;
        // 接收到的数据
        StringBuffer recieveData = new StringBuffer();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    request.getInputStream(), StandardCharsets.UTF_8.name()));
            while ((inputLine = in.readLine()) != null) {
                recieveData.append(inputLine);
            }
        } catch (IOException e) {
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }

        return recieveData.toString();
    }

    /**
     * 描述:获取 post 请求的 byte[] 数组
     * <pre>
     * 举例：
     * </pre>
     *
     * @param request
     * @return
     * @throws IOException
     */
    private static byte[] getRequestPostBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    public static String parseCondition(String condition) {
        String returnCondition = MockParamConditionEnums.VALUE_EQUALS.name();
        if (StringUtils.isNotEmpty(condition)) {
            switch (condition) {
                case "value_not_eq":
                    returnCondition = MockParamConditionEnums.VALUE_NOT_EQUALS.name();
                    break;
                case "value_contain":
                    returnCondition = MockParamConditionEnums.VALUE_CONTAINS.name();
                    break;
                case "length_eq":
                    returnCondition = MockParamConditionEnums.LENGTH_EQUALS.name();
                    break;
                case "length_not_eq":
                    returnCondition = MockParamConditionEnums.LENGTH_NOT_EQUALS.name();
                    break;
                case "length_large_than":
                    returnCondition = MockParamConditionEnums.LENGTH_LARGE_THAN.name();
                    break;
                case "length_shot_than":
                    returnCondition = MockParamConditionEnums.LENGTH_SHOT_THAN.name();
                    break;
                case "regular_match":
                    returnCondition = MockParamConditionEnums.REGULAR_MATCH.name();
                    break;
                default:
                    returnCondition = MockParamConditionEnums.VALUE_EQUALS.name();
                    break;
            }
        }
        return returnCondition;
    }

    public static boolean checkParamsCompliance(JSONObject queryParamsObj, List<MockConfigRequestParams> mockConfigRequestParamList, boolean isAllMatch) {
        if (isAllMatch) {
            if (CollectionUtils.isNotEmpty(mockConfigRequestParamList)) {
                for (MockConfigRequestParams params : mockConfigRequestParamList) {
                    String key = params.getKey();
                    if (queryParamsObj.has(key)) {
                        boolean isMatch = MockApiUtils.isValueMatch(String.valueOf(queryParamsObj.get(key)), params);
                        if (!isMatch) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } else {
            if (CollectionUtils.isNotEmpty(mockConfigRequestParamList)) {
                for (MockConfigRequestParams params : mockConfigRequestParamList) {
                    String key = params.getKey();
                    if (queryParamsObj.has(key)) {
                        boolean isMatch = MockApiUtils.isValueMatch(String.valueOf(queryParamsObj.get(key)), params);
                        if (isMatch) {
                            return true;
                        }
                    }
                }
            } else {
                return true;
            }
            return false;
        }
    }

    public static boolean checkParamsCompliance(JSONObject queryParamsObj, MockConfigRequestParams mockConfigRequestParams) {
        if (mockConfigRequestParams != null) {
            String key = mockConfigRequestParams.getKey();
            if (queryParamsObj.has(key)) {
                return MockApiUtils.isValueMatch(String.valueOf(queryParamsObj.get(key)), mockConfigRequestParams);
            }
        }
        return true;
    }

    public static boolean checkParamsCompliance(JSONArray jsonArray, List<MockConfigRequestParams> mockConfigRequestParamList, boolean isAllMatch) {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                boolean isMatch = checkParamsCompliance(obj, mockConfigRequestParamList, isAllMatch);
                if (isMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValueMatch(String requestParam, MockConfigRequestParams params) {
        if (StringUtils.isBlank(params.getCondition())) {
            params.setCondition(MockParamConditionEnums.VALUE_EQUALS.name());
        }
        if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.VALUE_EQUALS.name())) {
            return StringUtils.equals(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.VALUE_NOT_EQUALS.name())) {
            return !StringUtils.equals(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.VALUE_CONTAINS.name())) {
            return StringUtils.contains(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.LENGTH_EQUALS.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() == length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.LENGTH_NOT_EQUALS.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() != length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.LENGTH_SHOT_THAN.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() < length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.LENGTH_LARGE_THAN.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() > length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnums.REGULAR_MATCH.name())) {
            try {
                Pattern pattern = Pattern.compile(params.getValue());
                Matcher matcher = pattern.matcher(requestParam);
                boolean isMatch = matcher.matches();
                return isMatch;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isUrlMatch(String url, String compareUrl) {
        String urlSuffix = url;
        if (urlSuffix.startsWith("/")) {
            urlSuffix = urlSuffix.substring(1);
        }
        String[] urlParams = urlSuffix.split("/");
        if (StringUtils.equalsAny(compareUrl, url, "/" + url)) {
            return true;
        } else {
            if (StringUtils.isEmpty(compareUrl)) {
                return false;
            }
            if (compareUrl.startsWith("/")) {
                compareUrl = compareUrl.substring(1);
            }
            if (StringUtils.isNotEmpty(compareUrl)) {
                String[] pathArr = compareUrl.split("/");
                if (pathArr.length == urlParams.length) {
                    boolean isFetch = true;
                    for (int i = 0; i < urlParams.length; i++) {
                        String pathItem = pathArr[i];
                        String urlItem = urlParams[i];
                        if (!(pathItem.startsWith("{") && pathItem.endsWith("}")) && !(urlItem.startsWith("{") && urlItem.endsWith("}"))) {
                            if (!StringUtils.equals(pathArr[i], urlParams[i])) {
                                isFetch = false;
                                break;
                            }
                        }

                    }
                    if (isFetch) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isUrlInList(String url, Collection<String> urlList) {
        if (CollectionUtils.isEmpty(urlList)) {
            return false;
        }
        String urlSuffix = url;
        if (urlSuffix.startsWith("/")) {
            urlSuffix = urlSuffix.substring(1);
        }
        String[] urlParams = urlSuffix.split("/");
        for (String path : urlList) {
            if (StringUtils.equalsAny(path, url, "/" + url)) {
                return true;
            } else {
                if (StringUtils.isEmpty(path)) {
                    continue;
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (StringUtils.isNotEmpty(path)) {
                    String[] pathArr = path.split("/");
                    if (pathArr.length == urlParams.length) {
                        boolean isFetch = true;
                        for (int i = 0; i < urlParams.length; i++) {
                            String pathItem = pathArr[i];
                            String urlItem = urlParams[i];
                            if (!(pathItem.startsWith("{") && pathItem.endsWith("}")) && !(urlItem.startsWith("{") && urlItem.endsWith("}"))) {
                                if (!StringUtils.equals(pathArr[i], urlParams[i])) {
                                    isFetch = false;
                                    break;
                                }
                            }

                        }
                        if (isFetch) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
