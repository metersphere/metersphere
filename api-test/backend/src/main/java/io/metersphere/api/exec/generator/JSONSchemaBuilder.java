package io.metersphere.api.exec.generator;


import com.google.gson.*;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.NumberUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JSONSchemaBuilder {
    private static void analyzeSchema(String json, JSONObject rootObj, Map<String, String> map) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        analyzeRootSchemaElement(rootElement, rootObj, map);
    }

    private static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj, Map<String, String> map) {
        if (rootElement.has(PropertyConstant.TYPE) || rootElement.has(PropertyConstant.ALL_OF)) {
            analyzeObject(rootElement, rootObj, map);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement, map);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj, Map<String, String> map) {
        if (object.has(PropertyConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(PropertyConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has(PropertyConstant.PROPERTIES)) {
                    formatObject(map, allOfElementObj, rootObj);
                }
            }
        } else if (object.has(PropertyConstant.PROPERTIES)) {
            formatObject(map, object, rootObj);
        } else if (object.has(PropertyConstant.TYPE)) {
            if (object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.ARRAY)) {
                analyzeProperty(rootObj, PropertyConstant.MS_OBJECT, object, map);
            } else if (!object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.OBJECT)) {
                analyzeProperty(rootObj, object.getAsString(), object, map);
            }
        }
    }

    public static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static boolean valueOf(String evlValue, String propertyName, JSONObject concept) {
        if (StringUtils.startsWith(evlValue, "@")) {
            String str = ScriptEngineUtils.calculate(evlValue);
            switch (evlValue) {
                case "@integer":
                case "@natural":
                    concept.put(propertyName, NumberUtils.parseNumber(str, Long.class));
                    break;
                case "@boolean":
                    concept.put(propertyName, Boolean.parseBoolean(str));
                    break;
                case "@float":
                    concept.put(propertyName, Float.parseFloat(str));
                    break;
                default:
                    concept.put(propertyName, str);
                    break;
            }
            return false;
        } else {
            concept.put(propertyName, ScriptEngineUtils.buildFunctionCallString(evlValue));
            return true;
        }
    }

    private static void arrayValueOf(String evlValue, JSONArray array) {
        if (StringUtils.startsWith(evlValue, "@")) {
            String str = ScriptEngineUtils.calculate(evlValue);
            switch (evlValue) {
                case "@integer":
                    array.put(NumberUtils.parseNumber(str, Long.class));
                    break;
                case "@boolean":
                    array.put(Boolean.parseBoolean(str));
                    break;
                case "@float":
                    array.put(Float.parseFloat(str));
                    break;
                default:
                    array.put(str);
                    break;
            }
        } else {
            array.put(ScriptEngineUtils.buildFunctionCallString(evlValue));
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object, Map<String, String> map) {
        if (!object.has(PropertyConstant.TYPE)) {
            return;
        }
        String propertyObjType = null;
        if (object.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
            propertyObjType = object.get(PropertyConstant.TYPE).getAsString();
        }
        if (propertyObjType.equals(PropertyConstant.STRING) || propertyObjType.equals(PropertyConstant.ENUM)) {
            concept.put(propertyName, FormatterUtil.getMockValue(object));
        } else if (propertyObjType.equals(PropertyConstant.INTEGER) || propertyObjType.equals(PropertyConstant.NUMBER)) {
            try {
                concept.put(propertyName, 0);
                if (FormatterUtil.isMockValue(object)) {
                    Number value = FormatterUtil.getElementValue(object).getAsNumber();
                    if (value.toString().indexOf(".") == -1) {
                        concept.put(propertyName, value.longValue());
                    } else {
                        concept.put(propertyName, new BigDecimal(object.getAsString()));
                    }
                }
            } catch (Exception e) {
                boolean hasValue = valueOf(FormatterUtil.getElementValue(object).getAsString(), propertyName, concept);
                if (hasValue) {
                    String value = FormatterUtil.getElementValue(object).getAsString();
                    processValue(concept, map, propertyName, value);
                }
            }
        } else if (propertyObjType.equals(PropertyConstant.BOOLEAN)) {
            // 先设置空值
            concept.put(propertyName, false);
            try {
                if (FormatterUtil.isMockValue(object)) {
                    String value = FormatterUtil.getMockValue(object);
                    if (StringUtils.isNotEmpty(value)) {
                        if (value.indexOf("\"") != -1) {
                            value = value.replaceAll("\"", StringUtils.EMPTY);
                        }
                        if (isBoolean(value)) {
                            concept.put(propertyName, Boolean.valueOf(value));
                        } else {
                            processValue(concept, map, propertyName, value);
                        }
                    }
                }
            } catch (Exception e) {
                concept.put(propertyName, false);
            }
        } else if (propertyObjType.equals(PropertyConstant.ARRAY)) {
            analyzeArray(concept, propertyName, object, map);
        } else if (propertyObjType.equals(PropertyConstant.OBJECT)) {
            JSONObject obj = new JSONObject();
            concept.put(propertyName, obj);
            analyzeObject(object, obj, map);
        } else if (StringUtils.equalsIgnoreCase(propertyObjType, "null")) {
            concept.put(propertyName, JSONObject.NULL);
        }
    }

    public static void processValue(JSONObject concept, Map<String, String> map, String propertyName, String value) {
        concept.put(propertyName, value);
        String key = StringUtils.join("\"", propertyName, "\"", ":\"", value, "\"");
        String targetValue = StringUtils.join("\"", propertyName, "\"", ":", value);
        map.put(key, targetValue);
    }

    public static void processArrayValue(Map<String, String> map, String value) {
        String key = StringUtils.join("\"", value, "\"");
        String targetValue = StringUtils.join(value);
        map.put(key, targetValue);
    }

    private static void analyzeArray(JSONObject concept, String propertyName, JsonObject object, Map<String, String> map) {
        JSONArray array = new JSONArray();
        if (object.has(PropertyConstant.ITEMS)) {
            if (object.get(PropertyConstant.ITEMS).isJsonArray()) {
                JsonArray jsonArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                loopArray(jsonArray, array, propertyName, object, map);
            } else {
                array.put(object.get(PropertyConstant.ITEMS).getAsJsonObject());
            }
        }
        concept.put(propertyName, array);
    }

    private static void loopArray(JsonArray sourceArray, JSONArray targetArray, String propertyName, JsonObject object, Map<String, String> map) {
        sourceArray.forEach(element -> {
            JsonObject itemsObject = element.getAsJsonObject();
            if (object.has(PropertyConstant.ITEMS)) {
                if (FormatterUtil.isMockValue(itemsObject)) {
                    formatItems(itemsObject, targetArray, map);
                } else if (itemsObject.has(PropertyConstant.TYPE) && (itemsObject.has(PropertyConstant.ENUM) || itemsObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.STRING))) {
                    targetArray.put(FormatterUtil.getMockValue(itemsObject));
                } else if (itemsObject.has(PropertyConstant.TYPE) && itemsObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.NUMBER)) {
                    if (FormatterUtil.isMockValue(itemsObject)) {
                        arrayValueOf(FormatterUtil.getStrValue(itemsObject), targetArray);
                    } else {
                        targetArray.put(0);
                    }
                } else if (itemsObject.has(PropertyConstant.PROPERTIES)) {
                    JSONObject propertyConcept = new JSONObject(true);
                    formatObject(map, itemsObject, propertyConcept);
                    targetArray.put(propertyConcept);

                } else if (itemsObject.has(PropertyConstant.TYPE) && itemsObject.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                    JSONObject newJsonObj = new JSONObject();
                    analyzeProperty(newJsonObj, propertyName + PropertyConstant.ITEM, itemsObject, map);
                    targetArray.put(newJsonObj.get(propertyName + PropertyConstant.ITEM));
                }
            } else if (object.has(PropertyConstant.ITEMS) && object.get(PropertyConstant.ITEMS).isJsonArray()) {
                JsonArray itemsObjectArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                targetArray.put(itemsObjectArray);
            }
        });
    }

    private static void formatItems(JsonObject itemsObject, JSONArray array, Map<String, String> map) {
        String type = StringUtils.EMPTY;
        if (itemsObject.has(PropertyConstant.TYPE)) {
            type = itemsObject.get(PropertyConstant.TYPE).getAsString();
        }
        try {
            if (StringUtils.equalsIgnoreCase(type, PropertyConstant.STRING)) {
                String value = FormatterUtil.getStrValue(itemsObject);
                array.put(value);
            } else if (StringUtils.equalsIgnoreCase(type, PropertyConstant.INTEGER)) {
                int value = FormatterUtil.getElementValue(itemsObject).getAsInt();
                array.put(value);
            } else if (StringUtils.equalsIgnoreCase(type, PropertyConstant.NUMBER)) {
                JsonElement valueObj = FormatterUtil.getElementValue(itemsObject);
                String value = valueObj.getAsString();
                if (StringUtils.isNotEmpty(valueObj.getAsString()) && valueObj.getAsString().indexOf(".") != -1) {
                    array.put(new BigDecimal(value));
                } else {
                    array.put(Integer.valueOf(value));
                }
            } else {
                arrayValueOf(FormatterUtil.getStrValue(itemsObject), array);
            }
        } catch (Exception e) {
            arrayValueOf(FormatterUtil.getStrValue(itemsObject), array);
            if (StringUtils.equalsAnyIgnoreCase(type, PropertyConstant.INTEGER, PropertyConstant.NUMBER)) {
                String value = FormatterUtil.getElementValue(itemsObject).getAsString();
                processArrayValue(map, value);
            }
        }
    }

    private static void formatObject(Map<String, String> map, JsonObject jsonObject, JSONObject concept) {
        JsonObject propertiesObj = jsonObject.get(PropertyConstant.PROPERTIES).getAsJsonObject();
        for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
            String propertyKey = entry.getKey();
            JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
            analyzeProperty(concept, propertyKey, propertyObj, map);
        }
    }

    private static void analyzeDefinitions(JsonObject object, Map<String, String> map) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        if (definitionsObj != null) {
            for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
                String definitionKey = entry.getKey();
                JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
                JSONObject obj = new JSONObject();
                analyzeRootSchemaElement(definitionObj, obj, map);
            }
        }
    }


    private static String formerJson(String jsonSchema, Map<String, String> map) {
        JSONObject root = new JSONObject(true);
        analyzeSchema(jsonSchema, root, map);
        // 格式化返回
        if (root.opt(PropertyConstant.MS_OBJECT) != null) {
            return root.get(PropertyConstant.MS_OBJECT).toString();
        }
        return root.toString();
    }

    public static String generator(String jsonSchema) {
        try {
            if (StringUtils.isEmpty(jsonSchema)) {
                return null;
            }
            Map<String, String> processMap = new HashMap<>();
            String json = formerJson(jsonSchema, processMap);
            if (MapUtils.isNotEmpty(processMap)) {
                for (String str : processMap.keySet()) {
                    json = json.replace(str, processMap.get(str));
                }
            }
            json = StringUtils.chomp(json.trim());
            if (StringUtils.startsWith(json, "[") && StringUtils.endsWith(json, "]")) {
                return JSONUtil.parserArray(json);
            } else {
                return JSONUtil.parserObject(json);
            }
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
