package io.metersphere.api.dto.mockserver;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.XMLUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
public class BodyParamMatchRole {
    @Schema(description = "参数类型(kv/json/xml/raw  默认为raw)")
    private String paramType;
    @Schema(description = "formData的匹配规则")
    private keyValueMatchRole formDataMatch;
    @Schema(description = "文本匹配规则")
    private String raw;

    public boolean matchXml(Map<String, Object> requestMap) {
        Map<String, Object> mockMap = XMLUtils.xmlStringToJson(raw);
        return this.matchMap(mockMap, requestMap);
    }

    public boolean matchJson(String requestJson) {

        if (StringUtils.startsWith(requestJson, "{") && StringUtils.endsWith(requestJson, "}")) {
            //入参是Object，如果mock期望设置的不是Object，视为无法匹配
            if (StringUtils.startsWith(this.raw, "{") && StringUtils.endsWith(this.raw, "}")) {
                Map<String, Object> mockMap = JSON.parseMap(this.raw);
                Map<String, Object> requestMap = JSON.parseMap(requestJson);
                return this.matchObject(mockMap, requestMap);
            } else {
                return false;
            }
        }

        if (StringUtils.startsWith(requestJson, "[") && StringUtils.endsWith(requestJson, "]")) {
            List<Object> requestList = JSON.parseArray(requestJson, Object.class);
            if (StringUtils.startsWith(this.raw, "{") && StringUtils.endsWith(this.raw, "}")) {
                //入参是Array，如果mock期望设置是Object，则入参中的任意一个匹配，视为匹配
                Map<String, Object> mockMap = JSON.parseMap(this.raw);
                for (Object requestObj : requestList) {
                    if (this.matchObject(mockMap, requestObj)) {
                        return true;
                    }
                }
                return false;
            } else if (StringUtils.startsWith(this.raw, "[") && StringUtils.endsWith(this.raw, "]")) {
                //入参是Array，如果mock期望设置也是Array，则Mock中的每个数据都匹配才视为匹配
                List<Object> mockList = JSON.parseArray(requestJson, Object.class);
                for (Object mockObj : mockList) {
                    boolean match = false;
                    for (int i = 0; i < requestList.size(); i++) {
                        Object requestObj = requestList.get(i);
                        match = this.matchObject(mockObj, requestObj);
                        if (match) {
                            requestList.remove(i);
                            break;
                        }
                    }
                    if (!match) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean matchMap(Map<String, Object> mockMap, Map<String, Object> requestMap) {
        for (Map.Entry<String, Object> entry : mockMap.entrySet()) {
            if (!this.matchObject(entry.getValue(), requestMap.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private boolean matchObject(Object mockRule, Object requestParam) {
        if (ObjectUtils.anyNull(mockRule, requestParam)) {
            return false;
        }

        if (mockRule instanceof List<?> && requestParam instanceof List<?>) {
            List<Object> mockList = (List<Object>) mockRule;
            List<Object> requestList = (List<Object>) requestParam;
            if (mockList.size() != requestList.size()) {
                return false;
            }
            for (int i = 0; i < mockList.size(); i++) {
                if (!this.matchObject(mockList.get(i), requestList.get(i))) {
                    return false;
                }
            }
            return true;
        } else if (mockRule instanceof Map<?, ?> && requestParam instanceof Map<?, ?>) {
            Map<String, Object> mockMap = (Map<String, Object>) mockRule;
            Map<String, Object> requestMap = (Map<String, Object>) requestParam;
            for (Map.Entry<?, ?> entry : mockMap.entrySet()) {
                if (!this.matchObject(entry.getValue(), requestMap.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }

        //既不是list也不是object，直接进行值比对
        return StringUtils.equals(String.valueOf(mockRule), String.valueOf(requestParam));
    }
}