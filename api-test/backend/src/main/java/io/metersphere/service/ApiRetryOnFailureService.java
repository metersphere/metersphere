package io.metersphere.service;

import io.metersphere.api.dto.definition.request.controller.MsRetryLoopController;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApiRetryOnFailureService {
    public final static List<String> requests = List.of(
            "HTTPSamplerProxy",
            "DubboSampler",
            "JDBCSampler",
            "TCPSampler",
            "JSR223Processor");

    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String TYPE = "type";
    private final static String RESOURCE_ID = "resourceId";
    private final static String RETRY = "MsRetry_";
    private final static String LOOP = "LoopController";

    public String retry(String data, long retryNum, boolean isCase) {
        if (StringUtils.isNotEmpty(data)) {
            JSONObject element = JSONUtil.parseObject(data);
            if (element != null && isCase) {
                return formatSampler(element, retryNum).toString();
            }
            if (element != null && element.has(HASH_TREE_ELEMENT) && !StringUtils.equalsIgnoreCase(element.optString(TYPE), LOOP)) {
                JSONArray hashTree = element.getJSONArray(HASH_TREE_ELEMENT);
                setRetry(hashTree, retryNum);
            }
            return element.toString();
        }
        return null;
    }

    public MsTestElement retryParse(String data) {
        try {
            return JSONUtil.parseObject(data, MsRetryLoopController.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    public void setRetry(JSONArray hashTree, long retryNum) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (StringUtils.equalsIgnoreCase(element.optString(TYPE), LOOP)) {
                continue;
            }
            JSONObject whileObj = formatSampler(element, retryNum);
            if (whileObj != null) {
                hashTree.put(i, whileObj);
            } else if (element.has(HASH_TREE_ELEMENT)) {
                JSONArray elementJSONArray = element.getJSONArray(HASH_TREE_ELEMENT);
                setRetry(elementJSONArray, retryNum);
            }
        }
    }

    private JSONObject formatSampler(JSONObject element, long retryNum) {
        if (element.has(TYPE) && requests.contains(element.optString(TYPE))) {
            MsRetryLoopController loopController = new MsRetryLoopController();
            loopController.setClazzName(MsRetryLoopController.class.getCanonicalName());
            loopController.setName(RETRY + element.optString(RESOURCE_ID));
            loopController.setRetryNum(retryNum);
            loopController.setEnable(true);
            loopController.setResourceId(UUID.randomUUID().toString());

            JSONObject whileObj = JSONUtil.parseObject(JSONUtil.toJSONString(loopController));
            JSONArray hashTree = new JSONArray();
            hashTree.put(element);

            whileObj.put(HASH_TREE_ELEMENT, hashTree);
            return whileObj;
        }
        return null;
    }
}
