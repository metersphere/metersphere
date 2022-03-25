package io.metersphere.commons.json;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.*;
import io.metersphere.commons.utils.EnumPropertyUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;

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
        if (rootElement.has(BasicConstant.TYPE) || rootElement.has(BasicConstant.ALL_OF)) {
            analyzeObject(rootElement, rootObj);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj) {
        if (object != null && object.has(BasicConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(BasicConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (!allOfElementObj.has(BasicConstant.PROPERTIES)) {
                    continue;
                }
                JsonObject propertiesObj = allOfElementObj.get(BasicConstant.PROPERTIES).getAsJsonObject();
                for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                    String propertyKey = entry.getKey();
                    JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                    analyzeProperty(rootObj, propertyKey, propertyObj);
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
            String propertyObjType = "";
            if (object.get(BasicConstant.TYPE) instanceof JsonPrimitive) {
                propertyObjType = object.get(BasicConstant.TYPE).getAsString();
            } else if (object.get(BasicConstant.TYPE) instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get(BasicConstant.TYPE).getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
                if (typeArray.size() > 1) {
                    if (typeArray.get(1).getAsString().equals("null")) {
                        // 暂不处理，后续使用时再加
                    }
                }
            }
            if (object.has(BasicConstant.DEFAULT)) {
                concept.put(propertyName, object.get(BasicConstant.DEFAULT));
            } else if (object.has(BasicConstant.ENUM)) {
                try {
                    if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                        Object value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK);
                        concept.put(propertyName, value);
                    } else {
                        List<Object> list = EnumPropertyUtil.analyzeEnumProperty(object);
                        if (list.size() > 0) {
                            int index = (int) (Math.random() * list.size());
                            concept.put(propertyName, list.get(index));
                        }
                    }
                } catch (Exception e) {
                    concept.put(propertyName, "");
                }
            } else if (propertyObjType.equals(BasicConstant.STRING)) {
                // 先设置空值
                concept.put(propertyName, "");
                if (object.has("format")) {
                    String propertyFormat = object.get("format").getAsString();
                    if (propertyFormat.equals("date-time")) {

                    }
                }
                if (object.has(BasicConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(BasicConstant.DEFAULT));
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                    concept.put(propertyName, value);
                }
            } else if (propertyObjType.equals(BasicConstant.INTEGER)) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has(BasicConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(BasicConstant.DEFAULT));
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    try {
                        int value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsInt();
                        concept.put(propertyName, value);
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                        concept.put(propertyName, value);
                    }
                }

            } else if (propertyObjType.equals(BasicConstant.NUMBER)) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has(BasicConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(BasicConstant.DEFAULT));
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    try {
                        Number value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsNumber();
                        if (value.toString().indexOf(".") == -1) {
                            concept.put(propertyName, value.longValue());
                        } else {
                            concept.put(propertyName, value.floatValue());
                        }
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                        concept.put(propertyName, value);
                    }
                }
            } else if (propertyObjType.equals(BasicConstant.BOOLEAN)) {
                // 先设置空值
                concept.put(propertyName, false);
                if (object.has(BasicConstant.DEFAULT)) {
                    concept.put(propertyName, object.get(BasicConstant.DEFAULT));
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).toString());
                    try {
                        if (StringUtils.isNotEmpty(value)) {
                            if (value.indexOf("\"") != -1) {
                                value = value.replaceAll("\"", "");
                            }
                            concept.put(propertyName, Boolean.valueOf(value));
                        }
                    } catch (Exception e) {
                        concept.put(propertyName, value);
                    }
                }
            } else if (propertyObjType.equals(BasicConstant.ARRAY)) {
                // 先设置空值
                List<Object> array = new LinkedList<>();
                JsonArray jsonArray = new JsonArray();
                if (object.has(BasicConstant.ITEMS)) {
                    if (object.get(BasicConstant.ITEMS).isJsonArray()) {
                        jsonArray = object.get(BasicConstant.ITEMS).getAsJsonArray();
                    } else {
                        JsonObject itemsObject = object.get(BasicConstant.ITEMS).getAsJsonObject();
                        array.add(itemsObject);
                    }
                }
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    if (object.has(BasicConstant.ITEMS)) {
                        if (jsonObject.has(BasicConstant.MOCK) && jsonObject.get(BasicConstant.MOCK).getAsJsonObject() != null
                                && StringUtils.isNotEmpty(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                            try {
                                if (jsonObject.has(BasicConstant.TYPE) && jsonObject.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.INTEGER)) {
                                    int value = jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsInt();
                                    array.add(value);
                                } else {
                                    String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                    array.add(value);
                                }
                            } catch (Exception e) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                array.add(value);
                            }
                        } else if (jsonObject.has(BasicConstant.ENUM)) {
                            array.add(EnumPropertyUtil.analyzeEnumProperty(jsonObject));
                        } else if (jsonObject.has(BasicConstant.TYPE) && jsonObject.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.STRING)) {
                            if (jsonObject.has(BasicConstant.DEFAULT)) {
                                array.add(jsonObject.get(BasicConstant.DEFAULT));
                            } else if (jsonObject.has(BasicConstant.MOCK) && jsonObject.get(BasicConstant.MOCK).getAsJsonObject() != null && StringUtils.isNotEmpty(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                array.add(value);
                            } else {
                                array.add("");
                            }
                        } else if (jsonObject.has(BasicConstant.TYPE) && jsonObject.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.NUMBER)) {
                            if (jsonObject.has(BasicConstant.DEFAULT)) {
                                array.add(jsonObject.get(BasicConstant.DEFAULT));
                            } else if (jsonObject.has(BasicConstant.MOCK) && jsonObject.get(BasicConstant.MOCK).getAsJsonObject() != null
                                    && StringUtils.isNotEmpty(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(jsonObject.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                                array.add(value);
                            } else {
                                array.add(0);
                            }
                        } else if (jsonObject.has(BasicConstant.PROPERTIES)) {
                            JSONObject propertyConcept = new JSONObject(true);
                            JsonObject propertiesObj = jsonObject.get(BasicConstant.PROPERTIES).getAsJsonObject();
                            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                                String propertyKey = entry.getKey();
                                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                                analyzeProperty(propertyConcept, propertyKey, propertyObj);
                            }
                            array.add(propertyConcept);

                        } else if (jsonObject.has(BasicConstant.TYPE) && jsonObject.get(BasicConstant.TYPE) instanceof JsonPrimitive) {
                            JSONObject newJsonObj = new JSONObject();
                            analyzeProperty(newJsonObj, propertyName + BasicConstant.ITEM, jsonObject);
                            array.add(newJsonObj.get(propertyName + BasicConstant.ITEM));
                        }
                    } else if (object.has(BasicConstant.ITEMS) && object.get(BasicConstant.ITEMS).isJsonArray()) {
                        JsonArray itemsObjectArray = object.get(BasicConstant.ITEMS).getAsJsonArray();
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


    private static void analyzeDefinitions(JsonObject object) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
            String definitionKey = entry.getKey();
            JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
            JSONObject obj = new JSONObject();
            analyzeRootSchemaElement(definitionObj, obj);
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
