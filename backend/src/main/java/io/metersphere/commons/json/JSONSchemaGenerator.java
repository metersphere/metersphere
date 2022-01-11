package io.metersphere.commons.json;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.*;
import io.metersphere.commons.utils.LogUtil;
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
        try {
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(json, JsonElement.class);
            JsonObject rootElement = element.getAsJsonObject();
            analyzeRootSchemaElement(rootElement, rootObj);
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    private static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj) {
        if (rootElement.has("type") || rootElement.has("allOf")) {
            analyzeObject(rootElement, rootObj);
        }
        if (rootElement.has("definitions")) {
            analyzeDefinitions(rootElement);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj) {
        if (object.has("allOf")) {
            JsonArray allOfArray = object.get("allOf").getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has("properties")) {
                    // Properties elements will become the attributes/references of the element
                    JsonObject propertiesObj = allOfElementObj.get("properties").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(rootObj, propertyKey, propertyObj);
                    }
                }
            }
        } else if (object.has("properties")) {
            JsonObject propertiesObj = object.get("properties").getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(rootObj, propertyKey, propertyObj);
            }
        } else if (object.has("type") && object.get("type").getAsString().equals("array")) {
            analyzeProperty(rootObj, "MS-OBJECT", object);
        } else if (object.has("type") && !object.get("type").getAsString().equals("object")) {
            analyzeProperty(rootObj, object.getAsString(), object);
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object) {
        if (object.has("type")) {
            String propertyObjType = null;
            if (object.get("type") instanceof JsonPrimitive) {
                propertyObjType = object.get("type").getAsString();
            } else if (object.get("type") instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get("type").getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
                if (typeArray.size() > 1) {
                    if (typeArray.get(1).getAsString().equals("null")) {
                        // 暂不处理，后续使用时再加
                    }
                }
            }
            if (object.has("default")) {
                concept.put(propertyName, object.get("default"));
            } else if (object.has("enum")) {
                try {
                    if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                        Object value = object.get("mock").getAsJsonObject().get("mock");
                        concept.put(propertyName, value);
                    } else {
                        List<Object> list = analyzeEnumProperty(object);
                        if (list.size() > 0) {
                            int index = (int) (Math.random() * list.size());
                            concept.put(propertyName, list.get(index));
                        }
                    }
                } catch (Exception e) {
                    concept.put(propertyName, "");
                }
            } else if (propertyObjType.equals("string")) {
                // 先设置空值
                concept.put(propertyName, "");
                if (object.has("format")) {
                    String propertyFormat = object.get("format").getAsString();
                    if (propertyFormat.equals("date-time")) {

                    }
                }
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString()) && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                    concept.put(propertyName, value);
                }
            } else if (propertyObjType.equals("integer")) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        int value = object.get("mock").getAsJsonObject().get("mock").getAsInt();
                        concept.put(propertyName, value);
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                        concept.put(propertyName, value);
                    }
                }

            } else if (propertyObjType.equals("number")) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        Number value = object.get("mock").getAsJsonObject().get("mock").getAsNumber();
                        if (value.toString().indexOf(".") == -1) {
                            concept.put(propertyName, value.longValue());
                        } else {
                            concept.put(propertyName, value.floatValue());
                        }
                    } catch (Exception e) {
                        String value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                        concept.put(propertyName, value);
                    }
                }
            } else if (propertyObjType.equals("boolean")) {
                // 先设置空值
                concept.put(propertyName, false);
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    String value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").toString());
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
            } else if (propertyObjType.equals("array")) {
                // 先设置空值
                List<Object> array = new LinkedList<>();

                JsonArray jsonArray = new JsonArray();
                if (object.has("items")) {
                    if(object.get("items").isJsonArray()){
                        jsonArray = object.get("items").getAsJsonArray();
                    }else {
                        JsonObject itemsObject = object.get("items").getAsJsonObject();
                        array.add(itemsObject);
                    }
                }

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject itemsObject = jsonArray.get(i).getAsJsonObject();
                    if (object.has("items")) {
                        if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                            try {
                                if(itemsObject.has("type") && itemsObject.get("type").getAsString().equals("integer")){
                                    int value = itemsObject.get("mock").getAsJsonObject().get("mock").getAsInt();
                                    array.add(value);
                                }else {
                                    String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                                    array.add(value);
                                }
                            } catch (Exception e) {
                                String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                                array.add(value);
                            }
                        } else if (itemsObject.has("enum")) {
                            array.add(analyzeEnumProperty(itemsObject));
                        } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("string")) {
                            if (itemsObject.has("default")) {
                                array.add(itemsObject.get("default"));
                            } else if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                                array.add(value);
                            } else {
                                array.add("");
                            }
                        } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("number")) {
                            if (itemsObject.has("default")) {
                                array.add(itemsObject.get("default"));
                            } else if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                                String value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                                array.add(value);
                            } else {
                                array.add(0);
                            }
                        } else if (itemsObject.has("properties")) {
                            JSONObject propertyConcept = new JSONObject(true);
                            JsonObject propertiesObj = itemsObject.get("properties").getAsJsonObject();
                            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                                String propertyKey = entry.getKey();
                                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                                analyzeProperty(propertyConcept, propertyKey, propertyObj);
                            }
                            array.add(propertyConcept);

                        } else if (itemsObject.has("type") && itemsObject.get("type") instanceof JsonPrimitive) {
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
            } else if (propertyObjType.equals("object")) {
                JSONObject obj = new JSONObject();
                concept.put(propertyName, obj);
                analyzeObject(object, obj);
            } else if (StringUtils.equalsIgnoreCase(propertyObjType,"null")) {
                concept.put(propertyName, null);
            }
        }
    }

    private static List<Object> analyzeEnumProperty(JsonObject object) {
        List<Object> list = new LinkedList<>();
        String jsonStr = null;
        try {
            JsonArray enumValues = object.get("enum").getAsJsonArray();
            for (JsonElement enumValueElem : enumValues) {
                String enumValue = enumValueElem.getAsString();
                list.add(enumValue);
            }
        } catch (Exception e) {
            jsonStr = object.get("enum").getAsString();
        }
        if (jsonStr != null && list.isEmpty()) {
            String[] arrays = jsonStr.split("\n");
            for (String str : arrays) {
                list.add(str);
            }
        }
        return list;
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
        try {
            JSONObject root = new JSONObject(true);
            generator(jsonSchema, root);
            // 格式化返回
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
            if (root.get("MS-OBJECT") != null) {
                return gson.toJson(root.get("MS-OBJECT"));
            }
            return gson.toJson(root);
        } catch (Exception e) {
            return jsonSchema;
        }
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
