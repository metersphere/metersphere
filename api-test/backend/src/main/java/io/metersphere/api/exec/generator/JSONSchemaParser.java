package io.metersphere.api.exec.generator;

import com.google.gson.*;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.EnumPropertyUtil;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JSONSchemaParser {

    private static void analyzeSchema(String json, JSONObject rootObj, Map<String, String> processMap) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        analyzeRootSchemaElement(rootElement, rootObj, processMap);
    }

    private static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj, Map<String, String> processMap) {
        if (rootElement.has(PropertyConstant.TYPE) || rootElement.has(PropertyConstant.ALL_OF)) {
            analyzeObject(rootElement, rootObj, processMap);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement, processMap);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj, Map<String, String> processMap) {
        if (object != null && object.has(PropertyConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(PropertyConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (!allOfElementObj.has(PropertyConstant.PROPERTIES)) {
                    continue;
                }
                formatObject(allOfElementObj, rootObj, processMap);
            }
        } else if (object.has(PropertyConstant.PROPERTIES)) {
            formatObject(object, rootObj, processMap);
        } else if (object.has(PropertyConstant.ADDITIONAL_PROPERTIES)) {
            analyzeProperty(rootObj, PropertyConstant.ADDITIONAL_PROPERTIES,
                    object.get(PropertyConstant.ADDITIONAL_PROPERTIES).getAsJsonObject(), processMap);
        } else if (object.has(PropertyConstant.TYPE)
                && object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.ARRAY)) {
            analyzeProperty(rootObj, PropertyConstant.MS_OBJECT, object, processMap);
        } else if (object.has(PropertyConstant.TYPE)
                && !object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.OBJECT)) {
            analyzeProperty(rootObj, object.getAsString(), object, processMap);
        }
    }

    private static void formatObject(JsonObject object, JSONObject rootObj, Map<String, String> processMap) {
        JsonObject propertiesObj = object.get(PropertyConstant.PROPERTIES).getAsJsonObject();
        for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
            String propertyKey = entry.getKey();
            JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
            analyzeProperty(rootObj, propertyKey, propertyObj, processMap);
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object, Map<String, String> processMap) {
        if (!object.has(PropertyConstant.TYPE)) {
            return;
        }
        String type = StringUtils.EMPTY;
        if (object.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
            type = object.get(PropertyConstant.TYPE).getAsString();
        }

        if (object.has(PropertyConstant.DEFAULT)) {
            concept.put(propertyName, object.get(PropertyConstant.DEFAULT).getAsString());
        } else if (object.has(PropertyConstant.ENUM)) {
            concept.put(propertyName, StringUtils.EMPTY);
            if (FormatterUtil.isMockValue(object)) {
                concept.put(propertyName, FormatterUtil.getElementValue(object));
            } else {
                List<Object> list = EnumPropertyUtil.analyzeEnumProperty(object);
                if (CollectionUtils.isNotEmpty(list)) {
                    int index = (int) (Math.random() * list.size());
                    concept.put(propertyName, list.get(index));
                }
            }
        } else if (StringUtils.equals(type, PropertyConstant.STRING)) {
            // 先设置空值
            concept.put(propertyName, StringUtils.EMPTY);
            if (object.has(PropertyConstant.DEFAULT)) {
                concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
            }
            if (FormatterUtil.isMockValue(object)) {
                String value = FormatterUtil.getStrValue(object);
                concept.put(propertyName, value);
            }
        } else if (StringUtils.equals(type, PropertyConstant.INTEGER)) {
            // 先设置空值
            concept.put(propertyName, 0);
            if (object.has(PropertyConstant.DEFAULT)) {
                concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
            }
            // 设置mock值
            if (FormatterUtil.isMockValue(object)) {
                if (FormatterUtil.isNumber(FormatterUtil.getStrValue(object))) {
                    int value = FormatterUtil.getElementValue(object).getAsInt();
                    concept.put(propertyName, value);
                } else {
                    String value = FormatterUtil.getStrValue(object);
                    JSONSchemaBuilder.processValue(concept, processMap, propertyName, value);
                }
            }
        } else if (StringUtils.equals(type, PropertyConstant.NUMBER)) {
            // 先设置空值
            concept.put(propertyName, 0);
            if (object.has(PropertyConstant.DEFAULT)) {
                concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
            }
            if (FormatterUtil.isMockValue(object)) {
                if (FormatterUtil.isNumber(FormatterUtil.getStrValue(object))) {
                    String value = FormatterUtil.getElementValue(object).getAsString();
                    if (value.indexOf(".") == -1) {
                        concept.put(propertyName, Long.valueOf(value));
                    } else {
                        concept.put(propertyName, new BigDecimal(value));
                    }
                } else {
                    JSONSchemaBuilder.processValue(concept, processMap, propertyName, FormatterUtil.getStrValue(object));
                }
            }
        } else if (StringUtils.equals(type, PropertyConstant.BOOLEAN)) {
            // 先设置空值
            concept.put(propertyName, false);
            if (object.has(PropertyConstant.DEFAULT)) {
                concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
            }
            if (FormatterUtil.isMockValue(object)) {
                String value = FormatterUtil.getStrValue(object);
                if (StringUtils.isNotEmpty(value)) {
                    if (value.indexOf("\"") != -1) {
                        value = value.replaceAll("\"", StringUtils.EMPTY);
                    }
                    concept.put(propertyName, Boolean.valueOf(value));
                }
            }
        } else if (StringUtils.equals(type, PropertyConstant.ARRAY)) {
            analyzeArray(concept, propertyName, object, processMap);
        } else if (StringUtils.equals(type, PropertyConstant.OBJECT)) {
            JSONObject obj = JSONUtil.createJsonObject(true);
            concept.put(propertyName, obj);
            analyzeObject(object, obj, processMap);
        } else if (StringUtils.equalsIgnoreCase(type, "null")) {
            concept.put(propertyName, JSONObject.NULL);
        } else {
            concept.put(propertyName, StringUtils.EMPTY);
        }
    }

    private static void analyzeArray(JSONObject concept, String propertyName, JsonObject object, Map<String, String> processMap) {
        JSONArray array = new JSONArray();
        if (object.has(PropertyConstant.ITEMS)) {
            if (object.get(PropertyConstant.ITEMS).isJsonArray()) {
                JsonArray jsonArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                loopArray(array, jsonArray, object, propertyName, processMap);
            } else {
                JsonObject itemsObject = object.get(PropertyConstant.ITEMS).getAsJsonObject();
                array.put(itemsObject);
            }
        }
        concept.put(propertyName, array);
    }

    private static void loopArray(JSONArray array, JsonArray jsonArray, JsonObject object, String propertyName, Map<String, String> processMap) {
        jsonArray.forEach(element -> {
            JsonObject jsonObject = element.getAsJsonObject();
            if (object.has(PropertyConstant.ITEMS)) {
                if (FormatterUtil.isMockValue(jsonObject)) {
                    if (jsonObject.has(PropertyConstant.TYPE)
                            && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.INTEGER)
                            && FormatterUtil.isNumber(FormatterUtil.getStrValue(jsonObject))) {
                        array.put(FormatterUtil.getElementValue(jsonObject).getAsInt());
                    } else {
                        array.put(FormatterUtil.getStrValue(jsonObject));
                    }
                } else if (jsonObject.has(PropertyConstant.ENUM)) {
                    array.put(EnumPropertyUtil.analyzeEnumProperty(jsonObject));
                } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.STRING)) {
                    if (jsonObject.has(PropertyConstant.DEFAULT)) {
                        array.put(jsonObject.get(PropertyConstant.DEFAULT));
                    } else if (FormatterUtil.isMockValue(jsonObject)) {
                        array.put(FormatterUtil.getStrValue(jsonObject));
                    } else {
                        array.put(StringUtils.EMPTY);
                    }
                } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.NUMBER)) {
                    if (jsonObject.has(PropertyConstant.DEFAULT)) {
                        array.put(jsonObject.get(PropertyConstant.DEFAULT));
                    } else if (FormatterUtil.isMockValue(jsonObject)) {
                        array.put(FormatterUtil.getStrValue(jsonObject));
                    } else {
                        array.put(0);
                    }
                } else if (jsonObject.has(PropertyConstant.PROPERTIES)) {
                    JSONObject propertyConcept = JSONUtil.createJsonObject(true);
                    formatObject(jsonObject, propertyConcept, processMap);
                    array.put(propertyConcept);

                } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                    JSONObject newJsonObj = JSONUtil.createJsonObject(true);
                    analyzeProperty(newJsonObj, propertyName + PropertyConstant.ITEM, jsonObject, processMap);
                    array.put(newJsonObj.get(propertyName + PropertyConstant.ITEM));
                }
            } else if (object.has(PropertyConstant.ITEMS) && object.get(PropertyConstant.ITEMS).isJsonArray()) {
                JsonArray itemsObjectArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                array.put(itemsObjectArray);
            }
        });
    }

    private static void analyzeDefinitions(JsonObject object, Map<String, String> processMap) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
            String definitionKey = entry.getKey();
            JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
            JSONObject obj = JSONUtil.createJsonObject(true);
            analyzeRootSchemaElement(definitionObj, obj, processMap);
        }
    }

    private static String formerJson(String jsonSchema) {
        JSONObject root = JSONUtil.createJsonObject(true);
        Map<String, String> processMap = new HashMap<>();
        String json;
        analyzeSchema(jsonSchema, root, processMap);
        // 格式化返回
        if (root.opt(PropertyConstant.MS_OBJECT) != null) {
            json = root.get(PropertyConstant.MS_OBJECT).toString();
        } else {
            json = root.toString();
        }
        if (MapUtils.isNotEmpty(processMap)) {
            for (String str : processMap.keySet()) {
                json = json.replace(str, processMap.get(str));
            }
        }
        return json;
    }

    public static String schemaToJson(String jsonSchema) {
        try {
            if (StringUtils.isEmpty(jsonSchema)) {
                return null;
            }
            String value = formerJson(jsonSchema);
            if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
                return JSONUtil.parserArray(value);
            } else {
                return JSONUtil.parserObject(value);
            }
        } catch (Exception ex) {
            return "Error";
        }
    }

    public static String preview(String jsonSchema) {
        try {
            String value = JSONSchemaBuilder.generator(jsonSchema);
            if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
                return JSONUtil.parserArray(value);
            } else {
                return JSONUtil.parserObject(value);
            }
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
