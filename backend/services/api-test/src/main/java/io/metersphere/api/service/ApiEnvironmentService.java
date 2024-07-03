package io.metersphere.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiEnvironmentService {

    private static final String COMMON_CONFIG = "config";
    private static final String VARIABLES = "commonVariables";
    private static final String VALUE = "value";
    private static final String ENABLE = "enable";
    private static final String NAME = "key";
    private static final String ENV_STR = "MS.ENV.";

    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void parseEnvironment(List<String> environmentVariables) {
        try {
            environmentVariables.forEach(this::parseEvn);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    private void parseEvn(String envStr) {
        String[] envStringArr = envStr.split(StringUtils.LF);
        Map<String, Map<String, String>> envVarsMap = new HashMap<>();
        for (String env : envStringArr) {
            if (StringUtils.contains(env, "=")) {
                String[] envItem = env.split("=");
                if (envItem.length == 0) {
                    continue;
                }
                String jmeterVarKey = envItem[0];
                if (checkValidity(jmeterVarKey)) {
                    String[] envAndKeyArr = jmeterVarKey.substring(ENV_STR.length()).split("\\.");
                    if (envAndKeyArr.length == 0) {
                        continue;
                    }
                    String envId = envAndKeyArr[0];
                    String key = StringUtils.join(ArrayUtils.remove(envAndKeyArr, 0), ".");
                    String value = StringUtils.substring(env, jmeterVarKey.length() + 1);
                    if (StringUtils.isNoneEmpty(envId, key)) {
                        envVarsMap.computeIfAbsent(envId, k -> new HashMap<>()).put(key, value);
                    }
                }
            }
        }

        if (!envVarsMap.isEmpty()) {
            envVarsMap.forEach(this::addParam);
        }
    }

    private boolean checkValidity(String str) {
        return str != null && str.startsWith(ApiEnvironmentService.ENV_STR);
    }

    private void addParam(String environmentId, Map<String, String> varMap) {
        if (varMap.isEmpty()) {
            return;
        }
        EnvironmentBlob environment = environmentBlobMapper.selectByPrimaryKey(environmentId);
        if (environment == null) {
            return;
        }

        try {
            JsonNode configObj = objectMapper.readTree(new String(environment.getConfig(), StandardCharsets.UTF_8));
            if (configObj.isMissingNode() || !configObj.has(VARIABLES)) {
                ObjectNode newCommonConfig = objectMapper.createObjectNode();
                List<JsonNode> variables = createArray(varMap);
                newCommonConfig.putArray(VARIABLES).addAll(variables);
                ((ObjectNode) configObj).set(COMMON_CONFIG, newCommonConfig);
                return;
            }

            JsonNode variables = configObj.path(VARIABLES);
            if (variables.isMissingNode()) {
                return;
            }

            boolean envNeedUpdate = false;
            for (Map.Entry<String, String> entry : varMap.entrySet()) {
                boolean contains = false;
                Iterator<JsonNode> elements = variables.elements();
                int length = 0;
                while (elements.hasNext()) {
                    length++;
                    JsonNode jsonObj = elements.next();
                    if (jsonObj.has(NAME) && jsonObj.has(VALUE)
                            && jsonObj.get(NAME).asText().equals(entry.getKey())) {
                        contains = true;
                        if (jsonObj.get(VALUE).asText().equals(entry.getValue())) {
                            break;
                        }
                        ((ObjectNode) jsonObj).put(VALUE, entry.getValue());
                        envNeedUpdate = true;
                        break;
                    } else {
                        envNeedUpdate = true;
                    }
                }

                if (!contains) {
                    // 不包含则添加
                    ObjectNode itemObj = objectMapper.createObjectNode();
                    itemObj.put(NAME, entry.getKey());
                    itemObj.put(VALUE, entry.getValue());
                    itemObj.put(ENABLE, true);
                    ArrayNode arrayNode = ((ArrayNode) variables);
                    JsonNode lastNode = arrayNode.get(arrayNode.size() - 1);
                    boolean isBlankLine = isBlankLine(lastNode);
                    if (isBlankLine) {
                        // 如果最后一行是空行，则替换最后一行
                        ((ArrayNode) variables).set(length - 1, itemObj);
                    } else {
                        ((ArrayNode) variables).add(itemObj);
                    }
                    // 新增一个空行
                    ObjectNode blankItem = objectMapper.createObjectNode();
                    blankItem.put(NAME, StringUtils.EMPTY);
                    blankItem.put(VALUE, StringUtils.EMPTY);
                    blankItem.put(ENABLE, true);
                    ((ArrayNode) variables).add(blankItem);
                    ((ObjectNode) configObj).set(VARIABLES, variables);
                }
            }

            if (envNeedUpdate) {
                environment.setConfig(objectMapper.writeValueAsBytes(configObj));
                environmentBlobMapper.updateByPrimaryKeyWithBLOBs(environment);
            }
        } catch (JsonProcessingException ex) {
            LogUtils.error("Setting environment variables exception", ex);
        }
    }

    private boolean isBlankLine(JsonNode lastNode) {
        boolean nameBlank = lastNode.get(NAME) == null || StringUtils.isBlank(lastNode.get(NAME).asText());
        boolean valueBlank = lastNode.get(VALUE) == null || StringUtils.isBlank(lastNode.get(VALUE).asText());
        return nameBlank && valueBlank;
    }

    private List<JsonNode> createArray(Map<String, String> varMap) {
        return varMap.entrySet().stream()
                .map(entry -> objectMapper.createObjectNode()
                        .put(NAME, entry.getKey())
                        .put(VALUE, entry.getValue())
                        .put(ENABLE, true))
                .collect(Collectors.toList());
    }
}
