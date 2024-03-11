package io.metersphere.api.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.*;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSchemaBuilder {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 支持json字符中带注释符
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 设置默认使用 BigDecimal 解析浮点数
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    }

    public static String jsonSchemaToJson(String jsonSchemaString) {
        try {
            // 解析 JSON Schema 字符串为 JsonNode
            JsonNode jsonSchemaNode = objectMapper.readTree(jsonSchemaString);
            Map<String, String> processMap = new HashMap<>();
            // 生成符合 JSON Schema 的 JSON
            JsonNode jsonNode = generateJson(jsonSchemaNode, processMap);
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            if (MapUtils.isNotEmpty(processMap)) {
                for (String str : processMap.keySet()) {
                    jsonString = jsonString.replace(str, processMap.get(str));
                }
            }
            return jsonString;
        } catch (Exception exception) {
            LogUtils.error("jsonSchemaToJson error", exception);
            return jsonSchemaString;
        }
    }

    private static JsonNode generateJson(JsonNode jsonSchemaNode, Map<String, String> processMap) {
        ObjectNode jsonNode = objectMapper.createObjectNode();

        String type = jsonSchemaNode.get(PropertyConstant.TYPE).asText();
        if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
            JsonNode propertiesNode = jsonSchemaNode.get(PropertyConstant.PROPERTIES);
            // 遍历 properties
            if (propertiesNode != null) {
                propertiesNode.fields().forEachRemaining(entry -> {
                    String propertyName = entry.getKey();
                    JsonNode propertyNode = entry.getValue();
                    // 根据属性类型生成对应的值
                    JsonNode valueNode = generateValue(entry.getKey(), propertyNode, processMap);
                    // 将属性和值添加到 JSON 对象节点
                    jsonNode.set(propertyName, valueNode);
                });
            }
        } else if (StringUtils.equals(type, PropertyConstant.ARRAY)) {
            JsonNode itemsNode = jsonSchemaNode.get(PropertyConstant.ITEMS);
            if (itemsNode != null) {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                arrayNode.add(generateValue(null, itemsNode, processMap));
                return arrayNode;
            }
        }
        return jsonNode;
    }

    private static JsonNode generateValue(String propertyName, JsonNode propertyNode, Map<String, String> processMap) {
        // 获取属性类型
        if (propertyNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = propertyNode.get(PropertyConstant.TYPE).asText();
        String value = propertyNode.get(PropertyConstant.EXAMPLE).asText();
        return switch (type) {
            case PropertyConstant.STRING ->
                    new TextNode(!StringUtils.equals(value, PropertyConstant.NULL) ? value : "string");
            case PropertyConstant.INTEGER -> {
                if (isVariable(value)) {
                    yield getJsonNodes(propertyName, processMap, value);
                } else {
                    yield new IntNode(propertyNode.get(PropertyConstant.EXAMPLE).asInt());
                }
            }
            case PropertyConstant.NUMBER -> {
                if (isVariable(value)) {
                    yield getJsonNodes(propertyName, processMap, value);
                } else {
                    yield new DecimalNode(propertyNode.get(PropertyConstant.EXAMPLE).decimalValue());
                }
            }
            case PropertyConstant.BOOLEAN -> {
                if (isVariable(value)) {
                    yield getJsonNodes(propertyName, processMap, value);
                } else {
                    yield BooleanNode.valueOf(propertyNode.get(PropertyConstant.EXAMPLE).asBoolean());
                }
            }

            case PropertyConstant.OBJECT -> generateJson(propertyNode, processMap);
            case PropertyConstant.ARRAY -> {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                JsonNode itemsNode = propertyNode.get(PropertyConstant.ITEMS);
                if (itemsNode != null) {
                    arrayNode.add(generateValue(null, itemsNode, processMap));
                }
                yield arrayNode;
            }
            default -> NullNode.getInstance();
        };

    }

    private static boolean isVariable(String value) {
        return !StringUtils.equals(value, PropertyConstant.NULL) && (value.startsWith("@") || value.startsWith("${"));
    }

    @NotNull
    private static TextNode getJsonNodes(String propertyName, Map<String, String> processMap, String value) {
        String key = StringUtils.join("\"", propertyName, "\"", " : \"", value, "\"");
        String targetValue = StringUtils.join("\"", propertyName, "\"", ": ", value);
        processMap.put(key, targetValue);
        return new TextNode(value);
    }

    public static String preview(String jsonSchema) {
        String jsonString = JsonSchemaBuilder.jsonSchemaToJson(jsonSchema);
        //需要匹配到mock函数  然后换成mock数据
        if (StringUtils.isNotBlank(jsonString)) {
            String pattern = "@[a-zA-Z\\\\(|,'-\\\\d ]*[a-zA-Z)-9),\\\\\"]";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(jsonString);
            while (matcher.find()) {
                //取出group的最后一个字符 主要是防止 @string|number 和 @string 这种情况
                String group = matcher.group();
                String lastChar = null;
                if (group.endsWith(",") || group.endsWith("\"")) {
                    lastChar = group.substring(group.length() - 1);
                    group = group.substring(0, group.length() - 1);
                }
                jsonString = jsonString.replace(matcher.group(),
                        StringUtils.join(Mock.calculate(group), lastChar));
            }
        }
        return jsonString;

    }
}
