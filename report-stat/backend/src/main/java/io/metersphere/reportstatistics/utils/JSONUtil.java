package io.metersphere.reportstatistics.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JSONUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object parseObject(String content) {
        return parseObject(content, Object.class);
    }

    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String content, TypeReference<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(InputStream src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List parseArray(String content) {
        return parseArray(content, Object.class);
    }

    public static <T> List<T> parseArray(String content, Class<T> valueType) {

        CollectionType javaType = typeFactory.constructCollectionType(List.class, valueType);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String content, TypeReference<T> valueType) {
        try {
            JavaType subType = typeFactory.constructType(valueType);
            CollectionType javaType = typeFactory.constructCollectionType(List.class, subType);
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map parseMap(String jsonObject) {
        try {
            return objectMapper.readValue(jsonObject, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
