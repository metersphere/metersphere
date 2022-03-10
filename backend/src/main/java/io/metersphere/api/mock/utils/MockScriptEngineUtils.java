package io.metersphere.api.mock.utils;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.JarConfigService;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockScriptEngineUtils {

    public JSONObject getVars(ScriptEngine engine) {
        try {
            return JSONObject.parseObject(JSONObject.toJSONString(engine.get("vars")));
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

    public void loadJar(String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        // 从URLClassLoader类中获取类所在文件夹的方法，jar也可以认为是一个文件夹
        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }
        // 获取方法的访问权限以便写回
        boolean accessible = method.isAccessible();
        try {
            method.setAccessible(true);
            // 获取系统类加载器
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL url = jarFile.toURI().toURL();
            method.invoke(classLoader, url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.setAccessible(accessible);
        }
    }

    /**
     * 加载jar包
     */
    private void loadJars() {
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        if (jarConfigService != null) {
            List<JarConfig> jars = jarConfigService.list();
            jars.forEach(jarConfig -> {
                try {
                    this.loadJar(jarConfig.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(e.getMessage(), e);
                }
            });
        }
    }

    public ScriptEngine getBaseScriptEngine(String scriptLanguage, String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        ScriptEngine engine = null;
        try {
            if (StringUtils.isEmpty(scriptLanguage)) {
                return null;
            }
            String preScript = "";
            if (StringUtils.equalsIgnoreCase(scriptLanguage, "beanshell")) {
                ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
                engine = scriptEngineFactory.getEngineByName("beanshell");
                preScript = this.genBeanshellPreScript(url, headerMap, requestMockParams);
            } else if (StringUtils.equalsIgnoreCase(scriptLanguage, "python")) {
                ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
                engine = scriptEngineFactory.getEngineByName(scriptLanguage);
                preScript = this.genPythonPreScript(url, headerMap, requestMockParams);
            }
            this.loadJars();
            engine.eval(preScript);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return engine;
    }

    private String genBeanshellPreScript(String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        StringBuffer preScriptBuffer = new StringBuffer();
        preScriptBuffer.append("Map vars = new HashMap();\n");
        preScriptBuffer.append("vars.put(\"address\",\"" + url + "\");\n");
        //写入请求头
        if (headerMap != null) {
            for (Map.Entry<String, String> headEntry : headerMap.entrySet()) {
                String headerKey = headEntry.getKey();
                String headerValue = headEntry.getValue();
                preScriptBuffer.append("vars.put(\"header." + headerKey + "\",\"" + headerValue + "\");\n");
            }
        }

        //写入body参数
        if (requestMockParams != null) {
            if (requestMockParams.getBodyParams() != null) {
                if (requestMockParams.getBodyParams().size() == 1) {
                    //参数是jsonObject
                    JSONObject bodyParamObj = requestMockParams.getBodyParams().getJSONObject(0);
                    for (String key : bodyParamObj.keySet()) {
                        String value = String.valueOf(bodyParamObj.get(key));
                        value = StringUtils.replace(value, "\\", "\\\\");
                        value = StringUtils.replace(value, "\"", "\\\"");
                        preScriptBuffer.append("vars.put(\"body." + key + "\",\"" + value + "\");\n");
                        if (StringUtils.equalsIgnoreCase(key, "raw")) {
                            preScriptBuffer.append("vars.put(\"bodyRaw\",\"" + value + "\");\n");
                        }
                    }
                    String jsonBody = bodyParamObj.toJSONString();
                    jsonBody = StringUtils.replace(jsonBody, "\\", "\\\\");
                    jsonBody = StringUtils.replace(jsonBody, "\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"body.json\",\"" + jsonBody + "\");\n");
                } else {
                    preScriptBuffer.append("vars.put(\"bodyRaw\",\"" + requestMockParams.getBodyParams().toJSONString() + "\");\n");
                }

            }
            //写入query参数
            if (requestMockParams.getQueryParamsObj() != null) {
                JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
                for (String key : queryParamsObj.keySet()) {
                    String value = String.valueOf(queryParamsObj.get(key));
                    value = StringUtils.replace(value, "\\", "\\\\");
                    value = StringUtils.replace(value, "\"", "\\\"");
                    preScriptBuffer.append("vars.put(\"query." + key + "\",\"" + value + "\");\n");
                }
            }
            //写入rest参数
            if (requestMockParams.getRestParamsObj() != null) {
                JSONObject restParamsObj = requestMockParams.getRestParamsObj();
                for (String key : restParamsObj.keySet()) {
                    String value = String.valueOf(restParamsObj.get(key));
                    preScriptBuffer.append("vars.put(\"rest." + key + "\",\"" + value + "\");\n");
                }
            }
        }
        return preScriptBuffer.toString();
    }

    private String genPythonPreScript(String url, Map<String, String> headerMap, RequestMockParams requestMockParams) {
        StringBuffer preScriptBuffer = new StringBuffer();
        preScriptBuffer.append("vars = {}; \n");
        preScriptBuffer.append("vars[\"address\"]=\"" + url + "\";\n");
        //写入请求头
        for (Map.Entry<String, String> headEntry : headerMap.entrySet()) {
            String headerKey = headEntry.getKey();
            String headerValue = headEntry.getValue();
            preScriptBuffer.append("vars[\"header." + headerKey + "\"]=\"" + headerValue + "\";\n");
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
                    preScriptBuffer.append("vars[\"body." + key + "\"]=\"" + value + "\";\n");
                    if (StringUtils.equalsIgnoreCase(key, "raw")) {
                        preScriptBuffer.append("vars[\"bodyRaw\"]=\"" + value + "\";\n");
                    }
                }
                String jsonBody = bodyParamObj.toJSONString();
                jsonBody = StringUtils.replace(jsonBody, "\\", "\\\\");
                jsonBody = StringUtils.replace(jsonBody, "\"", "\\\"");
                preScriptBuffer.append("vars[\"body.json\"]=\"" + jsonBody + "\";\n");
            } else {
                preScriptBuffer.append("vars[\"bodyRaw\"]=\"" + requestMockParams.getBodyParams().toJSONString() + "\";\n");
            }

        }
        //写入query参数
        if (requestMockParams.getQueryParamsObj() != null) {
            JSONObject queryParamsObj = requestMockParams.getQueryParamsObj();
            for (String key : queryParamsObj.keySet()) {
                String value = String.valueOf(queryParamsObj.get(key));
                value = StringUtils.replace(value, "\\", "\\\\");
                value = StringUtils.replace(value, "\"", "\\\"");
                preScriptBuffer.append("vars[\"query." + key + "\"]=\"" + value + "\";\n");
            }
        }
        //写入rest参数
        if (requestMockParams.getRestParamsObj() != null) {
            JSONObject restParamsObj = requestMockParams.getRestParamsObj();
            for (String key : restParamsObj.keySet()) {
                String value = String.valueOf(restParamsObj.get(key));
                preScriptBuffer.append("vars[\"rest." + key + "\"]=\"" + value + "\";\n");
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
        if (varsObject != null && varsObject.containsKey(key)) {
            value = varsObject.getString(key);
        }
        if (StringUtils.isEmpty(value)) {
            try {
                value = JSONObject.toJSONString(scriptEngine.get(key));
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        if (StringUtils.isEmpty(value) || StringUtils.equals(value,"null")) {
            value = paramKey;
        }
        return value;
    }

}
