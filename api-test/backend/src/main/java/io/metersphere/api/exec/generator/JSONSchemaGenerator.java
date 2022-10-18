package io.metersphere.api.exec.generator;

import com.google.gson.*;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.EnumPropertyUtil;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class JSONSchemaGenerator {

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
        if (rootElement.has(PropertyConstant.TYPE) || rootElement.has(PropertyConstant.ALL_OF)) {
            analyzeObject(rootElement, rootObj);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj) {
        if (object != null && object.has(PropertyConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(PropertyConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (!allOfElementObj.has(PropertyConstant.PROPERTIES)) {
                    continue;
                }
                JsonObject propertiesObj = allOfElementObj.get(PropertyConstant.PROPERTIES).getAsJsonObject();
                for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                    String propertyKey = entry.getKey();
                    JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                    analyzeProperty(rootObj, propertyKey, propertyObj);
                }
            }
        } else if (object.has(PropertyConstant.PROPERTIES)) {
            JsonObject propertiesObj = object.get(PropertyConstant.PROPERTIES).getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(rootObj, propertyKey, propertyObj);
            }
        } else if (object.has(PropertyConstant.ADDITIONAL_PROPERTIES)) {
            JsonObject propertiesObj = object.get(PropertyConstant.ADDITIONAL_PROPERTIES).getAsJsonObject();
            analyzeProperty(rootObj, PropertyConstant.ADDITIONAL_PROPERTIES, propertiesObj);
        } else if (object.has(PropertyConstant.TYPE) && object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.ARRAY)) {
            analyzeProperty(rootObj, PropertyConstant.MS_OBJECT, object);
        } else if (object.has(PropertyConstant.TYPE) && !object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.OBJECT)) {
            analyzeProperty(rootObj, object.getAsString(), object);
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object) {
        if (object.has(PropertyConstant.TYPE)) {
            String propertyObjType = StringUtils.EMPTY;
            if (object.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                propertyObjType = object.get(PropertyConstant.TYPE).getAsString();
            } else if (object.get(PropertyConstant.TYPE) instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get(PropertyConstant.TYPE).getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
                if (typeArray.size() > 1) {
                    if (typeArray.get(1).getAsString().equals("null")) {
                        // 暂不处理，后续使用时再加
                    }
                }
            }
            if (object.has(PropertyConstant.DEFAULT)) {
                concept.put(propertyName, object.get(PropertyConstant.DEFAULT).getAsString());
            } else if (object.has(PropertyConstant.ENUM)) {
                try {
                    if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                        Object value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK);
                        concept.put(propertyName, value);
                    } else {
                        List<Object> list = EnumPropertyUtil.analyzeEnumProperty(object);
                        if (list.size() > 0) {
                            int index = (int) (Math.random() * list.size());
                            concept.put(propertyName, list.get(index));
                        }
                    }
                } catch (Exception e) {
                    concept.put(propertyName, StringUtils.EMPTY);
                }
            } else if (propertyObjType.equals(PropertyConstant.STRING)) {
                // 先设置空值
                concept.put(propertyName, StringUtils.EMPTY);
                if (object.has("format")) {
                    String propertyFormat = object.get("format").getAsString();
                    if (propertyFormat.equals("date-time")) {

                    }
                }
                if (object.has(PropertyConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                    concept.put(propertyName, value);
                }
            } else if (propertyObjType.equals(PropertyConstant.INTEGER)) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has(PropertyConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    try {
                        int value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsInt();
                        concept.put(propertyName, value);
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                        concept.put(propertyName, value);
                    }
                }

            } else if (propertyObjType.equals(PropertyConstant.NUMBER)) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has(PropertyConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    try {
                        Number value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsNumber();
                        if (value.toString().indexOf(".") == -1) {
                            concept.put(propertyName, value.longValue());
                        } else {
                            concept.put(propertyName, value.floatValue());
                        }
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                        concept.put(propertyName, value);
                    }
                }
            } else if (propertyObjType.equals(PropertyConstant.BOOLEAN)) {
                // 先设置空值
                concept.put(propertyName, false);
                if (object.has(PropertyConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(PropertyConstant.DEFAULT));
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).toString());
                    try {
                        if (StringUtils.isNotEmpty(value)) {
                            if (value.indexOf("\"") != -1) {
                                value = value.replaceAll("\"", StringUtils.EMPTY);
                            }
                            concept.put(propertyName, Boolean.valueOf(value));
                        }
                    } catch (Exception e) {
                        concept.put(propertyName, value);
                    }
                }
            } else if (propertyObjType.equals(PropertyConstant.ARRAY)) {
                // 先设置空值
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
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    if (object.has(PropertyConstant.ITEMS)) {
                        if (jsonObject.has(PropertyConstant.MOCK) && jsonObject.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                            try {
                                if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.INTEGER)) {
                                    int value = jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsInt();
                                    array.add(value);
                                } else {
                                    String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                                    array.add(value);
                                }
                            } catch (Exception e) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                                array.add(value);
                            }
                        } else if (jsonObject.has(PropertyConstant.ENUM)) {
                            array.add(EnumPropertyUtil.analyzeEnumProperty(jsonObject));
                        } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.STRING)) {
                            if (jsonObject.has(PropertyConstant.DEFAULT)) {
                                array.add(jsonObject.get(PropertyConstant.DEFAULT));
                            } else if (jsonObject.has(PropertyConstant.MOCK) && jsonObject.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                                array.add(value);
                            } else {
                                array.add(StringUtils.EMPTY);
                            }
                        } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.NUMBER)) {
                            if (jsonObject.has(PropertyConstant.DEFAULT)) {
                                array.add(jsonObject.get(PropertyConstant.DEFAULT));
                            } else if (jsonObject.has(PropertyConstant.MOCK) && jsonObject.get(PropertyConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                                array.add(value);
                            } else {
                                array.add(0);
                            }
                        } else if (jsonObject.has(PropertyConstant.PROPERTIES)) {
                            JSONObject propertyConcept = JSONUtil.createJsonObject(true);
                            JsonObject propertiesObj = jsonObject.get(PropertyConstant.PROPERTIES).getAsJsonObject();
                            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                                String propertyKey = entry.getKey();
                                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                                analyzeProperty(propertyConcept, propertyKey, propertyObj);
                            }
                            array.add(propertyConcept);

                        } else if (jsonObject.has(PropertyConstant.TYPE) && jsonObject.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                            JSONObject newJsonObj = JSONUtil.createJsonObject(true);
                            analyzeProperty(newJsonObj, propertyName + PropertyConstant.ITEM, jsonObject);
                            array.add(newJsonObj.get(propertyName + PropertyConstant.ITEM));
                        }
                    } else if (object.has(PropertyConstant.ITEMS) && object.get(PropertyConstant.ITEMS).isJsonArray()) {
                        JsonArray itemsObjectArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                        array.add(itemsObjectArray);
                    }
                }
                concept.put(propertyName, array);
            } else if (propertyObjType.equals(PropertyConstant.OBJECT)) {
                JSONObject obj = JSONUtil.createJsonObject(true);
                concept.put(propertyName, obj);
                analyzeObject(object, obj);
            } else if (StringUtils.equalsIgnoreCase(propertyObjType, "null")) {
                concept.put(propertyName, StringUtils.EMPTY);
            }
        }
    }


    private static void analyzeDefinitions(JsonObject object) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
            String definitionKey = entry.getKey();
            JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
            JSONObject obj = JSONUtil.createJsonObject(true);
            analyzeRootSchemaElement(definitionObj, obj);
        }
    }

    private static String formerJson(String jsonSchema) {
        JSONObject root = JSONUtil.createJsonObject(true);
        generator(jsonSchema, root);
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
            return formerJson(jsonSchema);
        } catch (Exception ex) {
            return jsonSchema;
        }
    }
}
