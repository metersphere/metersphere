package io.metersphere.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.metersphere.api.utils.regex.model.Node;
import io.metersphere.api.utils.regex.model.OrdinaryNode;
import io.metersphere.project.constants.PropertyConstant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

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
        if (jsonSchemaNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = getPropertyTextValue(jsonSchemaNode, PropertyConstant.TYPE);
        if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
            JsonNode propertiesNode = jsonSchemaNode.get(PropertyConstant.PROPERTIES);
            // 遍历 properties
            if (propertiesNode != null) {
                ObjectNode jsonNode = ApiDataUtils.createObjectNode();
                propertiesNode.fields().forEachRemaining(entry -> {
                    String propertyName = entry.getKey();
                    JsonNode propertyNode = entry.getValue();
                    // 根据属性类型生成对应的值
                    JsonNode valueNode = isPreview ? generateValueForPreview(entry.getKey(), propertyNode, processMap)
                            : generateValue(entry.getKey(), propertyNode, processMap);
                    // 将属性和值添加到 JSON 对象节点
                    jsonNode.set(propertyName, valueNode);
                });
                return jsonNode;
            }
        } else if (StringUtils.equals(type, PropertyConstant.ARRAY)) {
            JsonNode items = jsonSchemaNode.get(PropertyConstant.ITEMS);
            JsonNode maxItems = jsonSchemaNode.get(PropertyConstant.MAX_ITEMS);
            JsonNode minItems = jsonSchemaNode.get(PropertyConstant.MIN_ITEMS);
            if (items != null) {
                ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                if (isPreview) {
                    items.forEach(item -> arrayNode.add(generateValueForPreview(null, item, processMap)));
                } else {
                    int max = isTextNotBlank(maxItems) ? maxItems.asInt() : Integer.MAX_VALUE;
                    int min = isTextNotBlank(minItems) ? minItems.asInt() : 0;
                    // 自动生成数据，根据 minItems 和 maxItems 生成
                    int itemSize = Math.min(items.size(), max);
                    for (int i = 0; i < itemSize; i++) {
                        JsonNode itemNode = items.get(i);
                        JsonNode valueNode = generateValue(null, itemNode, processMap);
                        arrayNode.add(valueNode);
                    }
                    if (min > itemSize) {
                        for (int i = itemSize; i < min; i++) {
                            // 如果不足最小个数，则默认补充字符类型的数组项
                            TextNode itemNode = new TextNode(generateStr(8));
                            arrayNode.add(itemNode);
                        }
                    }
                }
                return arrayNode;
            }
        }
        return null;
    }

    private static JsonNode generateValueForPreview(String propertyName, JsonNode propertyNode, Map<String, String> processMap) {
        // 获取属性类型
        if (propertyNode instanceof NullNode) {
            return NullNode.getInstance();
        }
        String type = getPropertyTextValue(propertyNode, PropertyConstant.TYPE);
        String value = getPropertyTextValue(propertyNode, PropertyConstant.EXAMPLE);
        return switch (type) {
            case PropertyConstant.STRING -> new TextNode(StringUtils.isBlank(value) ? "string" : value);
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
            case PropertyConstant.ARRAY -> generateJson(propertyNode, processMap, true);
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
            case PropertyConstant.STRING -> new TextNode(generateStrValue(propertyNode, value));
            case PropertyConstant.INTEGER -> {
                if (StringUtils.isBlank(value)) {
                    JsonNode enumValues = propertyNode.get(PropertyConstant.ENUM_VALUES);
                    JsonNode defaultValue = propertyNode.get(PropertyConstant.DEFAULT_VALUE);
                    if (enumValues != null && enumValues instanceof ArrayNode) {
                        value = enumValues.get(new SecureRandom().nextInt(enumValues.size())).asText();
                    } else if (isTextNotBlank(defaultValue)) {
                        value = defaultValue.asText();
                    } else {
                        JsonNode maximum = propertyNode.get(PropertyConstant.MAXIMUM);
                        JsonNode minimum = propertyNode.get(PropertyConstant.MINIMUM);
                        int max = isTextNotBlank(maximum) ? maximum.asInt() : Integer.MAX_VALUE;
                        int min = isTextNotBlank(minimum) ? minimum.asInt() : Integer.MIN_VALUE;
                        // 这里减去负数可能超过整型最大值，使用 Long 类型
                        value = new SecureRandom().nextLong(Long.valueOf(max) - Long.valueOf(min)) + min + StringUtils.EMPTY;
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
                        value = enumValues.get(new SecureRandom().nextInt(enumValues.size())).asText();
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
            case PropertyConstant.OBJECT -> generateJson(propertyNode, processMap, false);
            case PropertyConstant.ARRAY -> generateJson(propertyNode, processMap, false);
            default -> NullNode.getInstance();
        };

    }

    private static String generateStrValue(JsonNode propertyNode, String value) {
        if (StringUtils.isBlank(value)) {
            JsonNode enumValues = propertyNode.get(PropertyConstant.ENUM_VALUES);
            JsonNode defaultValue = propertyNode.get(PropertyConstant.DEFAULT_VALUE);
            JsonNode pattern = propertyNode.get(PropertyConstant.PATTERN);
            JsonNode maxLength = propertyNode.get(PropertyConstant.MAX_LENGTH);
            JsonNode minLength = propertyNode.get(PropertyConstant.MIN_LENGTH);
            int max;
            int min;
            if (isTextNotBlank(maxLength) && isTextNotBlank(minLength)) {
                max = maxLength.asInt();
                min = minLength.asInt();
            } else if (isTextNotBlank(maxLength)) {
                // 只填了最大值，最小值默为 1
                max = maxLength.asInt();
                min = max > 0 ? 1 : 0;
            } else if (isTextNotBlank(minLength)) {
                // 只填了最小值，最大值默为最小值 + 10
                min = minLength.asInt();
                max = min + 10;
            } else {
                // 都没填，默认生成8位
                max = 8;
                min = 8;
            }
            if (enumValues != null && enumValues instanceof ArrayNode) {
                value = enumValues.get(new SecureRandom().nextInt(enumValues.size())).asText();
                if (isTextNotBlank(maxLength) && value.length() > max) {
                    value = value.substring(0, max);
                }
            } else if (isTextNotBlank(pattern)) {
                try {
                    Node node = new OrdinaryNode(pattern.asText());
                    value = node.random();
                } catch (Exception e) {
                    value = pattern.asText();
                }
            } else if (isTextNotBlank(defaultValue)) {
                value = defaultValue.asText();
                if (isTextNotBlank(maxLength) && value.length() > max) {
                    value = value.substring(0, max);
                }
                if (value.length() < min) {
                    value = value + generateStr(min - value.length());
                }
            } else {
                value = generateStr(new SecureRandom().nextInt(max - min + 1) + min);
            }
        }
        return value;
    }

    private static String generateStr(int length) {
        return RandomStringGenerator.builder().withinRange('0', 'z').build().generate(length);
    }

    private static boolean isTextNotBlank(JsonNode jsonNode) {
        return jsonNode != null && !(jsonNode instanceof NullNode) && StringUtils.isNotBlank(jsonNode.asText());
    }

    private static String getPropertyTextValue(JsonNode propertyNode, String key) {
        JsonNode jsonNode = propertyNode.get(key);
        return jsonNode == null || jsonNode instanceof NullNode ? StringUtils.EMPTY : jsonNode.asText();
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
