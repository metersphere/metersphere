package io.metersphere.api.exec.generator;

import com.apifan.common.random.source.DateTimeSource;
import com.apifan.common.random.source.InternetSource;
import com.apifan.common.random.source.NumberSource;
import com.google.gson.*;
import com.mifmif.common.regex.Generex;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * 生成测试数据
 */
public class TestDataGenerator {
    private static final String TYPE = "type";
    private static final String ALL_OF = "allOf";
    private static final String DEFINITIONS = "definitions";
    private static final String PROPERTIES = "properties";
    private static final String ARRAY = "array";
    private static final String OBJECT = "object";
    private static final String MS_OBJECT = "MS-OBJECT";
    private static final String ITEMS = "items";
    private static final String ENUM = "enum";
    private static final String STRING = "string";
    private static final String DEFAULT = "default";
    private static final String MOCK = "mock";
    private static final String MAXLENGTH = "maxLength";
    private static final String MINLENGTH = "minLength";
    private static final String FORMAT = "format";
    private static final String PATTERN = "pattern";
    private static final String INTEGER = "integer";
    private static final String NUMBER = "number";
    private static final String BOOLEAN = "boolean";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";

    public static void analyzeSchema(String json, JSONObject rootObj) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        analyzeRootSchemaElement(rootElement, rootObj);
    }

    public static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj) {
        if ((rootElement.has(TYPE) || rootElement.has(ALL_OF)) && rootElement != null) {
            analyzeObject(rootElement, rootObj);
        }
        if (rootElement.has(DEFINITIONS)) {
            analyzeDefinitions(rootElement);
        }
    }

    public static void analyzeObject(JsonObject object, JSONObject rootObj) {
        if (object.has(ALL_OF)) {
            for (JsonElement el : object.get(ALL_OF).getAsJsonArray()) {
                JsonObject elObj = el.getAsJsonObject();
                if (elObj.has(PROPERTIES)) {
                    analyzeProperties(rootObj, elObj);
                }
            }
        } else if (object.has(PROPERTIES)) {
            analyzeProperties(rootObj, object);
        } else if (object.has(TYPE) && object.get(TYPE).getAsString().equals(ARRAY)) {
            analyzeProperty(rootObj, MS_OBJECT, object);
        } else if (object.has(TYPE) && !object.get(TYPE).getAsString().equals(OBJECT)) {
            analyzeProperty(rootObj, object.getAsString(), object);
        }
    }

    private static void analyzeProperties(JSONObject rootObj, JsonObject allOfElementObj) {
        JsonObject propertiesObj = allOfElementObj.get(PROPERTIES).getAsJsonObject();
        for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
            String propertyKey = entry.getKey();
            JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
            analyzeProperty(rootObj, propertyKey, propertyObj);
        }
    }

    public static void analyzeItems(JSONObject concept, String propertyName, JsonObject object) {
        // 先设置空值
        List<Object> array = new LinkedList<>();
        JsonArray jsonArray = new JsonArray();
        if (object.has(ITEMS) && object.get(ITEMS).isJsonArray()) {
            jsonArray = object.get(ITEMS).getAsJsonArray();
        } else {
            JsonObject itemsObject = object.get(ITEMS).getAsJsonObject();
            array.add(itemsObject);
        }

        for (JsonElement element : jsonArray) {
            JsonObject itemsObject = element.getAsJsonObject();
            if (object.has(ITEMS)) {
                if (itemsObject.has(ENUM)) {
                    array.add(analyzeEnumProperty(itemsObject));
                } else if (itemsObject.has(TYPE) && itemsObject.get(TYPE).getAsString().equals(STRING)) {
                    array.add(analyzeString(itemsObject));
                } else if (itemsObject.has(PROPERTIES)) {
                    JSONObject propertyConcept = new JSONObject();
                    analyzeProperties(propertyConcept, itemsObject);
                    array.add(propertyConcept);
                } else if (itemsObject.has(TYPE) && itemsObject.get(TYPE) instanceof JsonPrimitive) {
                    JSONObject newJsonObj = new JSONObject();
                    analyzeProperty(newJsonObj, propertyName + "_item", itemsObject);
                    array.add(newJsonObj.get(propertyName + "_item"));
                }
            } else if (object.has(ITEMS) && object.get(ITEMS).isJsonArray()) {
                JsonArray itemsObjectArray = object.get(ITEMS).getAsJsonArray();
                array.add(itemsObjectArray);
            }
        }
        concept.put(propertyName, array);
    }

    public static String getMockValue(JsonObject object) {
        if (object.has(MOCK)
                && object.get(MOCK).getAsJsonObject() != null
                && object.get(MOCK).getAsJsonObject().get(MOCK) != null
                && StringUtils.isNotBlank(object.get(MOCK).getAsJsonObject().get(MOCK).getAsString())) {
            if (StringUtils.startsWithAny(object.get(MOCK).getAsJsonObject().get(MOCK).getAsString(), "@", "${")) {
                return ScriptEngineUtils.calculate(object.get(MOCK).getAsJsonObject().get(MOCK).getAsString());
            }
        }
        return null;
    }

    public static String analyzeString(JsonObject object) {
        // 先设置空值
        if (object.has(DEFAULT)) {
            return object.get(DEFAULT).getAsString();
        }
        Object mockValue = getMockValue(object);
        if (mockValue != null) {
            return mockValue.toString();
        }
        int maxLength = 9;
        if (object.has(MAXLENGTH)) {
            maxLength = object.get(MAXLENGTH).getAsInt();
        }
        int minLength = 0;
        if (object.has(MINLENGTH)) {
            minLength = object.get(MINLENGTH).getAsInt();
        }
        String value = minLength > maxLength ? mockValue.toString() : RandomStringUtils.randomAlphanumeric(minLength, maxLength);
        Object enumObj = analyzeEnumProperty(object);
        String v = enumObj == null ? "" : String.valueOf(enumObj);
        value = StringUtils.isNotBlank(v) ? v : value;
        try {
            if (object.has(FORMAT)) {
                value = switch (object.get(FORMAT).getAsString()) {
                    case "date-time" -> DateTimeSource.getInstance().randomTimestamp(LocalDate.now()) + "";
                    case "date" -> DateTimeSource.getInstance().randomDate(LocalDate.now().getYear(), "yyyy-MM-dd");
                    case "email" -> InternetSource.getInstance().randomEmail(maxLength);
                    case "hostname" -> InternetSource.getInstance().randomDomain(maxLength);
                    case "ipv4" -> InternetSource.getInstance().randomPublicIpv4();
                    case "ipv6" -> InternetSource.getInstance().randomIpV6();
                    case "uri" -> InternetSource.getInstance().randomStaticUrl("jpg");
                    default -> value;
                };
            } else if (object.has(PATTERN)) {
                String pattern = object.get(PATTERN).getAsString();
                if (StringUtils.isNotEmpty(pattern)) {
                    Generex generex = new Generex(pattern);
                    value = generex.random();
                }
            }
            return value;
        } catch (Exception e) {
            return value;
        }
    }

    public static Object analyzeInteger(JsonObject object) {
        // 先设置空值
        if (object.has(DEFAULT) && FormatterUtil.isNumber(object.get(DEFAULT))) {
            return object.get(DEFAULT).getAsInt();
        }
        Object mockValue = getMockValue(object);
        if (mockValue != null && FormatterUtil.isNumber(mockValue)) {
            return Integer.parseInt(mockValue.toString());
        }
        int minimum = 1;
        int maximum = 101;
        if (object.has(MINIMUM)) {
            minimum = object.get(MINIMUM).getAsInt() < 0 ? 0 : object.get(MINIMUM).getAsInt();
        }
        if (object.has(MAXIMUM)) {
            maximum = object.get(MAXIMUM).getAsInt();
        }
        return NumberSource.getInstance().randomInt(minimum, maximum);
    }

    public static Object analyzeNumber(JsonObject object) {
        if (object != null && object.has(DEFAULT) && FormatterUtil.isNumber(object.has(DEFAULT))) {
            return object.get(DEFAULT).getAsFloat();
        }
        Object mockValue = getMockValue(object);
        if (mockValue != null && FormatterUtil.isNumber(mockValue)) {
            return Float.parseFloat(mockValue.toString());
        }
        float maximum = 200001.0f;
        float minimum = 100000.0f;
        if (object.has(MINIMUM)) {
            float min = object.get(MINIMUM).getAsFloat();
            minimum = min < 0 ? 0 : min;
        }
        if (object.has(MAXIMUM)) {
            float max = object.get(MAXIMUM).getAsFloat();
            maximum = max > 0 ? max : maximum;
        }
        return NumberSource.getInstance().randomDouble(minimum, maximum);
    }

    public static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    public static Boolean analyzeBoolean(JsonObject object) {
        if (object.has(DEFAULT)) {
            return object.get(DEFAULT).getAsBoolean();
        }
        return getRandomBoolean();
    }

    public static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object) {
        if (object != null && object.has(TYPE)) {
            String propertyObjType = getPropertyObjType(object);
            if (object.has(DEFAULT)) {
                concept.put(propertyName, object.get(DEFAULT).getAsString());
            } else if (object.has(ENUM)) {
                concept.put(propertyName, analyzeEnumProperty(object));
            } else if (propertyObjType.equals(STRING)) {
                concept.put(propertyName, analyzeString(object));
            } else if (propertyObjType.equals(INTEGER)) {
                concept.put(propertyName, analyzeInteger(object));
            } else if (propertyObjType.equals(NUMBER)) {
                concept.put(propertyName, analyzeNumber(object));
            } else if (propertyObjType.equals(BOOLEAN)) {
                concept.put(propertyName, analyzeBoolean(object));
            } else if (propertyObjType.equals(ARRAY)) {
                analyzeItems(concept, propertyName, object);
            } else if (propertyObjType.equals(OBJECT)) {
                JSONObject obj = new JSONObject();
                concept.put(propertyName, obj);
                analyzeObject(object, obj);
            }
        }
    }

    public static String getPropertyObjType(JsonObject object) {
        if (object.get(TYPE) != null && object.get(TYPE) instanceof JsonPrimitive) {
            return object.get(TYPE).getAsString();
        } else if (object.get(TYPE) instanceof JsonArray) {
            JsonArray typeArray = object.get(TYPE).getAsJsonArray();
            return typeArray.get(0).getAsString();
        }
        return null;
    }

    public static Object analyzeEnumProperty(JsonObject object) {
        Object enumValue = "";
        try {
            if (object.get(ENUM) != null) {
                String enums = object.get(ENUM).getAsString();
                if (StringUtils.isNotBlank(enums)) {
                    String enumArr[] = enums.split("\n");
                    int index = (int) (Math.random() * enumArr.length);
                    enumValue = enumArr[index];
                }
            }
            String propertyObjType = getPropertyObjType(object);
            if (propertyObjType.equals(INTEGER)) {
                enumValue = Integer.parseInt(enumValue.toString());
            } else if (propertyObjType.equals(NUMBER)) {
                enumValue = Float.parseFloat(enumValue.toString());
            }
        } catch (Exception e) {
            return enumValue;
        }
        return enumValue;
    }

    public static void analyzeDefinitions(JsonObject object) {
        JsonObject definitionsObj = object.get(DEFINITIONS).getAsJsonObject();
        for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
            String definitionKey = entry.getKey();
            JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
            JSONObject obj = new JSONObject();
            analyzeRootSchemaElement(definitionObj, obj);
        }
    }

    public static String generator(String jsonSchema) {
        try {
            if (StringUtils.isEmpty(jsonSchema)) {
                return null;
            }
            JSONObject root = new JSONObject();
            analyzeSchema(jsonSchema, root);
            // 格式化返回
            if (root != null && root.has(MS_OBJECT)) {
                return root.get(MS_OBJECT).toString();
            }
            return root.toString();
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
