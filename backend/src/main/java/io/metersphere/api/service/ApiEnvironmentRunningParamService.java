package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * 2021/5/13 6:24 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiEnvironmentRunningParamService {
    @Resource
    ApiTestEnvironmentMapper testEnvironmentMapper;

    public void addParam(String environmentId, Map<String, String> varMap) {
        if (MapUtils.isEmpty(varMap)) {
            return;
        }
        ApiTestEnvironmentWithBLOBs environment = testEnvironmentMapper.selectByPrimaryKey(environmentId);
        if (environment == null) {
            return;
        }
        boolean envNeedUpdate = false;
        try {
            JSONObject configObj = JSONObject.parseObject(environment.getConfig());
            if (configObj.containsKey("commonConfig")) {
                JSONObject commonConfig = configObj.getJSONObject("commonConfig");
                if (commonConfig.containsKey("variables")) {
                    JSONArray variables = commonConfig.getJSONArray("variables");

                    for (Map.Entry<String, String> entry : varMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        boolean contains = false;
                        for (int i = 0; i < variables.size(); i++) {
                            JSONObject jsonObj = variables.getJSONObject(i);
                            if (jsonObj.containsKey("name") && StringUtils.equals(jsonObj.getString("name"), key)) {
                                contains = true;
                                if (jsonObj.containsKey("value") && StringUtils.equals(jsonObj.getString("value"), value)) {
                                    break;
                                } else {
                                    envNeedUpdate = true;
                                    jsonObj.put("value", value);
                                }

                            }
                        }
                        if (!contains) {
                            envNeedUpdate = true;
                            JSONObject itemObj = new JSONObject();
                            itemObj.put("name", key);
                            itemObj.put("value", value);
                            itemObj.put("enable", true);

                            if (variables.size() == 0) {
                                variables.add(itemObj);
                            } else {
                                variables.add(variables.size() - 1, itemObj);
                            }
                            commonConfig.put("variables", variables);
                        }
                    }
                } else {
                    JSONArray variables = new JSONArray();
                    for (Map.Entry<String, String> entry : varMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        JSONObject itemObj = new JSONObject();
                        itemObj.put("name", key);
                        itemObj.put("value", value);
                        itemObj.put("enable", true);
                        variables.add(itemObj);
                    }
                    JSONObject emptyObj = new JSONObject();
                    emptyObj.put("enable", true);
                    variables.add(emptyObj);

                    commonConfig.put("variables", variables);

                }
            } else {
                JSONObject commonConfig = new JSONObject();
                JSONArray variables = new JSONArray();
                for (Map.Entry<String, String> entry : varMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("name", key);
                    itemObj.put("value", value);
                    itemObj.put("enable", true);
                    variables.add(itemObj);
                }
                JSONObject emptyObj = new JSONObject();
                emptyObj.put("enable", true);
                variables.add(emptyObj);
                commonConfig.put("variables", variables);
                configObj.put("commonConfig", commonConfig);
            }
            if (envNeedUpdate) {
                environment.setConfig(configObj.toJSONString());
                testEnvironmentMapper.updateByPrimaryKeyWithBLOBs(environment);
            }

        } catch (Exception ex) {
            LoggerUtil.error("设置环境变量异常", ex);
        }
    }

    public void parseEvn(String envStr) {
        String[] envStringArr = envStr.split("\n");
        Map<String, Map<String, String>> envVarsMap = new HashMap<>();
        for (String env : envStringArr) {
            if (StringUtils.contains(env, "=")) {
                String[] envItem = env.split("=");
                String jmeterVarKey = envItem[0];
                if (this.checkValidity(jmeterVarKey, "MS.ENV.")) {
                    String[] envAndKeyArr = jmeterVarKey.substring("MS.ENV.".length()).split("\\.");
                    String envId = envAndKeyArr[0];
                    String[] keyArr = ArrayUtils.remove(envAndKeyArr, 0);
                    String key = StringUtils.join(keyArr, ".");
                    String value = StringUtils.substring(env, jmeterVarKey.length() + 1);
                    if (StringUtils.isNoneEmpty(envId, key)) {
                        if (envVarsMap.containsKey(envId)) {
                            envVarsMap.get(envId).put(key, value);
                        } else {
                            Map<String, String> varMap = new HashMap<>();
                            varMap.put(key, value);
                            envVarsMap.put(envId, varMap);
                        }
                    }
                }
            }
        }

        if (MapUtils.isNotEmpty(envVarsMap)) {
            for (Map.Entry<String, Map<String, String>> entry : envVarsMap.entrySet()) {
                String envId = entry.getKey();
                Map<String, String> vars = entry.getValue();
                this.addParam(envId, vars);
            }
        }
    }

    public void parseEnvironment(List<String> evnStrList) {
        for (String evnStr : evnStrList) {
            this.parseEvn(evnStr);
        }
    }

    public boolean checkValidity(String str, String regex) {
        if (str == null) {
            return false;
        }
        if (regex == null) {
            return true;
        }

        if (str.startsWith(regex)) {
            return true;
        } else {
            return false;
        }
    }
}
