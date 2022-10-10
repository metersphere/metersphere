package io.metersphere.api.exec.generator;


import com.google.gson.*;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.EnumPropertyUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.util.NumberUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JSONSchemaRunTest {

    private static void generator(String json, JSONObject obj, Map<String, String> map) {
        analyzeSchema(json, obj, map);
    }

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

    private static boolean isMock(JsonObject itemsObject) {
        return (itemsObject.has(PropertyConstant.MOCK) && itemsObject.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString()));
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj, Map<String, String> map) {
        if (object.has(PropertyConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(PropertyConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has(PropertyConstant.PROPERTIES)) {
                    JsonObject propertiesObj = allOfElementObj.get(PropertyConstant.PROPERTIES).getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(rootObj, propertyKey, propertyObj, map);
                    }
                }
            }
        } else if (object.has(PropertyConstant.PROPERTIES)) {
            JsonObject propertiesObj = object.get(PropertyConstant.PROPERTIES).getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(rootObj, propertyKey, propertyObj, map);
            }
        } else if (object.has(PropertyConstant.TYPE) && object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.ARRAY)) {
            analyzeProperty(rootObj, PropertyConstant.MS_OBJECT, object, map);
        } else if (object.has(PropertyConstant.TYPE) && !object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.OBJECT)) {
            analyzeProperty(rootObj, object.getAsString(), object, map);
        }
    }

    public static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static void valueOf(String evlValue, String propertyName, JSONObject concept) {
        if (StringUtils.startsWith(evlValue, "@")) {
            String str = ScriptEngineUtils.calculate(evlValue);
            switch (evlValue) {
                case "@integer":
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
        } else {
            String value = ScriptEngineUtils.buildFunctionCallString(evlValue);
            concept.put(propertyName, value);
        }
    }

    private static void arrayValueOf(String evlValue, List<Object> array) {
        if (StringUtils.startsWith(evlValue, "@")) {
            String str = ScriptEngineUtils.calculate(evlValue);
            switch (evlValue) {
                case "@integer":
                    array.add(NumberUtils.parseNumber(str, Long.class));
                    break;
                case "@boolean":
                    array.add(Boolean.parseBoolean(str));
                    break;
                case "@float":
                    array.add(Float.parseFloat(str));
                    break;
                default:
                    array.add(str);
                    break;
            }
        } else {
            String value = ScriptEngineUtils.buildFunctionCallString(evlValue);
            array.add(value);
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
            concept.put(propertyName, getValue(object));
        } else if (propertyObjType.equals(PropertyConstant.INTEGER) || propertyObjType.equals(PropertyConstant.NUMBER)) {
            try {
                concept.put(propertyName, 0);
                if (isMock(object)) {
                    Number value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsNumber();
                    if (value.toString().indexOf(".") == -1) {
                        concept.put(propertyName, value.longValue());
                    } else {
                        concept.put(propertyName, value.floatValue());
                    }
                }
            } catch (Exception e) {
                valueOf(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString(), propertyName, concept);
            }
        } else if (propertyObjType.equals(PropertyConstant.BOOLEAN)) {
            // 先设置空值
            concept.put(propertyName, false);
            try {
                if (isMock(object)) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).toString());
                    if (StringUtils.isNotEmpty(value)) {
                        if (value.indexOf("\"") != -1) {
                            value = value.replaceAll("\"", "");
                        }
                        if (isBoolean(value)) {
                            concept.put(propertyName, Boolean.valueOf(value));
                        } else {
                            concept.put(propertyName, value);
                            map.put("\"" + propertyName + "\"" + ": \"" + value + "\"", "\"" + propertyName + "\"" + ":" + value);
                        }
                    }
                }
            } catch (Exception e) {
                concept.put(propertyName, false);
            }
        } else if (propertyObjType.equals(PropertyConstant.ARRAY)) {
            List<Object> array = new LinkedList<>();
            JsonArray jsonArray = new JsonArray();
            if (object.has(PropertyConstant.ITEMS)) {
                if (object.get(PropertyConstant.ITEMS).isJsonArray()) {
                    jsonArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                } else {
                    JsonObject itemsObject = object.get(PropertyConstant.ITEMS).getAsJsonObject();
                    array.add(itemsObject);
                }
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject itemsObject = jsonArray.get(i).getAsJsonObject();
                if (object.has(PropertyConstant.ITEMS)) {
                    if (isMock(itemsObject)) {
                        try {
                            String type = "";
                            if (itemsObject.has(PropertyConstant.TYPE)) {
                                type = itemsObject.get(PropertyConstant.TYPE).getAsString();
                            }
                            if (StringUtils.equalsIgnoreCase(type, PropertyConstant.STRING)) {
                                String value = itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString();
                                array.add(value);
                            } else if (StringUtils.equalsIgnoreCase(type, PropertyConstant.INTEGER)) {
                                int value = itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsInt();
                                array.add(value);
                            } else if (StringUtils.equalsIgnoreCase(type, PropertyConstant.NUMBER)) {
                                JsonElement valueObj = itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK);
                                Number value = valueObj.getAsNumber();
                                if (StringUtils.isNotEmpty(valueObj.getAsString()) && valueObj.getAsString().indexOf(".") != -1) {
                                    array.add(value.floatValue());
                                } else {
                                    array.add(value.longValue());
                                }
                            } else {
                                arrayValueOf(itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString(), array);
                            }
                        } catch (Exception e) {
                            arrayValueOf(itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString(), array);
                        }
                    } else if (itemsObject.has(PropertyConstant.TYPE) && (itemsObject.has(PropertyConstant.ENUM) || itemsObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.STRING))) {
                        array.add(getValue(itemsObject));
                    } else if (itemsObject.has(PropertyConstant.TYPE) && itemsObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.NUMBER)) {
                        if (isMock(itemsObject)) {
                            arrayValueOf(itemsObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString(), array);
                        } else {
                            array.add(0);
                        }
                    } else if (itemsObject.has(PropertyConstant.PROPERTIES)) {
                        JSONObject propertyConcept = new JSONObject(true);
                        JsonObject propertiesObj = itemsObject.get(PropertyConstant.PROPERTIES).getAsJsonObject();
                        for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                            String propertyKey = entry.getKey();
                            JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                            analyzeProperty(propertyConcept, propertyKey, propertyObj, map);
                        }
                        array.add(propertyConcept);

                    } else if (itemsObject.has(PropertyConstant.TYPE) && itemsObject.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                        JSONObject newJsonObj = new JSONObject();
                        analyzeProperty(newJsonObj, propertyName + PropertyConstant.ITEM, itemsObject, map);
                        array.add(newJsonObj.get(propertyName + PropertyConstant.ITEM));
                    }
                } else if (object.has(PropertyConstant.ITEMS) && object.get(PropertyConstant.ITEMS).isJsonArray()) {
                    JsonArray itemsObjectArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                    array.add(itemsObjectArray);
                }
            }
            concept.put(propertyName, array);
        } else if (propertyObjType.equals(PropertyConstant.OBJECT)) {
            JSONObject obj = new JSONObject();
            concept.put(propertyName, obj);
            analyzeObject(object, obj, map);
        } else if (StringUtils.equalsIgnoreCase(propertyObjType, "null")) {
            concept.put(propertyName, "");
        }
    }

    private static Object getValue(JsonObject object) {
        try {
            if (isMock(object)) {
                String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                return value;
            } else if (object.has(PropertyConstant.ENUM)) {
                List<Object> list = EnumPropertyUtil.analyzeEnumProperty(object);
                if (CollectionUtils.isNotEmpty(list)) {
                    int index = (int) (Math.random() * list.size());
                    return list.get(index);
                }
            }
        } catch (Exception e) {
            return object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK);
        }
        return "";
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
        generator(jsonSchema, root, map);
        // 格式化返回
        if (root.opt(PropertyConstant.MS_OBJECT) != null) {
            return root.get(PropertyConstant.MS_OBJECT).toString();
        }
        return root.toString();
    }

    public static String getJson(String jsonSchema) {
        try {
            if (StringUtils.isEmpty(jsonSchema)) {
                return null;
            }
            Map<String, String> map = new HashMap<>();
            String json = formerJson(jsonSchema, map);
            if (!map.isEmpty()) {
                for (String str : map.keySet()) {
                    json = json.replace(str, map.get(str));
                }
            }
            return json;
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
