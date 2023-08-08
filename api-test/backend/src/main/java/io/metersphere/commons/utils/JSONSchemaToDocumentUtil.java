package io.metersphere.commons.utils;

import com.google.gson.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class JSONSchemaToDocumentUtil {

    private static void analyzeRootSchemaElement(JsonObject rootElement, List<DocumentElement> roots) {
        if (rootElement.has(PropertyConstant.TYPE) || rootElement.has(PropertyConstant.ALL_OF)) {
            analyzeObject(rootElement, roots);
        }
    }

    private static void analyzeObject(JsonObject object, List<DocumentElement> roots) {
        List<String> requiredList = new ArrayList<>();
        if (object.get(PropertyConstant.REQUIRED) != null) {
            JsonArray jsonElements = object.get(PropertyConstant.REQUIRED).getAsJsonArray();
            for (JsonElement jsonElement : jsonElements) {
                requiredList.add(jsonElement.getAsString());
            }
        }
        if (object.has(PropertyConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(PropertyConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has(PropertyConstant.PROPERTIES)) {
                    JsonObject propertiesObj = allOfElementObj.get(PropertyConstant.PROPERTIES).getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(roots, propertyKey, propertyObj, requiredList);
                    }
                }
            }
        } else if (object.has(PropertyConstant.PROPERTIES)) {
            JsonObject propertiesObj = object.get(PropertyConstant.PROPERTIES).getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                if (propertyObj.get(PropertyConstant.REQUIRED) != null) {
                    JsonArray jsonElements = propertyObj.get(PropertyConstant.REQUIRED).getAsJsonArray();
                    for (JsonElement jsonElement : jsonElements) {
                        requiredList.add(jsonElement.getAsString());
                    }
                }
                analyzeProperty(roots, propertyKey, propertyObj, requiredList);
            }
        } else if (object.has(PropertyConstant.TYPE)
                && object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.ARRAY)) {
            analyzeProperty(roots, PropertyConstant.MS_OBJECT, object, requiredList);
        } else if (object.has(PropertyConstant.TYPE)
                && !object.get(PropertyConstant.TYPE).getAsString().equals(PropertyConstant.OBJECT)) {
            analyzeProperty(roots, object.getAsString(), object, requiredList);
        }
    }

    private static void analyzeProperty(List<DocumentElement> concept,
                                        String propertyName, JsonObject object, List<String> requiredList) {
        if (object.has(PropertyConstant.TYPE)) {
            String propertyObjType = null;
            if (object.get(PropertyConstant.TYPE) instanceof JsonPrimitive) {
                propertyObjType = object.get(PropertyConstant.TYPE).getAsString();
            } else if (object.get(PropertyConstant.TYPE) instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get(PropertyConstant.TYPE).getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
            }
            Object value = null;
            boolean required = requiredList.contains(propertyName);
            if (object.has(PropertyConstant.DEFAULT)) {
                value = object.get(PropertyConstant.DEFAULT) != null ? object.get(PropertyConstant.DEFAULT).getAsString() : StringUtils.EMPTY;
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (object.has(PropertyConstant.ENUM)) {
                try {
                    if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                            && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                        value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK);
                    } else {
                        List<Object> list = EnumPropertyUtil.analyzeEnumProperty(object);
                        if (CollectionUtils.isNotEmpty(list)) {
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
            } else if (propertyObjType.equals(PropertyConstant.STRING)) {
                // 先设置空值
                if (object.has(PropertyConstant.DEFAULT)) {
                    value = object.get(PropertyConstant.DEFAULT) != null ? object.get(PropertyConstant.DEFAULT).getAsString() : StringUtils.EMPTY;
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(PropertyConstant.INTEGER)) {
                if (object.has(PropertyConstant.DEFAULT)) {
                    value = object.get(PropertyConstant.DEFAULT);
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    try {
                        value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsInt();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(PropertyConstant.NUMBER)) {
                if (object.has(PropertyConstant.DEFAULT)) {
                    value = object.get(PropertyConstant.DEFAULT);
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    try {
                        value = object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsBigDecimal();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(PropertyConstant.BOOLEAN)) {
                if (object.has(PropertyConstant.DEFAULT)) {
                    value = object.get(PropertyConstant.DEFAULT);
                }
                if (object.has(PropertyConstant.MOCK) && object.get(PropertyConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).getAsString())) {
                    try {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(PropertyConstant.MOCK).getAsJsonObject().get(PropertyConstant.MOCK).toString());
                    } catch (Exception e) {
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(PropertyConstant.ARRAY)) {
                List<DocumentElement> elements = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, StringUtils.EMPTY, requiredList.contains(propertyName), true, elements));
                JsonArray jsonArray = object.get(PropertyConstant.ITEMS).getAsJsonArray();
                analyzeArray(propertyName, jsonArray, elements, requiredList);
            } else if (propertyObjType.equals(PropertyConstant.OBJECT)) {
                List<DocumentElement> list = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, StringUtils.EMPTY, list));
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
                array.add(new DocumentElement(propertyName, StringUtils.EMPTY, StringUtils.EMPTY, requiredList.contains(StringUtils.EMPTY + i + StringUtils.EMPTY), elements));
                analyzeArray(StringUtils.EMPTY, itemsObject, elements, requiredList);
            } else if (obj.isJsonObject()) {
                List<String> requiredItems = new ArrayList<>();
                if (obj.getAsJsonObject().get(PropertyConstant.REQUIRED) != null) {
                    JsonArray jsonElements = obj.getAsJsonObject().get(PropertyConstant.REQUIRED).getAsJsonArray();
                    for (JsonElement jsonElement : jsonElements) {
                        requiredItems.add(jsonElement.getAsString());
                    }
                }
                analyzeProperty(array, String.valueOf(i), obj.getAsJsonObject(), CollectionUtils.isNotEmpty(requiredItems) ? requiredItems : requiredList);
            } else {
                JsonPrimitive primitive = (JsonPrimitive) obj;
                array.add(new DocumentElement(propertyName, primitive.getAsString(), StringUtils.EMPTY, requiredList.contains(propertyName), null));
            }
        }
    }

    public static List<DocumentElement> getDocument(String jsonSchema) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(jsonSchema, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        List<DocumentElement> roots = new LinkedList<>();
        analyzeRootSchemaElement(rootElement, roots);
        if (rootElement.get(PropertyConstant.TYPE) != null) {
            if (rootElement.get(PropertyConstant.TYPE).toString().equals(PropertyConstant.OBJECT)
                    || rootElement.get(PropertyConstant.TYPE).toString().equals("\"object\"")) {

                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot(PropertyConstant.ROOT, roots));
                }};

            } else {
                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot(PropertyConstant.ARRAY, roots));
                }};
            }
        }
        return roots;
    }
}
