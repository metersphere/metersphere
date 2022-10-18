package io.metersphere.log.utils;

import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiTestEnvironmentDiffUtil {

    public static String diff(String newValue, String oldValue) {
        try {
            Map<String, Object> bloBsNew = JSON.parseObject(newValue, Map.class);
            Map<String, Object> bloBsOld = JSON.parseObject(oldValue, Map.class);

            Map<String, String> diffMap = new LinkedHashMap<>();
            diffMap.put("type", "preAndPostScript");

            // 对比全局脚本配置参数
            if (!StringUtils.equals(bloBsNew.get("globalScriptConfig").toString(), bloBsOld.get("globalScriptConfig").toString())) {
                Map<String, Object> globalScriptNew = JSON.parseObject(JSON.toJSONString(bloBsNew.get("globalScriptConfig")), Map.class);
                Map<String, Object> globalScriptOld = JSON.parseObject(JSON.toJSONString(bloBsOld.get("globalScriptConfig")), Map.class);
                // 前置脚本过滤请求类型
                if (!StringUtils.equals(globalScriptNew.get("filterRequestPreScript").toString(), globalScriptOld.get("filterRequestPreScript").toString())) {
                    diffMap.put("filterRequestPreScriptRaw1", JSON.toJSONString(globalScriptNew.get("filterRequestPreScript")));
                    diffMap.put("filterRequestPreScriptRaw2", JSON.toJSONString(globalScriptOld.get("filterRequestPreScript")));
                }
                // 后置脚本过滤请求类型
                if (!StringUtils.equals(globalScriptNew.get("filterRequestPostScript").toString(), globalScriptOld.get("filterRequestPostScript").toString())) {
                    diffMap.put("filterRequestPostScriptRaw1", JSON.toJSONString(globalScriptNew.get("filterRequestPostScript")));
                    diffMap.put("filterRequestPostScriptRaw2", JSON.toJSONString(globalScriptOld.get("filterRequestPostScript")));
                }
                // 前置脚本执行顺序
                if (!StringUtils.equals(globalScriptNew.get("isPreScriptExecAfterPrivateScript").toString(), globalScriptOld.get("isPreScriptExecAfterPrivateScript").toString())) {
                    diffMap.put("isPreScriptExecAfterPrivateScriptRaw1", JSON.toJSONString(globalScriptNew.get("isPreScriptExecAfterPrivateScript")));
                    diffMap.put("isPreScriptExecAfterPrivateScriptRaw2", JSON.toJSONString(globalScriptOld.get("isPreScriptExecAfterPrivateScript")));
                }
                // 后置脚本执行顺序
                if (!StringUtils.equals(globalScriptNew.get("isPostScriptExecAfterPrivateScript").toString(), globalScriptOld.get("isPostScriptExecAfterPrivateScript").toString())) {
                    diffMap.put("isPostScriptExecAfterPrivateScriptRaw1", JSON.toJSONString(globalScriptNew.get("isPostScriptExecAfterPrivateScript")));
                    diffMap.put("isPostScriptExecAfterPrivateScriptRaw2", JSON.toJSONString(globalScriptOld.get("isPostScriptExecAfterPrivateScript")));
                }
                // 前置关联场景结果
                if (!StringUtils.equals(globalScriptNew.get("connScenarioPreScript").toString(), globalScriptOld.get("connScenarioPreScript").toString())) {
                    diffMap.put("connScenarioPreScriptRaw1", JSON.toJSONString(globalScriptNew.get("connScenarioPreScript")));
                    diffMap.put("connScenarioPreScriptRaw2", JSON.toJSONString(globalScriptOld.get("connScenarioPreScript")));
                }
                // 后置关联场景结果
                if (!StringUtils.equals(globalScriptNew.get("connScenarioPostScript").toString(), globalScriptOld.get("connScenarioPostScript").toString())) {
                    diffMap.put("connScenarioPostScriptRaw1", JSON.toJSONString(globalScriptNew.get("connScenarioPostScript")));
                    diffMap.put("connScenarioPostScriptRaw2", JSON.toJSONString(globalScriptOld.get("connScenarioPostScript")));
                }
                // 全局脚本设置
                diffMap.put("globalScriptConfigRaw1", JSON.toJSONString(bloBsNew.get("globalScriptConfig")));
                diffMap.put("globalScriptConfigRaw2", JSON.toJSONString(bloBsOld.get("globalScriptConfig")));
            }

            // 对比全局前置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.get("preProcessor").toString(), bloBsOld.get("preProcessor").toString())) {
                diffMap.put("preProcessorRaw1", formatting(bloBsNew.get("preProcessor")));
                diffMap.put("preProcessorRaw2", formatting(bloBsOld.get("preProcessor")));
            }

            // 对比全局前置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.get("preStepProcessor").toString(), bloBsOld.get("preStepProcessor").toString())) {
                diffMap.put("preStepProcessorRaw1", formatting(bloBsNew.get("preStepProcessor")));
                diffMap.put("preStepProcessorRaw2", formatting(bloBsOld.get("preStepProcessor")));
            }

            // 对比全局后置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.get("postProcessor").toString(), bloBsOld.get("postProcessor").toString())) {
                diffMap.put("postProcessorRaw1", formatting(bloBsNew.get("postProcessor")));
                diffMap.put("postProcessorRaw2", formatting(bloBsOld.get("postProcessor")));
            }

            // 对比全局后置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.get("postStepProcessor").toString(), bloBsOld.get("postStepProcessor").toString())) {
                diffMap.put("postStepProcessorRaw1", formatting(bloBsNew.get("postStepProcessor")));
                diffMap.put("postStepProcessorRaw2", formatting(bloBsOld.get("postStepProcessor")));
            }

            if (diffMap.size() > 1) {
                return JSON.toJSONString(diffMap);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    private static String formatting(Object target) {
        Map<String, Object> result = JSON.parseObject(JSON.toJSONString(target), Map.class);
        result.remove("$type");
        return JSON.toJSONString(result);
    }
}
