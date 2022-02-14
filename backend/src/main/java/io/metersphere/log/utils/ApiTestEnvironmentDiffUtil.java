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
                diffMap.put("globalScriptConfig_raw_1", bloBsNew.getString("globalScriptConfig"));
                diffMap.put("globalScriptConfig_raw_2", bloBsOld.getString("globalScriptConfig"));
            }

            // 对比全局前置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.getString("preProcessor"), bloBsOld.getString("preProcessor"))) {
                diffMap.put("preProcessor_raw_1", bloBsNew.getString("preProcessor"));
                diffMap.put("preProcessor_raw_2", bloBsOld.getString("preProcessor"));
            }

            // 对比全局前置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.getString("preStepProcessor"), bloBsOld.getString("preStepProcessor"))) {
                diffMap.put("preStepProcessor_raw_1", bloBsNew.getString("preStepProcessor"));
                diffMap.put("preStepProcessor_raw_2", bloBsOld.getString("preStepProcessor"));
            }

            // 对比全局后置脚本(单个请求)
            if (!StringUtils.equals(bloBsNew.getString("postProcessor"), bloBsOld.getString("postProcessor"))) {
                diffMap.put("postProcessor_raw_1", bloBsNew.getString("postProcessor"));
                diffMap.put("postProcessor_raw_2", bloBsOld.getString("postProcessor"));
            }

            // 对比全局后置脚本(所有请求)
            if (!StringUtils.equals(bloBsNew.getString("postStepProcessor"), bloBsOld.getString("postStepProcessor"))) {
                diffMap.put("postStepProcessor_raw_1", bloBsNew.getString("postStepProcessor"));
                diffMap.put("postStepProcessor_raw_2", bloBsOld.getString("postStepProcessor"));
            }

            if (diffMap.size() > 1) {
                return JSON.toJSONString(diffMap);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }
}
