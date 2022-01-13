package io.metersphere.api.dto.mock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import io.metersphere.api.dto.mockconfig.response.JsonSchemaReturnObj;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.JSONSchemaGenerator;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtils;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.samplers.SampleResult;
import org.json.XML;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

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
        for (int i = 0; i < mockExpectHeaderArray.size(); i++) {
            JSONObject obj = mockExpectHeaderArray.getJSONObject(i);
            if (obj.containsKey("name") && obj.containsKey("value")) {
                mockExpectHeaders.put(obj.getString("name"), obj.getString("value"));
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

    public static JSON getExpectBodyParams(JSONObject bodyObj) {
        JSON returnJson = new JSONArray();

        try {
            String type = bodyObj.getString("type");
            if (StringUtils.equalsIgnoreCase(type, "JSON")) {
                //判断是否是JsonSchema
                boolean isJsonSchema = false;
                if (bodyObj.containsKey("format")) {
                    String foramtValue = String.valueOf(bodyObj.get("format"));
                    if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                        isJsonSchema = true;
                    }
                }

                String jsonString = "";
                if (isJsonSchema) {
                    if (bodyObj.containsKey("jsonSchema")) {
                        String bodyRetunStr = bodyObj.getJSONObject("jsonSchema").toJSONString();
                        jsonString = JSONSchemaGenerator.getJson(bodyRetunStr);
                    }
                } else {
                    if (bodyObj.containsKey("raw")) {
                        jsonString = bodyObj.getString("raw");
                    }
                }
                JSONValidator jsonValidator = JSONValidator.from(jsonString);
                if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                    returnJson = JSONArray.parseArray(jsonString);
                } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
                    JSONObject jsonObject = JSONObject.parseObject(jsonString);
                    returnJson = jsonObject;
                }

            } else if (StringUtils.equalsIgnoreCase(type, "XML")) {
                if (bodyObj.containsKey("raw")) {
                    String xmlStr = bodyObj.getString("raw");
                    JSONObject matchObj = XMLUtils.XmlToJson(xmlStr);
                    returnJson = matchObj;
                }
            } else if (StringUtils.equalsIgnoreCase(type, "Raw")) {
                if (bodyObj.containsKey("raw")) {
                    String raw = bodyObj.getString("raw");
                    if (StringUtils.isNotEmpty(raw)) {
                        JSONObject rawObject = new JSONObject();
                        rawObject.put("raw", raw);
                        returnJson = rawObject;
                    }
                }
            } else if (StringUtils.equalsAnyIgnoreCase(type, "Form Data", "WWW_FORM")) {
                if (bodyObj.containsKey("kvs")) {
                    JSONObject bodyParamArr = new JSONObject();
                    JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                    for (int i = 0; i < kvsArr.size(); i++) {
                        JSONObject kv = kvsArr.getJSONObject(i);
                        if (kv.containsKey("name")) {
                            String values = kv.getString("value");
                            if (StringUtils.isEmpty(values)) {
                                values = "";
                            } else {
                                try {
                                    values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                } catch (Exception e) {
                                }
                            }
                            bodyParamArr.put(kv.getString("name"), values);
                        }
                    }
                    returnJson = bodyParamArr;
                }
            }
        } catch (Exception e) {
        }


        return returnJson;
    }

    public static JSONObject parseJsonSchema(JSONObject bodyReturnObj) {
        JSONObject returnObj = new JSONObject();
        if (bodyReturnObj == null) {
            return returnObj;
        }

        Set<String> keySet = bodyReturnObj.keySet();
        for (String key : keySet) {
            try {
                JsonSchemaReturnObj obj = bodyReturnObj.getObject(key, JsonSchemaReturnObj.class);
                if (StringUtils.equals("object", obj.getType())) {
                    JSONObject itemObj = parseJsonSchema(obj.getProperties());
                    if (!itemObj.isEmpty()) {
                        returnObj.put(key, itemObj);
                    }
                } else if (StringUtils.equals("array", obj.getType())) {
                    if (obj.getItems() != null) {
                        JSONObject itemObj = obj.getItems();
                        if (itemObj.containsKey("type")) {
                            if (StringUtils.equals("object", itemObj.getString("type")) && itemObj.containsKey("properties")) {
                                JSONObject arrayObj = itemObj.getJSONObject("properties");
                                JSONObject parseObj = parseJsonSchema(arrayObj);
                                JSONArray array = new JSONArray();
                                array.add(parseObj);
                                returnObj.put(key, array);
                            } else if (StringUtils.equals("string", itemObj.getString("type")) && itemObj.containsKey("mock")) {
                                JsonSchemaReturnObj arrayObj = JSONObject.toJavaObject(itemObj, JsonSchemaReturnObj.class);
                                String value = getMockValues(arrayObj.getMockValue());
                                JSONArray array = new JSONArray();
                                array.add(value);
                                returnObj.put(key, array);
                            }
                        }
                    }
                } else {
                    String values = obj.getMockValue();
                    if (StringUtils.isEmpty(values)) {
                        values = "";
                    } else {
                        try {
                            values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                        } catch (Exception e) {
                        }
                    }
                    returnObj.put(key, values);
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return returnObj;
    }

    private static String getMockValues(String values) {
        if (StringUtils.isEmpty(values)) {
            values = "";
        } else {
            try {
                values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
            } catch (Exception e) {
            }
        }
        return values;
    }

    public static JSONObject getParamsByJSONArray(JSONArray array) {
        JSONObject returnObject = new JSONObject();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.containsKey("name") && obj.containsKey("value")) {
                returnObject.put(obj.getString("name"), obj.getString("value"));
            }
        }
        return returnObject;
    }

    public static Map<String, String> getApiResponse(String response) {
        Map<String, String> returnMap = new HashMap<>();
        String returnStr = "";
        if (StringUtils.isNotEmpty(response)) {
            try {
                JSONObject respObj = JSONObject.parseObject(response);
                if (respObj.containsKey("body")) {
                    JSONObject bodyObj = respObj.getJSONObject("body");
                    if (bodyObj.containsKey("type")) {
                        String type = bodyObj.getString("type");
                        if (StringUtils.equals(type, "JSON")) {
                            //判断是否是JsonSchema
                            boolean isJsonSchema = false;
                            if (bodyObj.containsKey("format")) {
                                String foramtValue = String.valueOf(bodyObj.get("format"));
                                if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                                    isJsonSchema = true;
                                }
                            }
                            if (isJsonSchema) {
                                if (bodyObj.containsKey("jsonSchema") && bodyObj.getJSONObject("jsonSchema").containsKey("properties")) {
                                    String bodyRetunStr = bodyObj.getJSONObject("jsonSchema").getJSONObject("properties").toJSONString();
                                    JSONObject bodyReturnObj = JSONObject.parseObject(bodyRetunStr);
                                    JSONObject returnObj = MockApiUtils.parseJsonSchema(bodyReturnObj);
                                    returnStr = returnObj.toJSONString();
                                }
                            } else {
                                if (bodyObj.containsKey("raw")) {
                                    returnStr = bodyObj.getString("raw");
                                }
                            }
                        } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                            if (bodyObj.containsKey("raw")) {
                                String raw = bodyObj.getString("raw");
                                returnStr = raw;
                            }
                        } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                            Map<String, String> paramMap = new LinkedHashMap<>();
                            if (bodyObj.containsKey("kvs")) {
                                JSONArray bodyParamArr = new JSONArray();
                                JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                for (int i = 0; i < kvsArr.size(); i++) {
                                    JSONObject kv = kvsArr.getJSONObject(i);
                                    if (kv.containsKey("name")) {
                                        String values = kv.getString("value");
                                        if (StringUtils.isEmpty(values)) {
                                            values = "";
                                        } else {
                                            try {
                                                values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                            } catch (Exception e) {
                                            }
                                        }
                                        paramMap.put(kv.getString("name"), values);
                                    }
                                }
                            }
                            returnStr = JSONObject.toJSONString(paramMap);
                        }
                    }
                }
            } catch (Exception e) {
                MSException.throwException(e);
            }
        }
        returnMap.put("returnMsg", returnStr);
        return returnMap;
    }

    public static String getResultByResponseResult(JSONObject bodyObj, String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        if (requestMockParams == null) {
            requestMockParams = new RequestMockParams();
        }
        if (bodyObj == null && bodyObj.isEmpty()) {
            return "";
        } else {
            String returnStr = "";
            if (bodyObj.containsKey("type")) {
                String type = bodyObj.getString("type");
                if (StringUtils.equals(type, "JSON")) {
                    //判断是否是JsonSchema
                    boolean isJsonSchema = false;
                    if (bodyObj.containsKey("format")) {
                        String foramtValue = String.valueOf(bodyObj.get("format"));
                        if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                            isJsonSchema = true;
                        }
                    }
                    if (isJsonSchema) {
                        if (bodyObj.containsKey("jsonSchema") && bodyObj.getJSONObject("jsonSchema").containsKey("properties")) {
                            String bodyRetunStr = bodyObj.getJSONObject("jsonSchema").getJSONObject("properties").toJSONString();
                            JSONObject bodyReturnObj = JSONObject.parseObject(bodyRetunStr);
                            JSONObject returnObj = MockApiUtils.parseJsonSchema(bodyReturnObj);
                            returnStr = returnObj.toJSONString();
                        }
                    } else {
                        if (bodyObj.containsKey("raw")) {
                            returnStr = bodyObj.getString("raw");
                        }
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "Raw")) {
                    if (bodyObj.containsKey("raw")) {
                        String raw = bodyObj.getString("raw");
                        returnStr = raw;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "XML")) {
                    if (bodyObj.containsKey("xmlHeader")) {
                        String xmlHeader = bodyObj.getString("xmlHeader");
                        if (!StringUtils.startsWith(xmlHeader, "<?") && !StringUtils.endsWith(xmlHeader, "?>")) {
                            returnStr = "<?" + xmlHeader + "?>\r\n";
                        } else {
                            returnStr = xmlHeader;
                        }
                    }
                    if (bodyObj.containsKey("xmlRaw")) {
                        String raw = bodyObj.getString("xmlRaw");
                        returnStr = returnStr + raw;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "fromApi")) {
                    if (bodyObj.containsKey("apiRspRaw")) {
                        String raw = bodyObj.getString("apiRspRaw");
                        returnStr = raw;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(type, "script")) {
                    if (bodyObj.containsKey("scriptObject")) {
                        JSONObject scriptObj = bodyObj.getJSONObject("scriptObject");
                        String script = scriptObj.getString("script");
                        String scriptLanguage = scriptObj.getString("scriptLanguage");

                        String baseScript = parseScript(url, headerMap, requestMockParams);
                        try {
                            script = baseScript + script;
                            if (StringUtils.isEmpty(scriptLanguage)) {
                                scriptLanguage = "beanshell";
                            }
                            returnStr = runScript(script, scriptLanguage);
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                }

            }
            return returnStr;
        }
    }

    private static String parseScript(String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        StringBuffer scriptStringBuffer = new StringBuffer();
        scriptStringBuffer.append("import java.util.HashMap;\n\n");
        scriptStringBuffer.append("HashMap requestParams = new HashMap();\n");
        scriptStringBuffer.append("requestParams.put(\"address\",\"" + url + "\");\n");
        //写入请求头
        for (Map.Entry<String, String> headEntry : headerMap.entrySet()) {
            String headerKey = headEntry.getKey();
            String headerValue = headEntry.getValue();
            scriptStringBuffer.append("requestParams.put(\"header." + headerKey + "\",\"" + headerValue + "\");\n");
        }
        //写入body参数
        if (requestMockParams.getBodyParams() != null) {
            if (requestMockParams.getBodyParams().size() == 1) {
                //参数是jsonObject
                JSONObject bodyParamObj = requestMockParams.getBodyParams().getJSONObject(0);
                for (String key : bodyParamObj.keySet()) {
                    String value = String.valueOf(bodyParamObj.get(key));
                    value = StringUtils.replace(value, "\\", "\\\\");
                    value = StringUtils.replace(value, "\"", "\\\"");
                    scriptStringBuffer.append("requestParams.put(\"body." + key + "\",\"" + value + "\");\n");
                    if (StringUtils.equalsIgnoreCase(key, "raw")) {
                        scriptStringBuffer.append("requestParams.put(\"bodyRaw\",\"" + value + "\");\n");
                    }
                }
                String jsonBody = bodyParamObj.toJSONString();
                jsonBody = StringUtils.replace(jsonBody, "\\", "\\\\");
                jsonBody = StringUtils.replace(jsonBody, "\"", "\\\"");
                scriptStringBuffer.append("requestParams.put(\"body.json\",\"" + jsonBody + "\");\n");
            } else {
                scriptStringBuffer.append("requestParams.put(\"bodyRaw\",\"" + requestMockParams.getBodyParams().toJSONString() + "\");\n");
            }

        }
        //写入query参数
        if (requestMockParams.getQueryParamsObj() != null) {
            JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
            for (String key : queryParamsObj.keySet()) {
                String value = String.valueOf(queryParamsObj.get(key));
                value = StringUtils.replace(value, "\\", "\\\\");
                value = StringUtils.replace(value, "\"", "\\\"");
                scriptStringBuffer.append("requestParams.put(\"query." + key + "\",\"" + value + "\");\n");
            }
        }
        //写入rest参数
        if (requestMockParams.getRestParamsObj() != null) {
            JSONObject restParamsObj = requestMockParams.getRestParamsObj();
            for (String key : restParamsObj.keySet()) {
                String value = String.valueOf(restParamsObj.get(key));
                scriptStringBuffer.append("requestParams.put(\"rest." + key + "\",\"" + value + "\");\n");
            }
        }
        return scriptStringBuffer.toString();
    }

    private static String runScript(String script, String scriptLanguage) throws ScriptException {
        JSR223Sampler jmeterScriptSampler = new JSR223Sampler();
        jmeterScriptSampler.setScriptLanguage(scriptLanguage);
        jmeterScriptSampler.setScript(script);
        SampleResult result = jmeterScriptSampler.sample(null);
        return result.getResponseDataAsString();

    }

    public static RequestMockParams getParams(String urlParams, String apiPath, JSONObject queryParamsObject, JSON paramJson, boolean isPostRequest) {
        RequestMockParams returnParams = getGetParamMap(urlParams, apiPath, queryParamsObject, isPostRequest);
        if (paramJson != null) {
            if (paramJson instanceof JSONObject) {
                if (!((JSONObject) paramJson).isEmpty()) {
                    JSONArray paramsArray = new JSONArray();
                    paramsArray.add(paramJson);
                    returnParams.setBodyParams(paramsArray);
                }
            } else if (paramJson instanceof JSONArray) {
                JSONArray paramArray = (JSONArray) paramJson;
                if (!paramArray.isEmpty()) {
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

        if(isPostRequest){
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(queryParamsObject);
            requestMockParams.setBodyParams(jsonArray);
        }
        return requestMockParams;
    }

    public static JSON getPostParamMap(HttpServletRequest request) {
        if (StringUtils.startsWithIgnoreCase(request.getContentType(), "application/JSON")) {
            JSON returnJson = null;
            try {
                String param = getRequestPostStr(request);
                if (StringUtils.isNotEmpty(param)) {
                    JSONValidator jsonValidator = JSONValidator.from(param);
                    if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                        returnJson = JSONArray.parseArray(param);
                    } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
                        returnJson = JSONObject.parseObject(param);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
            return returnJson;
        } else if (StringUtils.startsWithIgnoreCase(request.getContentType(), "text/xml")) {
            String xmlString = readXml(request);
            JSONObject object = XMLUtils.XmlToJson(xmlString);
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
                    String value = "";
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
        String result = "";
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
            result = new String(outSteam.toByteArray(), "UTF-8");
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
            charEncoding = "UTF-8";
        }
        if(buffer == null){
            return "";
        }else {
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
                    request.getInputStream(), "UTF-8"));
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
}
