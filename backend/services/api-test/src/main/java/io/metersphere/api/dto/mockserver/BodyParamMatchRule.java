package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.XMLUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
public class BodyParamMatchRule {
    /**
     * 当前选择的请求体类型
     * 可选值为 {@link Body.BodyType}
     * 同时持久化多个类型的请求体
     */
    @NotBlank
    private String bodyType;
    /**
     * None 请求体
     * 当 bodyType 为 NONE 时，使用该字段
     */
    private NoneBody noneBody;
    /**
     * form-data 请求体
     * 当 bodyType 为 FORM_DATA 时，使用该字段
     */
    private MockFormDataBody formDataBody = new MockFormDataBody();
    /**
     * x-www-form-urlencoded 请求体
     * 当 bodyType 为 WWW_FORM 时，使用该字段
     */
    private KeyValueMatchRule wwwFormBody = new KeyValueMatchRule();
    /**
     * json 请求体
     * 当 bodyType 为 JSON 时，使用该字段
     */
    private JsonBody jsonBody = new JsonBody();
    /**
     * xml 请求体
     * 当 bodyType 为 XML 时，使用该字段
     */
    private XmlBody xmlBody = new XmlBody();
    /**
     * raw 请求体
     * 当 bodyType 为 RAW 时，使用该字段
     */
    private RawBody rawBody = new RawBody();
    /**
     * binary 请求体
     * 当 bodyType 为 BINARY 时，使用该字段
     */
    private BinaryBody binaryBody = new BinaryBody();

    public boolean matchXml(Map<String, Object> requestMap) {
        Map<String, Object> mockMap = XMLUtils.xmlStringToJson(xmlBody.getValue());
        return this.matchMap(mockMap, requestMap);
    }

    public boolean matchJson(String requestJson) {
        String jsonStr = jsonBody.getJsonValue();
        if (StringUtils.startsWith(requestJson, "{") && StringUtils.endsWith(requestJson, "}")) {
            //入参是Object，如果mock期望设置的不是Object，视为无法匹配
            if (StringUtils.startsWith(jsonStr, "{") && StringUtils.endsWith(jsonStr, "}")) {
                Map<String, Object> mockMap = JSON.parseMap(jsonStr);
                Map<String, Object> requestMap = JSON.parseMap(requestJson);
                return this.matchObject(mockMap, requestMap);
            } else {
                return false;
            }
        }

        if (StringUtils.startsWith(requestJson, "[") && StringUtils.endsWith(requestJson, "]")) {
            List<Object> requestList = JSON.parseArray(requestJson, Object.class);
            if (StringUtils.startsWith(jsonStr, "{") && StringUtils.endsWith(jsonStr, "}")) {
                //入参是Array，如果mock期望设置是Object，则入参中的任意一个匹配，视为匹配
                Map<String, Object> mockMap = JSON.parseMap(jsonStr);
                for (Object requestObj : requestList) {
                    if (this.matchObject(mockMap, requestObj)) {
                        return true;
                    }
                }
                return false;
            } else if (StringUtils.startsWith(jsonStr, "[") && StringUtils.endsWith(jsonStr, "]")) {
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
        if ((mockMap.isEmpty() && !requestMap.isEmpty()) || (!mockMap.isEmpty() && requestMap.isEmpty())) {
            return false;
        }
        for (Map.Entry<String, Object> entry : mockMap.entrySet()) {
            if (!this.matchObject(entry.getValue(), requestMap.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private boolean matchObject(Object mockRule, Object requestParam) {

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