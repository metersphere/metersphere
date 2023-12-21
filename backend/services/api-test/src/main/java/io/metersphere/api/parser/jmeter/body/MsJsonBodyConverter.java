package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.JsonBody;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-14  21:15
 */
public class MsJsonBodyConverter extends MsBodyConverter<JsonBody> {
    @Override
    public void parse(HTTPSamplerProxy sampler, JsonBody body, ParameterConfig config) {
        sampler.setPostBodyRaw(true);
        try {
            String raw = null;
            if (body.getEnableJsonSchema()) {
//                todo jsonSchema
//                JSONSchemaBuilder.generator(JSONUtil.toJSONString(this.getJsonSchema()))
//                raw = StringEscapeUtils.unescapeJava();
            } else {
                raw = parseJsonMock(body.getJsonValue());
            }
            handleRowBody(sampler, raw);
        } catch (Exception e) {
            LogUtils.error("json mock value is abnormal", e);
        }
    }


    /**
     * 将json中的 @xxx 转换成 ${__Mock(@xxx)}
     * @param jsonStr
     * @return
     */
    private String parseJsonMock(String jsonStr) {
        if (StringUtils.isNotEmpty(jsonStr)) {
            String value = StringUtils.chomp(jsonStr.trim());
            try {
                if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
                    List list = JSON.parseArray(jsonStr);
                    parseMock(list);
                    return JSON.toJSONString(list);
                } else {
                    Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
                    parseMock(map);
                    return JSON.toJSONString(map);
                }
            } catch (Exception e) {
                // 如果json串中的格式不是标准的json格式，正则替换变量
                return parseTextMock(jsonStr);
            }
        }
        return jsonStr;
    }

    private void parseMock(List list) {
        Map<Integer, String> replaceDataMap = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            Object obj = list.get(index);
            if (obj instanceof Map) {
                parseMock((Map) obj);
            } else if (obj instanceof String) {
                if (StringUtils.isNotBlank((String) obj)) {
                    String str = Mock.buildFunctionCallString((String) obj);
                    replaceDataMap.put(index, str);
                }
            }
        }
        for (Map.Entry<Integer, String> entry : replaceDataMap.entrySet()) {
            int replaceIndex = entry.getKey();
            String replaceStr = entry.getValue();
            list.set(replaceIndex, replaceStr);
        }
    }

    private void parseMock(Map map) {
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                parseMock((List) value);
            } else if (value instanceof Map) {
                parseMock((Map) value);
            } else if (value instanceof String) {
                if (StringUtils.isNotBlank((String) value)) {
                    value = Mock.buildFunctionCallString((String) value);
                }
                map.put(key, value);
            }
        }
    }
}
