package io.metersphere.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.project.constants.PropertyConstant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSchemaBuilder {

    public static String jsonSchemaToJson(String jsonSchemaString) {
        // 解析 JSON Schema 字符串为 JsonNode
        JsonNode jsonSchemaNode = ApiDataUtils.readTree(jsonSchemaString);
        Map<String, String> processMap = new HashMap<>();
        // 生成符合 JSON Schema 的 JSON
        JsonNode jsonNode = generateJson(jsonSchemaNode, processMap);
        String jsonString = ApiDataUtils.writerWithDefaultPrettyPrinter(jsonNode);
        if (MapUtils.isNotEmpty(processMap)) {
            for (String str : processMap.keySet()) {
                jsonString = jsonString.replace(str, processMap.get(str));
            }
        }
        return jsonString;
    }

    private static JsonNode generateJson(JsonNode jsonSchemaNode, Map<String, String> processMap) {
        ObjectNode jsonNode = ApiDataUtils.createObjectNode();

        if (jsonSchemaNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = jsonSchemaNode.get(PropertyConstant.TYPE) == null ? StringUtils.EMPTY : jsonSchemaNode.get(PropertyConstant.TYPE).asText();
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
            JsonNode items = jsonSchemaNode.get(PropertyConstant.ITEMS);
            if (items != null) {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                items.forEach(item -> arrayNode.add(generateValue(null, item, processMap)));
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
        String type = propertyNode.get(PropertyConstant.TYPE) == null ? StringUtils.EMPTY : propertyNode.get(PropertyConstant.TYPE).asText();
        String value = propertyNode.get(PropertyConstant.EXAMPLE) == null ? StringUtils.EMPTY : propertyNode.get(PropertyConstant.EXAMPLE).asText();
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
                    try {
                        yield new DecimalNode(new BigDecimal(propertyNode.get(PropertyConstant.EXAMPLE).asText()));
                    } catch (Exception e) {
                        yield new DecimalNode(propertyNode.get(PropertyConstant.EXAMPLE).decimalValue());
                    }
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
                JsonNode items = propertyNode.get(PropertyConstant.ITEMS);
                if (items != null) {
                    items.forEach(item -> arrayNode.add(generateValue(null, item, processMap)));
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
        String jsonString = jsonSchemaToJson(jsonSchema);
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
