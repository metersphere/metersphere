package io.metersphere.commons.json;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.*;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class JSONSchemaRunTest {

    private static void generator(String json, JSONObject obj) {
        analyzeSchema(json, obj);
    }

    private static void analyzeSchema(String json, JSONObject rootObj) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        analyzeRootSchemaElement(rootElement, rootObj);
    }

    private static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj) {
        if (rootElement.has(BasicConstant.TYPE) || rootElement.has(BasicConstant.ALL_OF)) {
            analyzeObject(rootElement, rootObj);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj) {
        if (object.has(BasicConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(BasicConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has(BasicConstant.PROPERTIES)) {
                    JsonObject propertiesObj = allOfElementObj.get(BasicConstant.PROPERTIES).getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(rootObj, propertyKey, propertyObj);
                    }
                }
            }
        } else if (object.has(BasicConstant.PROPERTIES)) {
            JsonObject propertiesObj = object.get(BasicConstant.PROPERTIES).getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(rootObj, propertyKey, propertyObj);
            }
        } else if (object.has(BasicConstant.TYPE) && object.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.ARRAY)) {
            analyzeProperty(rootObj, BasicConstant.MS_OBJECT, object);
        } else if (object.has(BasicConstant.TYPE) && !object.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.OBJECT)) {
            analyzeProperty(rootObj, object.getAsString(), object);
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object) {
        if (object.has(BasicConstant.TYPE)) {
            String propertyObjType = null;
            if (object.get(BasicConstant.TYPE) instanceof JsonPrimitive) {
                propertyObjType = object.get(BasicConstant.TYPE).getAsString();
            }
            if (propertyObjType.equals(BasicConstant.STRING) || propertyObjType.equals(BasicConstant.ENUM)) {
                concept.put(propertyName, getValue(object));
            } else if (propertyObjType.equals(BasicConstant.INTEGER) || propertyObjType.equals(BasicConstant.NUMBER)) {
                try {
                    concept.put(propertyName, 0);
                    if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                            && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                        Number value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsNumber();
                        if (value.toString().indexOf(".") == -1) {
                            concept.put(propertyName, value.longValue());
                        } else {
                            concept.put(propertyName, value.floatValue());
                        }
                    }
                } catch (Exception e) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                    concept.put(propertyName, value);
                }
            } else if (propertyObjType.equals(BasicConstant.BOOLEAN)) {
                // 先设置空值
                concept.put(propertyName, false);
                try {
                    if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                            && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).toString());

                        if (StringUtils.isNotEmpty(value)) {
                            if (value.indexOf("\"") != -1) {
                                value = value.replaceAll("\"", "");
                            }
                            concept.put(propertyName, Boolean.valueOf(value));
                        }
                    }
                } catch (Exception e) {
                    concept.put(propertyName, false);
                }
            } else if (propertyObjType.equals(BasicConstant.ARRAY)) {
                List<Object> array = new LinkedList<>();
                JsonArray jsonArray = new JsonArray();
                if (object.has("items")) {
                    if (object.get("items").isJsonArray()) {
                        jsonArray = object.get("items").getAsJsonArray();
                    } else {
                        JsonObject itemsObject = object.get("items").getAsJsonObject();
                        array.add(itemsObject);
                    }
                }

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject itemsObject = jsonArray.get(i).getAsJsonObject();
                    if (object.has("items")) {
                        if (itemsObject.has(BasicConstant.MOCK) && itemsObject.get(BasicConstant.MOCK).getAsJsonObject() != null
                                && StringUtils.isNotEmpty(itemsObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                            try {
                                int value = itemsObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsInt();
                                array.add(value);
                            } catch (Exception e) {
                                String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                array.add(value);
                            }
                        } else if (itemsObject.has(BasicConstant.TYPE) && (itemsObject.has(BasicConstant.ENUM) || itemsObject.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.STRING))) {
                            array.add(getValue(itemsObject));
                        } else if (itemsObject.has(BasicConstant.TYPE) && itemsObject.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.NUMBER)) {
                            if (itemsObject.has(BasicConstant.MOCK) && itemsObject.get(BasicConstant.MOCK).getAsJsonObject() != null
                                    && StringUtils.isNotEmpty(itemsObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                array.add(value);
                            } else {
                                array.add(0);
                            }
                        } else if (itemsObject.has(BasicConstant.PROPERTIES)) {
                            JSONObject propertyConcept = new JSONObject(true);
                            JsonObject propertiesObj = itemsObject.get(BasicConstant.PROPERTIES).getAsJsonObject();
                            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                                String propertyKey = entry.getKey();
                                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                                analyzeProperty(propertyConcept, propertyKey, propertyObj);
                            }
                            array.add(propertyConcept);

                        } else if (itemsObject.has(BasicConstant.TYPE) && itemsObject.get(BasicConstant.TYPE) instanceof JsonPrimitive) {
                            JSONObject newJsonObj = new JSONObject();
                            analyzeProperty(newJsonObj, propertyName + "_item", itemsObject);
                            array.add(newJsonObj.get(propertyName + "_item"));
                        }
                    } else if (object.has("items") && object.get("items").isJsonArray()) {
                        JsonArray itemsObjectArray = object.get("items").getAsJsonArray();
                        array.add(itemsObjectArray);
                    }
                }
                concept.put(propertyName, array);
            } else if (propertyObjType.equals(BasicConstant.OBJECT)) {
                JSONObject obj = new JSONObject();
                concept.put(propertyName, obj);
                analyzeObject(object, obj);
            } else if (StringUtils.equalsIgnoreCase(propertyObjType, "null")) {
                concept.put(propertyName, null);
            }
        }
    }

    private static Object getValue(JsonObject object) {
        try {
            if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                    && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                return value;
            }
        } catch (Exception e) {
            return object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK);
        }
        return "";
    }

    private static void analyzeDefinitions(JsonObject object) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        if (definitionsObj != null) {
            for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
                String definitionKey = entry.getKey();
                JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
                JSONObject obj = new JSONObject();
                analyzeRootSchemaElement(definitionObj, obj);
            }
        }
    }


    private static String formerJson(String jsonSchema) {
        JSONObject root = new JSONObject(true);
        generator(jsonSchema, root);
        // 格式化返回
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
        if (root.get(BasicConstant.MS_OBJECT) != null) {
            return gson.toJson(root.get(BasicConstant.MS_OBJECT));
        }
        return gson.toJson(root);
    }

    public static String getJson(String jsonSchema) {
        try {
            if (StringUtils.isEmpty(jsonSchema)) {
                return null;
            }
            return formerJson(jsonSchema);
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
