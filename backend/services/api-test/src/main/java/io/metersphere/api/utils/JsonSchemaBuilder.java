package io.metersphere.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.metersphere.project.constants.PropertyConstant;
import nl.flotsam.xeger.Xeger;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JsonSchemaBuilder {

    public static String jsonSchemaToJson(String jsonSchemaString, boolean isPreview) {
        // 解析 JSON Schema 字符串为 JsonNode
        JsonNode jsonSchemaNode = ApiDataUtils.readTree(jsonSchemaString);
        Map<String, String> processMap = new HashMap<>();
        // 生成符合 JSON Schema 的 JSON
        JsonNode jsonNode = generateJson(jsonSchemaNode, processMap, isPreview);
        String jsonString = ApiDataUtils.writerWithDefaultPrettyPrinter(jsonNode);
        if (MapUtils.isNotEmpty(processMap)) {
            for (String str : processMap.keySet()) {
                jsonString = jsonString.replace(str, processMap.get(str));
            }
        }
        return jsonString;
    }

    private static JsonNode generateJson(JsonNode jsonSchemaNode, Map<String, String> processMap, boolean isPreview) {
        ObjectNode jsonNode = ApiDataUtils.createObjectNode();

        if (jsonSchemaNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = getPropertyTextValue(jsonSchemaNode, PropertyConstant.TYPE);
        if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
            JsonNode propertiesNode = jsonSchemaNode.get(PropertyConstant.PROPERTIES);
            // 遍历 properties
            if (propertiesNode != null) {
                propertiesNode.fields().forEachRemaining(entry -> {
                    String propertyName = entry.getKey();
                    JsonNode propertyNode = entry.getValue();
                    // 根据属性类型生成对应的值
                    JsonNode valueNode = isPreview ? generateValueForPreview(entry.getKey(), propertyNode, processMap)
                            : generateValue(entry.getKey(), propertyNode, processMap);
                    // 将属性和值添加到 JSON 对象节点
                    jsonNode.set(propertyName, valueNode);
                });
            }
        } else if (StringUtils.equals(type, PropertyConstant.ARRAY)) {
            JsonNode items = jsonSchemaNode.get(PropertyConstant.ITEMS);
            if (items != null) {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                items.forEach(item -> {
                    JsonNode valueNode = isPreview ? generateValueForPreview(null, item, processMap)
                            : generateValue(null, item, processMap);
                    arrayNode.add(valueNode);
                });
                return arrayNode;
            }
        }
        return jsonNode;
    }

    private static JsonNode generateValueForPreview(String propertyName, JsonNode propertyNode, Map<String, String> processMap) {
        // 获取属性类型
        if (propertyNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = getPropertyTextValue(propertyNode, PropertyConstant.TYPE);
        String value = getPropertyTextValue(propertyNode, PropertyConstant.EXAMPLE);
        return switch (type) {
            case PropertyConstant.STRING ->
                    new TextNode(StringUtils.isBlank(value) ? "string" : value);
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
            case PropertyConstant.OBJECT -> generateJson(propertyNode, processMap, true);
            case PropertyConstant.ARRAY -> {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                JsonNode items = propertyNode.get(PropertyConstant.ITEMS);
                if (items != null) {
                    items.forEach(item -> arrayNode.add(generateValueForPreview(null, item, processMap)));
                }
                yield arrayNode;
            }
            default -> NullNode.getInstance();
        };

    }

    private static JsonNode generateValue(String propertyName, JsonNode propertyNode, Map<String, String> processMap) {
        // 获取属性类型
        if (propertyNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = getPropertyTextValue(propertyNode, PropertyConstant.TYPE);
        String value = getPropertyTextValue(propertyNode, PropertyConstant.EXAMPLE);
        return switch (type) {
            case PropertyConstant.STRING -> {
                if (StringUtils.isBlank(value)) {
                    JsonNode enumValues = propertyNode.get(PropertyConstant.ENUM_VALUES);
                    JsonNode defaultValue = propertyNode.get(PropertyConstant.DEFAULT_VALUE);
                    JsonNode pattern = propertyNode.get(PropertyConstant.PATTERN);
                    JsonNode maxLength = propertyNode.get(PropertyConstant.MAX_LENGTH);
                    JsonNode minLength = propertyNode.get(PropertyConstant.MIN_LENGTH);
                    int max = isTextNotBlank(maxLength) ? maxLength.asInt() : 20;
                    int min = isTextNotBlank(minLength) ? minLength.asInt() : 1;
                    if (enumValues != null && enumValues instanceof ArrayNode) {
                        value = enumValues.get(new Random().nextInt(enumValues.size())).asText();
                    } else if (isTextNotBlank(defaultValue)) {
                        value = defaultValue.asText();
                    } else if (isTextNotBlank(pattern)) {
                        Xeger generator = new Xeger(pattern.asText());
                        value = generator.generate();
                    } else {
                        value = RandomStringGenerator.builder().withinRange('0', 'z').build().generate(new Random().nextInt(max - min + 1) + min);
                    }
                }
                yield new TextNode(value);
            }
            case PropertyConstant.INTEGER -> {
                if (StringUtils.isBlank(value)) {
                    JsonNode enumValues = propertyNode.get(PropertyConstant.ENUM_VALUES);
                    JsonNode defaultValue = propertyNode.get(PropertyConstant.DEFAULT_VALUE);
                    if (enumValues != null && enumValues instanceof ArrayNode) {
                        value = enumValues.get(new Random().nextInt(enumValues.size())).asText();
                    } else if (isTextNotBlank(defaultValue)) {
                        value = defaultValue.asText();
                    } else {
                        JsonNode maximum = propertyNode.get(PropertyConstant.MAXIMUM);
                        JsonNode minimum = propertyNode.get(PropertyConstant.MINIMUM);
                        int max = isTextNotBlank(maximum) ? maximum.asInt() : Integer.MAX_VALUE;
                        int min = isTextNotBlank(minimum) ? minimum.asInt() : Integer.MIN_VALUE;
                        // 这里减去负数可能超过整型最大值，使用 Long 类型
                        value = new Random().nextLong(Long.valueOf(max) - Long.valueOf(min)) + min + StringUtils.EMPTY;
                    }
                } else {
                    if (isVariable(value)) {
                        yield getJsonNodes(propertyName, processMap, value);
                    }
                }
                try {
                    yield new IntNode(Integer.valueOf(value));
                } catch (Exception e) {
                    yield new IntNode(propertyNode.get(PropertyConstant.EXAMPLE).asInt());
                }
            }
            case PropertyConstant.NUMBER -> {
                if (StringUtils.isBlank(value)) {
                    JsonNode enumValues = propertyNode.get(PropertyConstant.ENUM_VALUES);
                    JsonNode defaultValue = propertyNode.get(PropertyConstant.DEFAULT_VALUE);
                    if (enumValues != null && enumValues instanceof ArrayNode) {
                        value = enumValues.get(new Random().nextInt(enumValues.size())).asText();
                    } else if (isTextNotBlank(defaultValue)) {
                        value = defaultValue.asText();
                    } else {
                        JsonNode maximum = propertyNode.get(PropertyConstant.MAXIMUM);
                        JsonNode minimum = propertyNode.get(PropertyConstant.MINIMUM);
                        BigDecimal max = isTextNotBlank(maximum) ? new BigDecimal(maximum.asText()) : new BigDecimal(String.valueOf(Float.MAX_VALUE));
                        BigDecimal min = isTextNotBlank(minimum) ? new BigDecimal(minimum.asText()) : new BigDecimal(String.valueOf(Float.MIN_VALUE));
                        BigDecimal randomBigDecimal = min.add(new BigDecimal(String.valueOf(Math.random())).multiply(max.subtract(min)));
                        yield new DecimalNode(randomBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                } else {
                    if (isVariable(value)) {
                        yield getJsonNodes(propertyName, processMap, value);
                    }
                }
                try {
                    yield new DecimalNode(new BigDecimal(value));
                } catch (Exception e) {
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
            case PropertyConstant.OBJECT -> generateJson(propertyNode, processMap, true);
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

    private static boolean isTextNotBlank(JsonNode jsonNode) {
        return jsonNode != null && !(jsonNode instanceof NullNode) && StringUtils.isNotBlank(jsonNode.asText());
    }

    private static String getPropertyTextValue(JsonNode propertyNode, String key) {
        return propertyNode.get(key) == null ? StringUtils.EMPTY : propertyNode.get(key).asText();
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
        return jsonSchemaToJson(jsonSchema, true);
    }

    public static String jsonSchemaAutoGenerate(String jsonString) {
        return jsonSchemaToJson(jsonString, false);
    }
}
