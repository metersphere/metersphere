package io.metersphere.log.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiTestEnvironmentDiffUtil {

    public static String diff(String newValue, String oldValue) {
        try {
            JSONObject bloBsNew = JSON.parseObject(newValue);
            JSONObject bloBsOld = JSON.parseObject(oldValue);

            Map<String, String> diffMap = new LinkedHashMap<>();
            diffMap.put("type", "preAndPostScript");

            // 对比全局脚本配置参数
            if (!StringUtils.equals(bloBsNew.getString("globalScriptConfig"), bloBsOld.getString("globalScriptConfig"))) {
                JSONObject globalScriptNew = JSON.parseObject(bloBsNew.getString("globalScriptConfig"));
                JSONObject globalScriptOld = JSON.parseObject(bloBsOld.getString("globalScriptConfig"));
                // 前置脚本过滤请求类型
                if (!StringUtils.equals(globalScriptNew.getString("filterRequestPreScript"), globalScriptOld.getString("filterRequestPreScript"))) {
                    diffMap.put("filterRequestPreScriptRaw1", globalScriptNew.getString("filterRequestPreScript"));
                    diffMap.put("filterRequestPreScriptRaw2", globalScriptOld.getString("filterRequestPreScript"));
                }
                // 后置脚本过滤请求类型
                if (!StringUtils.equals(globalScriptNew.getString("filterRequestPostScript"), globalScriptOld.getString("filterRequestPostScript"))) {
                    diffMap.put("filterRequestPostScriptRaw1", globalScriptNew.getString("filterRequestPostScript"));
                    diffMap.put("filterRequestPostScriptRaw2", globalScriptOld.getString("filterRequestPostScript"));
                }
                // 前置脚本执行顺序
                if (!StringUtils.equals(globalScriptNew.getString("isPreScriptExecAfterPrivateScript"), globalScriptOld.getString("isPreScriptExecAfterPrivateScript"))) {
                    diffMap.put("isPreScriptExecAfterPrivateScriptRaw1", globalScriptNew.getString("isPreScriptExecAfterPrivateScript"));
                    diffMap.put("isPreScriptExecAfterPrivateScriptRaw2", globalScriptOld.getString("isPreScriptExecAfterPrivateScript"));
                }
                // 后置脚本执行顺序
                if (!StringUtils.equals(globalScriptNew.getString("isPostScriptExecAfterPrivateScript"), globalScriptOld.getString("isPostScriptExecAfterPrivateScript"))) {
                    diffMap.put("isPostScriptExecAfterPrivateScriptRaw1", globalScriptNew.getString("isPostScriptExecAfterPrivateScript"));
                    diffMap.put("isPostScriptExecAfterPrivateScriptRaw2", globalScriptOld.getString("isPostScriptExecAfterPrivateScript"));
                }
                // 前置关联场景结果
                if (!StringUtils.equals(globalScriptNew.getString("connScenarioPreScript"), globalScriptOld.getString("connScenarioPreScript"))) {
                    diffMap.put("connScenarioPreScriptRaw1", globalScriptNew.getString("connScenarioPreScript"));
                    diffMap.put("connScenarioPreScriptRaw2", globalScriptOld.getString("connScenarioPreScript"));
                }
                // 后置关联场景结果
                if (!StringUtils.equals(globalScriptNew.getString("connScenarioPostScript"), globalScriptOld.getString("connScenarioPostScript"))) {
                    diffMap.put("connScenarioPostScriptRaw1", globalScriptNew.getString("connScenarioPostScript"));
                    diffMap.put("connScenarioPostScriptRaw2", globalScriptOld.getString("connScenarioPostScript"));
                }
                // 全局脚本设置
                diffMap.put("globalScriptConfigRaw1", bloBsNew.getString("globalScriptConfig"));
                diffMap.put("globalScriptConfigRaw2", bloBsOld.getString("globalScriptConfig"));
            }

            // 对比全局前置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.getString("preProcessor"), bloBsOld.getString("preProcessor"))) {
                diffMap.put("preProcessorRaw1", formatting(bloBsNew.getString("preProcessor")));
                diffMap.put("preProcessorRaw2", formatting(bloBsOld.getString("preProcessor")));
            }

            // 对比全局前置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.getString("preStepProcessor"), bloBsOld.getString("preStepProcessor"))) {
                diffMap.put("preStepProcessorRaw1", formatting(bloBsNew.getString("preStepProcessor")));
                diffMap.put("preStepProcessorRaw2", formatting(bloBsOld.getString("preStepProcessor")));
            }

            // 对比全局后置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.getString("postProcessor"), bloBsOld.getString("postProcessor"))) {
                diffMap.put("postProcessorRaw1", formatting(bloBsNew.getString("postProcessor")));
                diffMap.put("postProcessorRaw2", formatting(bloBsOld.getString("postProcessor")));
            }

            // 对比全局后置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.getString("postStepProcessor"), bloBsOld.getString("postStepProcessor"))) {
                diffMap.put("postStepProcessorRaw1", formatting(bloBsNew.getString("postStepProcessor")));
                diffMap.put("postStepProcessorRaw2", formatting(bloBsOld.getString("postStepProcessor")));
            }

            if (diffMap.size() > 1) {
                return JSON.toJSONString(diffMap);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    public static String formatting(String target) {
        JSONObject result = JSONObject.parseObject(target);
        result.remove("$type");
        return result.toJSONString();
    }
}
