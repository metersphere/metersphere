package io.metersphere.api.dto.mock;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import io.metersphere.api.dto.mockconfig.response.JsonSchemaReturnObj;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.JSONSchemaGenerator;
import io.metersphere.commons.utils.XMLUtils;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.samplers.SampleResult;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author song.tianyang
 * @Date 2021/10/14 3:00 下午
 */
public class MockApiUtils {
    public static Map<String,String> getHttpRequestHeader(HttpServletRequest request){
        Map<String,String> returnMap = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()){
            String header = headers.nextElement();
            String headerValue = request.getHeader(header);
            returnMap.put(header,headerValue);
        }
        return returnMap;
    }

    public static boolean matchRequestHeader(JSONArray mockExpectHeaderArray, Map<String, String> requestHeaderMap) {
        Map<String,String> mockExpectHeaders = new HashMap<>();
        for(int i = 0; i < mockExpectHeaderArray.size(); i++){
            JSONObject obj = mockExpectHeaderArray.getJSONObject(i);
            if(obj.containsKey("name") && obj.containsKey("value") && obj.containsKey("enable")){
                boolean enable = obj.getBoolean("enable");
                if(enable){
                    mockExpectHeaders.put(obj.getString("name"),obj.getString("value"));
                }
            }
        }
        if(MapUtils.isEmpty(requestHeaderMap) && MapUtils.isNotEmpty(mockExpectHeaders)){
            return false;
        }else {
            for (Map.Entry<String, String> entry: mockExpectHeaders.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();

                if(!requestHeaderMap.containsKey(key)){
                    return false;
                }else {
                    if(!StringUtils.equals(value,requestHeaderMap.get(key))){
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static JSONArray getExpectBodyParams(JSONObject bodyObj) {
        JSONArray returnJsonArray = new JSONArray();

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
                    if (bodyObj.containsKey("jsonSchema") && bodyObj.getJSONObject("jsonSchema").containsKey("properties")) {
                        String bodyRetunStr = bodyObj.getJSONObject("jsonSchema").getJSONObject("properties").toJSONString();
                        jsonString = JSONSchemaGenerator.getJson(bodyRetunStr);
//                        JSONObject bodyReturnObj = JSONObject.parseObject(bodyRetunStr);
//                        JSONObject returnObj = parseJsonSchema(bodyReturnObj);
//                        returnStr = returnObj.toJSONString();
                    }
                } else {
                    if (bodyObj.containsKey("raw")) {
                        jsonString = bodyObj.getString("raw");
                    }
                }
                JSONValidator jsonValidator = JSONValidator.from(jsonString);
                if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                    returnJsonArray = JSONArray.parseArray(jsonString);
                } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
                    JSONObject jsonObject = JSONObject.parseObject(jsonString);
                    returnJsonArray.add(jsonObject);
                }

            } else if (StringUtils.equalsIgnoreCase(type, "XML")) {
                if (bodyObj.containsKey("raw")) {
                    String xmlStr = bodyObj.getString("raw");
                    JSONObject matchObj = XMLUtils.XmlToJson(xmlStr);
                    returnJsonArray.add(matchObj);
                }
            } else if (StringUtils.equalsIgnoreCase(type,  "Raw")) {
                if (bodyObj.containsKey("raw")) {
                    String raw = bodyObj.getString("raw");
                    JSONObject rawObject = new JSONObject();
                    rawObject.put("raw",raw);
                    returnJsonArray.add(rawObject);
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
                    returnJsonArray.add(bodyParamArr);
                }
            }
        }catch (Exception e){}


        return  returnJsonArray;
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
                e.printStackTrace();
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

    public static JSONObject getParams(JSONArray array) {
        JSONObject returnObject = new JSONObject();
        for(int i = 0; i < array.size();i ++){
            JSONObject obj = array.getJSONObject(i);
            if(obj.containsKey("name") && obj.containsKey("value") && obj.containsKey("enable")){
                boolean isEnable = obj.getBoolean("enable");
                if(isEnable){
                    returnObject.put(obj.getString("name"),obj.getString("value"));
                }
            }
        }
        return returnObject;
    }

    public static Map<String, String> getApiResponse(String response) {
        Map<String, String> returnMap = new HashMap<>();
        String returnStr = "";
        if(StringUtils.isNotEmpty(response)){
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
            }catch (Exception e){
                MSException.throwException(e);
            }
        }
        returnMap.put("returnMsg",returnStr);
        return returnMap;
    }

    public static String getResultByResponseResult(JSONObject bodyObj,String url, Map<String,String> headerMap,RequestMockParams requestMockParams) {
        if(headerMap == null){
            headerMap = new HashMap<>();
        }
        if(requestMockParams == null){
            requestMockParams = new RequestMockParams();
        }
        if(bodyObj == null && bodyObj.isEmpty()){
            return "";
        }else {
            String returnStr = "";
            if(bodyObj.containsKey("type")){
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
                } else if(StringUtils.equalsAnyIgnoreCase(type,"Raw")){
                    if (bodyObj.containsKey("raw")) {
                        String raw = bodyObj.getString("raw");
                        returnStr = raw;
                    }
                } else if(StringUtils.equalsAnyIgnoreCase(type,"XML")){
                    if (bodyObj.containsKey("xmlHeader")) {
                        String xmlHeader = bodyObj.getString("xmlHeader");
                        if(!StringUtils.startsWith(xmlHeader,"<?") && !StringUtils.endsWith(xmlHeader,"?>")){
                            returnStr = "<?"+xmlHeader+"?>\r\n";
                        }else {
                            returnStr = xmlHeader;
                        }
                    }
                    if (bodyObj.containsKey("xmlRaw")) {
                        String raw = bodyObj.getString("xmlRaw");
                        returnStr = returnStr + raw;
                    }
                } else if(StringUtils.equalsAnyIgnoreCase(type,"fromApi")){
                    if (bodyObj.containsKey("apiRspRaw")) {
                        String raw = bodyObj.getString("apiRspRaw");
                        returnStr = raw;
                    }
                } else if(StringUtils.equalsAnyIgnoreCase(type,"script")){
                    if (bodyObj.containsKey("scriptObject")) {
                        JSONObject scriptObj = bodyObj.getJSONObject("scriptObject");
                        String script = scriptObj.getString("script");
                        String scriptLanguage =scriptObj.getString("scriptLanguage");

                        returnStr = parseScript(script,url,headerMap,requestMockParams);



                        runScript(script,scriptLanguage);
                    }
                }

            }
            return returnStr;
        }
    }

    private static String parseScript(String script,String url,Map<String,String> headerMap,RequestMockParams requestMockParams) {
        String returnMsg = "";
        if(StringUtils.isNotEmpty(script)){
            String [] scriptRowArr = StringUtils.split(script,"\n");
            for (String scriptRow : scriptRowArr) {
                scriptRow = scriptRow.trim();
                if(StringUtils.startsWith(scriptRow,"returnMsg.add(") && StringUtils.endsWith(scriptRow,")")){
                    scriptRow = scriptRow.substring(14,scriptRow.length()-1).trim();
                    if(StringUtils.equalsIgnoreCase(scriptRow,"@address")){
                        returnMsg += url;
                    }else if(StringUtils.startsWith(scriptRow,"@header(${") && StringUtils.endsWith(scriptRow,"})")){
                        String paramName = scriptRow.substring(10,scriptRow.length()-2);
                        if(headerMap.containsKey(paramName)){
                            returnMsg += headerMap.get(paramName);
                        }
                    }else if(StringUtils.startsWith(scriptRow,"@body(${") && StringUtils.endsWith(scriptRow,"})")){
                        String paramName = scriptRow.substring(8,scriptRow.length()-2);
                        if(requestMockParams.getBodyParams() != null && requestMockParams.getBodyParams().size() > 0){
                            JSONObject bodyParamObj = requestMockParams.getBodyParams().getJSONObject(0);
                            if(bodyParamObj.containsKey(paramName)){
                                returnMsg += String.valueOf(bodyParamObj.get(paramName));
                            }
                        }
                    }else if(StringUtils.equalsIgnoreCase(scriptRow,"@bodyRaw")){
                        if(requestMockParams.getBodyParams() != null && requestMockParams.getBodyParams().size() > 0){
                            JSONObject bodyParamObj = requestMockParams.getBodyParams().getJSONObject(0);
                            if(bodyParamObj.containsKey("raw")){
                                returnMsg += String.valueOf(bodyParamObj.get("raw"));
                            }
                        }
                    }else if(StringUtils.startsWith(scriptRow,"@query(${") && StringUtils.endsWith(scriptRow,"})")){
                        String paramName = scriptRow.substring(9,scriptRow.length()-2);
                        if(requestMockParams.getQueryParamsObj() != null && requestMockParams.getQueryParamsObj().containsKey(paramName)){
                            returnMsg += String.valueOf(requestMockParams.getQueryParamsObj().get(paramName));
                        }
                    }else if(StringUtils.startsWith(scriptRow,"@rest(${") && StringUtils.endsWith(scriptRow,"})")){
                        String paramName = scriptRow.substring(8,scriptRow.length()-2);
                        if(requestMockParams.getRestParamsObj() != null && requestMockParams.getRestParamsObj().containsKey(paramName)){
                            returnMsg += String.valueOf(requestMockParams.getRestParamsObj().get(paramName));
                        }
                    }else {
                        returnMsg += scriptRow;
                    }
                }

            }
        }
        return returnMsg;
    }

    private static void runScript(String script, String scriptLanguage) {
        JSR223Sampler jmeterScriptSampler = new JSR223Sampler();
        jmeterScriptSampler.setScriptLanguage(scriptLanguage);
        jmeterScriptSampler.setScript(script);
        SampleResult result = jmeterScriptSampler.sample(null);
        System.out.println(result.getResponseData());
    }
}
