package io.metersphere.api.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
public class JSONUtil {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    public static final int DEFAULT_MAX_STRING_LEN = Integer.MAX_VALUE;

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 使用BigDecimal来序列化
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        // 设置JSON处理字符长度限制
        objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(DEFAULT_MAX_STRING_LEN).build());
        // 处理时间格式
        objectMapper.registerModule(new JavaTimeModule());
    }


    public static JSONObject parseObject(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                throw new MSException("value is null");
            }
            Map<String, Object> map = JSON.parseObject(value, Map.class);
            return new JSONObject(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray parseArray(String text) {
        List<Object> list = JSON.parseObject(text, List.class);
        return new JSONArray(list);
    }

    public static ObjectNode parseObjectNode(String text) {
        try {
            return (ObjectNode) objectMapper.readTree(text);
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return objectMapper.createObjectNode();
    }

    public static ObjectNode createObj() {
        return objectMapper.createObjectNode();
    }
}
