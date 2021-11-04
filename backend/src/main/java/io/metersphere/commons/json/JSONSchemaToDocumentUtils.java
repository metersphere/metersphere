package io.metersphere.commons.json;

import com.google.gson.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class JSONSchemaToDocumentUtils {

    private static void analyzeRootSchemaElement(JsonObject rootElement, List<DocumentElement> roots) {
        if (rootElement.has("type") || rootElement.has("allOf")) {
            analyzeObject(rootElement, roots);
        }
    }

    private static void analyzeObject(JsonObject object, List<DocumentElement> roots) {
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
                        analyzeProperty(roots, propertyKey, propertyObj);
                    }
                }
            }
        } else if (object.has("properties")) {
            JsonObject propertiesObj = object.get("properties").getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(roots, propertyKey, propertyObj);
            }
        } else if (object.has("type") && object.get("type").getAsString().equals("array")) {
            analyzeProperty(roots, "MS-OBJECT", object);
        } else if (object.has("type") && !object.get("type").getAsString().equals("object")) {
            analyzeProperty(roots, object.getAsString(), object);
        }
    }

    private static void analyzeProperty(List<DocumentElement> concept,
                                        String propertyName, JsonObject object) {
        if (object.has("type")) {
            String propertyObjType = null;
            if (object.get("type") instanceof JsonPrimitive) {
                propertyObjType = object.get("type").getAsString();
            } else if (object.get("type") instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get("type").getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
            }
            if (object.has("default")) {
                concept.add(new DocumentElement(propertyName, propertyObjType, object.get("default") != null ? object.get("default").toString() : "", null));
            } else if (object.has("enum")) {
                try {
                    if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                        Object value = object.get("mock").getAsJsonObject().get("mock");
                        concept.add(new DocumentElement(propertyName, propertyObjType, value.toString(), null));
                    } else {
                        List<Object> list = analyzeEnumProperty(object);
                        if (list.size() > 0) {
                            int index = (int) (Math.random() * list.size());
                            concept.add(new DocumentElement(propertyName, propertyObjType, list.get(index).toString(), null));
                        }
                    }
                } catch (Exception e) {
                    concept.add(new DocumentElement(propertyName, propertyObjType, "", null));
                }
            } else if (propertyObjType.equals("string")) {
                // 先设置空值
                String value = "";
                if (object.has("default")) {
                    value = object.get("default") != null ? object.get("default").toString() : "";
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString()) && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, null));
            } else if (propertyObjType.equals("integer")) {
                Object value = null;
                if (object.has("default")) {
                    value = object.get("default");
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        value = object.get("mock").getAsJsonObject().get("mock").getAsInt();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, null));
            } else if (propertyObjType.equals("number")) {
                Object value = null;
                if (object.has("default")) {
                    value = object.get("default");
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        value = object.get("mock").getAsJsonObject().get("mock").getAsNumber();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, null));
            } else if (propertyObjType.equals("boolean")) {
                Object value = null;
                if (object.has("default")) {
                    value = object.get("default");
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").toString());
                    } catch (Exception e) {
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, null));
            } else if (propertyObjType.equals("array")) {
                analyzeArray(propertyObjType, propertyName, object);
            } else if (propertyObjType.equals("object")) {
                List<DocumentElement> list = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, "", list));
                analyzeObject(object, list);
            }
        }
    }

    private static void analyzeArray(String propertyObjType, String propertyName, JsonObject object) {
        // 先设置空值
        List<DocumentElement> array = new LinkedList<>();
        JsonArray jsonArray = new JsonArray();
        if (object.has("items") && object.get("items").isJsonArray()) {
            jsonArray = object.get("items").getAsJsonArray();
        } else {
            JsonObject itemsObject = object.get("items").getAsJsonObject();
            array.add(new DocumentElement(propertyName, propertyObjType, itemsObject, null));
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject itemsObject = jsonArray.get(i).getAsJsonObject();
            if (object.has("items")) {
                Object value = null;
                if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        value = itemsObject.get("mock").getAsJsonObject().get("mock").getAsInt();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                    }
                } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("string")) {
                    if (itemsObject.has("default")) {
                        value = itemsObject.get("default");
                    } else if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                        value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                    }
                } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("number")) {
                    if (itemsObject.has("default")) {
                        value = itemsObject.get("default");
                    } else if (itemsObject.has("mock") && itemsObject.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString())) {
                        value = ScriptEngineUtils.buildFunctionCallString(itemsObject.get("mock").getAsJsonObject().get("mock").getAsString());
                    }
                } else if (itemsObject.has("properties")) {
                    List<DocumentElement> propertyConcept = new LinkedList<>();
                    JsonObject propertiesObj = itemsObject.get("properties").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(propertyConcept, propertyKey, propertyObj);
                    }
                }
                array.add(new DocumentElement(propertyName, itemsObject.get("type").getAsString(), value, null));
            } else if (object.has("items") && object.get("items").isJsonArray()) {
                JsonArray itemsObjectArray = object.get("items").getAsJsonArray();
                array.add(new DocumentElement(propertyName, itemsObject.get("type").getAsString(), itemsObjectArray, null));
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

    public static List<DocumentElement> getDocument(String jsonSchema) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(jsonSchema, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        List<DocumentElement> roots = new LinkedList<>();
        analyzeRootSchemaElement(rootElement, roots);
        return roots;
    }

}
