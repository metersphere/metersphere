package io.metersphere.api.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.MsJMeterComponent;
import io.metersphere.api.dto.request.controller.*;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.sdk.exception.MSException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class ApiDataUtils {
    private ApiDataUtils() {
    }

    private static ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();

    // 默认内置子组件，如果有新的子组件，需要在这里添加
    static final List<NamedType> namedTypes = new LinkedList<>();

    static {
        // 默认内置的子组件
        namedTypes.add(new NamedType(MsHTTPElement.class, MsHTTPElement.class.getSimpleName()));
        namedTypes.add(new NamedType(MsCommonElement.class, MsCommonElement.class.getSimpleName()));
        namedTypes.add(new NamedType(MsIfController.class, MsIfController.class.getSimpleName()));
        namedTypes.add(new NamedType(MsLoopController.class, MsLoopController.class.getSimpleName()));
        namedTypes.add(new NamedType(MsOnceOnlyController.class, MsOnceOnlyController.class.getSimpleName()));
        namedTypes.add(new NamedType(MsConstantTimerController.class, MsConstantTimerController.class.getSimpleName()));
        namedTypes.add(new NamedType(MsScriptElement.class, MsScriptElement.class.getSimpleName()));
        namedTypes.add(new NamedType(MsJMeterComponent.class, MsJMeterComponent.class.getSimpleName()));
        setObjectMapper(objectMapper);
        namedTypes.forEach(objectMapper::registerSubtypes);
    }

    private static void setObjectMapper(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 支持json字符中带注释符
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 设置默认使用 BigDecimal 解析浮点数
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    }

    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    public static Object parseObject(String content) {
        return parseObject(content, Object.class);
    }

    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    public static <T> T parseObject(InputStream src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    public static <T> List<T> parseArray(String content, Class<T> valueType) {
        CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, valueType);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    /**
     * 设置动态加载的jar的Resolver
     *
     * @param newTypes PluginSubType 注解的类
     */
    public static void setResolver(List<NamedType> newTypes) {
        // 获取所有子组件 TODO：此方法无法卸载组件
        // SubtypeResolver subtypeResolver = objectMapper.getSubtypeResolver();
        // objectMapper.setSubtypeResolver(subtypeResolver);

        objectMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .build();
        setObjectMapper(objectMapper);

        // 加入默认组件
        namedTypes.forEach(objectMapper::registerSubtypes);
        // 加入新的动态组件
        newTypes.forEach(objectMapper::registerSubtypes);

    }

    /**
     * 设置动态加载的jar的Resolver
     *
     * @param clazz PluginSubType 注解的类
     */
    public static void setResolver(Class<?> clazz) {
        setResolver(new LinkedList<>());
        // 加入新的动态组件
        objectMapper.registerSubtypes(clazz);
    }

    /**
     * 设置动态加载的jar的Resolver
     */
    public static void setResolvers(List<Class<?>> clazzList) {
        setResolver(new LinkedList<>());
        // 加入新的动态组件
        clazzList.forEach(objectMapper::registerSubtypes);
    }

    public static JsonNode readTree(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    public static String writerWithDefaultPrettyPrinter(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new MSException(e);
        }
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static <T> T convertValue(JsonNode jsonNode, Class<T> valueType) {
        try {
            return objectMapper.convertValue(jsonNode, valueType);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }

    public static byte[] toJSONBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
