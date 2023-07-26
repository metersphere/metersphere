package io.metersphere.api.exec.scenario;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
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
    public static final String COMMON_CONFIG = "commonConfig";
    public static final String VARIABLES = "variables";
    public static final String VALUE = "value";
    public static final String ENABLE = "enable";
    public static final String NAME = "name";
    public static final String ENV_STR = "MS.ENV.";
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
        try {
            JSONObject configObj = JSONUtil.parseObject(environment.getConfig());
            if (!configObj.has(COMMON_CONFIG)) {
                JSONObject commonConfig = new JSONObject();
                List<JSONObject> variables = createArray(varMap);
                JSONObject emptyObj = new JSONObject();
                emptyObj.put(ENABLE, true);
                variables.add(emptyObj);
                commonConfig.put(VARIABLES, variables);
                configObj.put(COMMON_CONFIG, commonConfig);
                return;
            }
            JSONObject commonConfig = configObj.optJSONObject(COMMON_CONFIG);
            if (!commonConfig.has(VARIABLES)) {
                List<JSONObject> variables = createArray(varMap);
                JSONObject emptyObj = new JSONObject();
                emptyObj.put(ENABLE, true);
                variables.add(emptyObj);
                commonConfig.put(VARIABLES, variables);
                return;
            }
            JSONArray variables = commonConfig.optJSONArray(VARIABLES);
            if (variables == null) {
                return;
            }
            boolean envNeedUpdate = false;
            for (Map.Entry<String, String> entry : varMap.entrySet()) {
                boolean contains = false;
                for (int i = 0; i < variables.length(); i++) {
                    JSONObject jsonObj = variables.optJSONObject(i);
                    if (jsonObj.has(NAME) && StringUtils.equals(jsonObj.optString(NAME), entry.getKey())) {
                        contains = true;
                        if (jsonObj.has(VALUE) && StringUtils.equals(jsonObj.optString(VALUE), entry.getValue())) {
                            break;
                        } else {
                            envNeedUpdate = true;
                            jsonObj.put(VALUE, entry.getValue());
                        }
                    }
                }
                if (!contains) {
                    envNeedUpdate = true;
                    JSONObject itemObj = new JSONObject();
                    itemObj.put(NAME, entry.getKey());
                    itemObj.put(VALUE, entry.getValue());
                    itemObj.put(ENABLE, true);
                    if (!variables.isEmpty() && StringUtils.isEmpty(variables.optJSONObject(variables.length() - 1).optString(NAME))) {
                        variables.put(variables.length() - 1, itemObj);
                    } else {
                        variables.put(itemObj);
                    }
                    JSONObject emptyObj = new JSONObject();
                    emptyObj.put(ENABLE, true);
                    variables.put(emptyObj);
                    commonConfig.put(VARIABLES, variables);
                }
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
        String[] envStringArr = envStr.split(StringUtils.LF);
        Map<String, Map<String, String>> envVarsMap = new HashMap<>();
        for (String env : envStringArr) {
            if (StringUtils.contains(env, "=")) {
                String[] envItem = env.split("=");
                if (ArrayUtils.isEmpty(envItem)) {
                    continue;
                }
                String jmeterVarKey = envItem[0];
                if (this.checkValidity(jmeterVarKey, ENV_STR)) {
                    String[] envAndKeyArr = jmeterVarKey.substring(ENV_STR.length()).split("\\.");
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

    private List<JSONObject> createArray(Map<String, String> varMap) {
        List<JSONObject> variables = new LinkedList<>();
        for (Map.Entry<String, String> entry : varMap.entrySet()) {
            JSONObject itemObj = new JSONObject();
            itemObj.put(NAME, entry.getKey());
            itemObj.put(VALUE, entry.getValue());
            itemObj.put(ENABLE, true);
            variables.add(itemObj);
        }
        return variables;
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
