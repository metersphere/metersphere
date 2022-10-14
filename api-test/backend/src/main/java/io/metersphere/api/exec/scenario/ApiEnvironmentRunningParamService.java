package io.metersphere.api.exec.scenario;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
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
            JSONObject configObj = JSONUtil.parseObject(environment.getConfig());
            if (configObj.has("commonConfig")) {
                JSONObject commonConfig = configObj.optJSONObject("commonConfig");
                if (commonConfig.has("variables")) {
                    JSONArray variables = commonConfig.optJSONArray("variables");
                    List<JSONObject> variableList = new LinkedList<>();
                    for (Map.Entry<String, String> entry : varMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        boolean contains = false;
                        for (int i = 0; i < variables.length(); i++) {
                            JSONObject jsonObj = variables.optJSONObject(i);
                            if (jsonObj.has("name") && StringUtils.equals(jsonObj.optString("name"), key)) {
                                contains = true;
                                if (jsonObj.has("value") && StringUtils.equals(jsonObj.optString("value"), value)) {
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
                            if (variableList.size() == 0) {
                                variableList.add(itemObj);
                            } else {
                                variableList.add(variables.length() - 1, itemObj);
                            }
                            commonConfig.put("variables", new JSONArray(variableList));
                        }
                    }
                } else {
                    List<JSONObject> variables = new LinkedList<>();
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
                    commonConfig.put("variables", new JSONArray(variables));
                }
            } else {
                JSONObject commonConfig = new JSONObject();
                List<JSONObject> variables = new LinkedList<>();
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
                commonConfig.put("variables", new JSONArray(variables));
                configObj.put("commonConfig", commonConfig);
            }
            if (envNeedUpdate) {
                environment.setConfig(configObj.toString());
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
                if (ArrayUtils.isEmpty(envItem)) {
                    continue;
                }
                String jmeterVarKey = envItem[0];
                if (this.checkValidity(jmeterVarKey, "MS.ENV.")) {
                    String[] envAndKeyArr = jmeterVarKey.substring("MS.ENV.".length()).split("\\.");
                    if (ArrayUtils.isEmpty(envAndKeyArr)) {
                        continue;
                    }
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
        try {
            for (String evnStr : evnStrList) {
                this.parseEvn(evnStr);
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
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
