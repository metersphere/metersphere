package io.metersphere.commons.json;

import com.google.gson.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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
        List<String> requiredList = new ArrayList<>();
        if (object.get("required") != null) {
            JsonArray jsonElements = object.get("required").getAsJsonArray();
            for (JsonElement jsonElement : jsonElements) {
                requiredList.add(jsonElement.getAsString());
            }
        }
        if (object.has("allOf")) {
            JsonArray allOfArray = object.get("allOf").getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has("properties")) {
                    JsonObject propertiesObj = allOfElementObj.get("properties").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(roots, propertyKey, propertyObj, requiredList);
                    }
                }
            }
        } else if (object.has("properties")) {
            JsonObject propertiesObj = object.get("properties").getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                if (propertyObj.get("required") != null) {
                    JsonArray jsonElements = propertyObj.get("required").getAsJsonArray();
                    for (JsonElement jsonElement : jsonElements) {
                        requiredList.add(jsonElement.getAsString());
                    }
                }
                analyzeProperty(roots, propertyKey, propertyObj, requiredList);
            }
        } else if (object.has("type") && object.get("type").getAsString().equals("array")) {
            analyzeProperty(roots, "MS-OBJECT", object, requiredList);
        } else if (object.has("type") && !object.get("type").getAsString().equals("object")) {
            analyzeProperty(roots, object.getAsString(), object, requiredList);
        }
    }

    private static void analyzeProperty(List<DocumentElement> concept,
                                        String propertyName, JsonObject object, List<String> requiredList) {
        if (object.has("type")) {
            String propertyObjType = null;
            if (object.get("type") instanceof JsonPrimitive) {
                propertyObjType = object.get("type").getAsString();
            } else if (object.get("type") instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get("type").getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
            }
            Object value = null;
            boolean required = requiredList.contains(propertyName);
            if (object.has("default")) {
                value = object.get("default") != null ? object.get("default").getAsString() : "";
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (object.has("enum")) {
                try {
                    if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                        value = object.get("mock").getAsJsonObject().get("mock");
                    } else {
                        List<Object> list = analyzeEnumProperty(object);
                        if (list.size() > 0) {
                            int index = (int) (Math.random() * list.size());
                            value = list.get(index).toString();
                        }
                    }
                    if (value != null && value instanceof JsonPrimitive) {
                        value = ((JsonPrimitive) value).getAsString();
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals("string")) {
                // 先设置空值
                if (object.has("default")) {
                    value = object.get("default") != null ? object.get("default").getAsString() : "";
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString()) && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").getAsString());
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals("integer")) {
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
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals("number")) {
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
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals("boolean")) {
                if (object.has("default")) {
                    value = object.get("default");
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    try {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get("mock").getAsJsonObject().get("mock").toString());
                    } catch (Exception e) {
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals("array")) {
                List<DocumentElement> elements = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, "", requiredList.contains(propertyName), true, elements));
                JsonArray jsonArray = object.get("items").getAsJsonArray();
                analyzeArray(propertyName, jsonArray, elements, requiredList);
            } else if (propertyObjType.equals("object")) {
                List<DocumentElement> list = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, "", list));
                analyzeObject(object, list);
            }
        }
    }

    private static void analyzeArray(String propertyName, JsonArray jsonArray, List<DocumentElement> array, List<String> requiredList) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement obj = jsonArray.get(i);
            if (obj.isJsonArray()) {
                JsonArray itemsObject = obj.getAsJsonArray();
                List<DocumentElement> elements = new LinkedList<>();
                array.add(new DocumentElement(propertyName, "", "", requiredList.contains("" + i + ""), elements));
                analyzeArray("", itemsObject, elements, requiredList);
            } else if (obj.isJsonObject()) {
                List<String> requiredItems = new ArrayList<>();
                if (obj.getAsJsonObject().get("required") != null) {
                    JsonArray jsonElements = obj.getAsJsonObject().get("required").getAsJsonArray();
                    for (JsonElement jsonElement : jsonElements) {
                        requiredItems.add(jsonElement.getAsString());
                    }
                }
                analyzeProperty(array, String.valueOf(i), obj.getAsJsonObject(), CollectionUtils.isNotEmpty(requiredItems) ? requiredItems : requiredList);
            } else {
                JsonPrimitive primitive = (JsonPrimitive) obj;
                array.add(new DocumentElement(propertyName, primitive.getAsString(), "", requiredList.contains(propertyName), null));
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
        if (rootElement.get("type") != null) {
            if (rootElement.get("type").toString().equals("object") || rootElement.get("type").toString().equals("\"object\"")) {
                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot("root", roots));
                }};
            } else {
                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot("array", roots));
                }};
            }
        }
        return roots;
    }
}
