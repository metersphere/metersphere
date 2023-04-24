package io.metersphere.commons.utils.mock;


import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.enums.MockRequestType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.ProjectClassLoader;
import io.metersphere.metadata.service.FileMetadataService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockScriptEngineUtils {

    public JSONObject getVars(ScriptEngine engine) {
        try {
            return JSONUtil.parseObject(JSON.toJSONString(engine.get("vars")));
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return new JSONObject();
    }

    public String get(ScriptEngine engine, String key) {
        return String.valueOf(engine.get(key));
    }

    public void runScript(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 加载jar包
     */
    private List<String> getJarPaths(String projectId) {
        FileMetadataService jarConfigService = CommonBeanFactory.getBean(FileMetadataService.class);
        if (jarConfigService != null) {
            return jarConfigService.getJar(new LinkedList<>() {{
                this.add(projectId);
            }});
        } else {
            return new ArrayList<>();
        }
    }

    public ScriptEngine getBaseScriptEngine(String projectId, String scriptLanguage, String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        ScriptEngine engine = null;
        try {
            if (StringUtils.isEmpty(scriptLanguage)) {
                return null;
            }
            String preScript = StringUtils.EMPTY;
            if (StringUtils.equalsIgnoreCase(scriptLanguage, ElementConstants.BEANSHELL)) {
                ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
                engine = scriptEngineFactory.getEngineByName(ElementConstants.BEANSHELL);
                preScript = this.genBeanshellPreScript(url, headerMap, requestMockParams);
            } else if (StringUtils.equalsIgnoreCase(scriptLanguage, "python")) {
                ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
                engine = scriptEngineFactory.getEngineByName(scriptLanguage);
                preScript = this.genPythonPreScript(url, headerMap, requestMockParams);
            }

            if (engine != null) {
                ClassLoader loader = ProjectClassLoader.getClassLoader(new ArrayList<>() {{
                    this.add(projectId);
                }});
                Thread.currentThread().setContextClassLoader(loader);
                engine.eval(preScript);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return engine;
    }

    private String escapeString(String str) {
        if (str == null) {
            return str;
        } else {
            return StringUtils.replace(str, "\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }
    }

    private String genBeanshellPreScript(String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        StringBuilder preScriptBuffer = new StringBuilder();
        preScriptBuffer.append("Map vars = new HashMap();\n");
        preScriptBuffer.append("vars.put(\"address\",\"").append(url).append("\");\n");
        //写入请求头
        if (headerMap != null) {
            for (Map.Entry<String, String> headEntry : headerMap.entrySet()) {
                String headerKey = headEntry.getKey();
                String headerValue = headEntry.getValue();
                headerKey = StringUtils.replace(headerKey, "\\", "\\\\").replace("\"", "\\\"");
                headerValue = StringUtils.replace(headerValue, "\\", "\\\\").replace("\"", "\\\"");
                preScriptBuffer.append("vars.put(\"header.").append(headerKey).append("\",\"").append(headerValue).append("\");\n");
            }
        }

        if (requestMockParams != null) {
            //判断是否含有tcpParam
            if (StringUtils.isNotEmpty(requestMockParams.getTcpParam())) {
                String value = this.escapeString(requestMockParams.getTcpParam());
                preScriptBuffer.append("vars.put(\"tcpParam\",\"").append(value).append("\");\n");
            }
            //写入body参数
            if (requestMockParams.isPost()) {
                if (requestMockParams.getQueryParamsObj() != null) {
                    JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
                    for (String key : queryParamsObj.keySet()) {
                        String value = String.valueOf(queryParamsObj.get(key));
                        value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                        key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                        preScriptBuffer.append("vars.put(\"body.").append(key).append("\",\"").append(value).append("\");\n");
                    }
                }
                if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.JSON.name())) {
                    String jsonBody = requestMockParams.getRaw();
                    jsonBody = StringUtils.replace(jsonBody, "\n", "");
                    jsonBody = StringUtils.replace(jsonBody, "\\", "\\\\");
                    jsonBody = StringUtils.replace(jsonBody, "\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"body.json\",\"").append(jsonBody).append("\");\n");
                } else if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.XML.name())) {
                    String xmlRaw = requestMockParams.getRaw();
                    xmlRaw = StringUtils.chomp(xmlRaw);
                    xmlRaw = StringUtils.replace(xmlRaw, "\n", "");
                    xmlRaw = StringUtils.replace(xmlRaw, "\\", "\\\\");
                    xmlRaw = StringUtils.replace(xmlRaw, "\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"body.xml\",\"").append(xmlRaw).append("\");\n");
                } else if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.RAW.name())) {
                    String bodyRowString = requestMockParams.getRaw();
                    bodyRowString = StringUtils.replace(bodyRowString, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"bodyRaw\",\"").append(bodyRowString).append("\");\n");
                }
            }

            //写入query参数
            if (!requestMockParams.isPost() && requestMockParams.getQueryParamsObj() != null) {
                JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
                for (String key : queryParamsObj.keySet()) {
                    String value = String.valueOf(queryParamsObj.get(key));
                    value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                    key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"query.").append(key).append("\",\"").append(value).append("\");\n");
                }
            }

            //写入rest参数
            if (requestMockParams.getRestParamsObj() != null) {
                JSONObject restParamsObj = requestMockParams.getRestParamsObj();
                for (String key : restParamsObj.keySet()) {
                    String value = String.valueOf(restParamsObj.get(key));
                    key = StringUtils.replace(key, "\"", "\\\"");
                    value = StringUtils.replace(value, "\"", "\\\"");
                    key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"rest.").append(key).append("\",\"").append(value).append("\");\n");
                }
            }
        }
        return preScriptBuffer.toString();
    }

    private String genPythonPreScript(String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        StringBuilder preScriptBuffer = new StringBuilder();
        preScriptBuffer.append("vars = {}; \n");
        preScriptBuffer.append("vars[\"address\"]=\"").append(url).append("\";\n");
        //写入请求头
        for (Map.Entry<String, String> headEntry : headerMap.entrySet()) {
            String headerKey = headEntry.getKey();
            String headerValue = headEntry.getValue();
            headerKey = StringUtils.replace(headerKey, "\\", "\\\\").replace("\"", "\\\"");
            headerValue = StringUtils.replace(headerValue, "\\", "\\\\").replace("\"", "\\\"");
            preScriptBuffer.append("vars[\"header.").append(headerKey).append("\"]=\"").append(headerValue).append("\";\n");
        }
        if (requestMockParams != null) {
            //判断是否含有tcpParam
            if (StringUtils.isNotEmpty(requestMockParams.getTcpParam())) {
                String value = requestMockParams.getTcpParam();
                value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                preScriptBuffer.append("vars[\"tcpParam\"]=\"").append(value).append("\";\n");
            }
            //写入body参数
            if (requestMockParams.isPost()) {
                if (requestMockParams.getQueryParamsObj() != null) {
                    JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
                    for (String key : queryParamsObj.keySet()) {
                        String value = String.valueOf(queryParamsObj.get(key));
                        value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                        key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                        preScriptBuffer.append("vars[\"body.").append(key).append("\"]=\"").append(value).append("\";\n");
                    }
                }
                if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.JSON.name())) {
                    String jsonRaw = requestMockParams.getRaw();
                    jsonRaw = StringUtils.chomp(jsonRaw);
                    jsonRaw = StringUtils.replace(jsonRaw, "\n", "");
                    jsonRaw = StringUtils.replace(jsonRaw, "\\", "\\\\");
                    jsonRaw = StringUtils.replace(jsonRaw, "\"", "\\\"");
                    preScriptBuffer.append("vars[\"body.json\"]=\"").append(jsonRaw).append("\";\n");
                } else if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.XML.name())) {
                    String xmlRaw = requestMockParams.getRaw();
                    xmlRaw = StringUtils.chomp(xmlRaw);
                    xmlRaw = StringUtils.replace(xmlRaw, "\n", "");
                    xmlRaw = StringUtils.replace(xmlRaw, "\\", "\\\\");
                    xmlRaw = StringUtils.replace(xmlRaw, "\"", "\\\"");
                    preScriptBuffer.append("vars[\"body.xml\"]=\"").append(xmlRaw).append("\";\n");
                } else if (StringUtils.equals(requestMockParams.getParamType(), MockRequestType.RAW.name())) {
                    String bodyRowString = requestMockParams.getRaw();
                    bodyRowString = StringUtils.replace(bodyRowString, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars[\"bodyRaw\"]=\"").append(bodyRowString).append("\";\n");
                }
            }

            //写入query参数
            if (!requestMockParams.isPost() && requestMockParams.getQueryParamsObj() != null) {
                JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
                for (String key : queryParamsObj.keySet()) {
                    String value = String.valueOf(queryParamsObj.get(key));
                    value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                    key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars[\"query.").append(key).append("\"]=\"").append(value).append("\";\n");
                }
            }

            //写入rest参数
            if (requestMockParams.getRestParamsObj() != null) {
                JSONObject restParamsObj = requestMockParams.getRestParamsObj();
                for (String key : restParamsObj.keySet()) {
                    String value = String.valueOf(restParamsObj.get(key));
                    key = StringUtils.replace(key, "\\", "\\\\").replace("\"", "\\\"");
                    value = StringUtils.replace(value, "\\", "\\\\").replace("\"", "\\\"");
                    preScriptBuffer.append("vars[\"rest.").append(key).append("\"]=\"").append(value).append("\";\n");
                }
            }
        }
        return preScriptBuffer.toString();
    }

    public String parseReportString(ScriptEngine scriptEngine, String reportString) {
        String regStr = "\\$\\{([^${}]+)\\}";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(reportString);
        List<String> paramKeys = new ArrayList<>();
        while (matcher.find()) {
            String paramKey = matcher.group(0);
            if (!paramKeys.contains(paramKey)) {
                paramKeys.add(paramKey);
            }
        }
        JSONObject varsObject = this.getVars(scriptEngine);
        for (String paramKey : paramKeys) {
            String value = this.getValue(scriptEngine, varsObject, paramKey);
            reportString = StringUtils.replace(reportString, paramKey, value);
        }
        return reportString;
    }

    private String getValue(ScriptEngine scriptEngine, JSONObject varsObject, String paramKey) {
        String key = paramKey;
        if (key.startsWith("${") && key.endsWith("}")) {
            key = paramKey.substring(2, key.length() - 1);
        }
        String value = null;
        if (varsObject != null && varsObject.has(key)) {
            value = varsObject.optString(key);
        }
        if (StringUtils.isEmpty(value)) {
            try {
                value = JSON.toJSONString(scriptEngine.get(key));
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        if (StringUtils.isEmpty(value) || StringUtils.equals(value, "null")) {
            value = paramKey;
        }
        return value;
    }

}
