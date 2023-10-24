package io.metersphere.api.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.metersphere.api.dto.jmeter.post.processors.MsPostJSR223Processor;
import io.metersphere.api.dto.jmeter.sampler.MsDebugSampler;
import io.metersphere.sdk.exception.MSException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class ApiDataUtils {
    private ApiDataUtils() {
    }

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();

    private static final StdSubtypeResolver resolver = new StdSubtypeResolver();

    static {
        // 添加处理资源文件的类
        final List<NamedType> namedTypes = new LinkedList<>();
        namedTypes.add(new NamedType(MsPostJSR223Processor.class, MsPostJSR223Processor.class.getSimpleName()));
        namedTypes.add(new NamedType(MsDebugSampler.class, MsDebugSampler.class.getSimpleName()));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 支持json字符中带注释符
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        namedTypes.forEach(resolver::registerSubtypes);
        objectMapper.setSubtypeResolver(resolver);
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
     * @param namedTypes PluginSubType 注解的类
     */
    public static void setResolver(List<NamedType> namedTypes) {
        namedTypes.forEach(resolver::registerSubtypes);
        objectMapper.setSubtypeResolver(resolver);
    }

    /**
     * 设置动态加载的jar的Resolver
     *
     * @param clazz PluginSubType 注解的类
     */
    public static void setResolver(Class<?> clazz) {
        resolver.registerSubtypes(clazz);
        objectMapper.setSubtypeResolver(resolver);
    }
}
