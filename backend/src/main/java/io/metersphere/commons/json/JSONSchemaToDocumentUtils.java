package io.metersphere.commons.json;

import com.google.gson.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.commons.utils.EnumPropertyUtil;
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
        if (rootElement.has(BasicConstant.TYPE) || rootElement.has(BasicConstant.ALL_OF)) {
            analyzeObject(rootElement, roots);
        }
    }

    private static void analyzeObject(JsonObject object, List<DocumentElement> roots) {
        List<String> requiredList = new ArrayList<>();
        if (object.get(BasicConstant.REQUIRED) != null) {
            JsonArray jsonElements = object.get(BasicConstant.REQUIRED).getAsJsonArray();
            for (JsonElement jsonElement : jsonElements) {
                requiredList.add(jsonElement.getAsString());
            }
        }
        if (object.has(BasicConstant.ALL_OF)) {
            JsonArray allOfArray = object.get(BasicConstant.ALL_OF).getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has(BasicConstant.PROPERTIES)) {
                    JsonObject propertiesObj = allOfElementObj.get(BasicConstant.PROPERTIES).getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(roots, propertyKey, propertyObj, requiredList);
                    }
                }
            }
        } else if (object.has(BasicConstant.PROPERTIES)) {
            JsonObject propertiesObj = object.get(BasicConstant.PROPERTIES).getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                if (propertyObj.get(BasicConstant.REQUIRED) != null) {
                    JsonArray jsonElements = propertyObj.get(BasicConstant.REQUIRED).getAsJsonArray();
                    for (JsonElement jsonElement : jsonElements) {
                        requiredList.add(jsonElement.getAsString());
                    }
                }
                analyzeProperty(roots, propertyKey, propertyObj, requiredList);
            }
        } else if (object.has(BasicConstant.TYPE)
                && object.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.ARRAY)) {
            analyzeProperty(roots, BasicConstant.MS_OBJECT, object, requiredList);
        } else if (object.has(BasicConstant.TYPE)
                && !object.get(BasicConstant.TYPE).getAsString().equals(BasicConstant.OBJECT)) {
            analyzeProperty(roots, object.getAsString(), object, requiredList);
        }
    }

    private static void analyzeProperty(List<DocumentElement> concept,
                                        String propertyName, JsonObject object, List<String> requiredList) {
        if (object.has(BasicConstant.TYPE)) {
            String propertyObjType = null;
            if (object.get(BasicConstant.TYPE) instanceof JsonPrimitive) {
                propertyObjType = object.get(BasicConstant.TYPE).getAsString();
            } else if (object.get(BasicConstant.TYPE) instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get(BasicConstant.TYPE).getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
            }
            Object value = null;
            boolean required = requiredList.contains(propertyName);
            if (object.has(BasicConstant.DEFAULT)) {
                value = object.get(BasicConstant.DEFAULT) != null ? object.get(BasicConstant.DEFAULT).getAsString() : "";
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (object.has(BasicConstant.ENUM)) {
                try {
                    if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                            && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                        value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK);
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
            } else if (propertyObjType.equals(BasicConstant.STRING)) {
                // 先设置空值
                if (object.has(BasicConstant.DEFAULT)) {
                    value = object.get(BasicConstant.DEFAULT) != null ? object.get(BasicConstant.DEFAULT).getAsString() : "";
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(BasicConstant.INTEGER)) {
                if (object.has(BasicConstant.DEFAULT)) {
                    value = object.get(BasicConstant.DEFAULT);
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    try {
                        value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsInt();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(BasicConstant.NUMBER)) {
                if (object.has(BasicConstant.DEFAULT)) {
                    value = object.get(BasicConstant.DEFAULT);
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    try {
                        value = object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsNumber();
                    } catch (Exception e) {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString());
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(BasicConstant.BOOLEAN)) {
                if (object.has(BasicConstant.DEFAULT)) {
                    value = object.get(BasicConstant.DEFAULT);
                }
                if (object.has(BasicConstant.MOCK) && object.get(BasicConstant.MOCK).getAsJsonObject() != null
                        && StringUtils.isNotEmpty(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).getAsString())) {
                    try {
                        value = ScriptEngineUtils.buildFunctionCallString(object.get(BasicConstant.MOCK).getAsJsonObject().get(BasicConstant.MOCK).toString());
                    } catch (Exception e) {
                    }
                }
                concept.add(new DocumentElement(propertyName, propertyObjType, value, required, null));
            } else if (propertyObjType.equals(BasicConstant.ARRAY)) {
                List<DocumentElement> elements = new LinkedList<>();
                concept.add(new DocumentElement(propertyName, propertyObjType, "", requiredList.contains(propertyName), true, elements));
                JsonArray jsonArray = object.get("items").getAsJsonArray();
                analyzeArray(propertyName, jsonArray, elements, requiredList);
            } else if (propertyObjType.equals(BasicConstant.OBJECT)) {
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
                if (obj.getAsJsonObject().get(BasicConstant.REQUIRED) != null) {
                    JsonArray jsonElements = obj.getAsJsonObject().get(BasicConstant.REQUIRED).getAsJsonArray();
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

    public static List<DocumentElement> getDocument(String jsonSchema) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(jsonSchema, JsonElement.class);
        JsonObject rootElement = element.getAsJsonObject();
        List<DocumentElement> roots = new LinkedList<>();
        analyzeRootSchemaElement(rootElement, roots);
        if (rootElement.get(BasicConstant.TYPE) != null) {
            if (rootElement.get(BasicConstant.TYPE).toString().equals(BasicConstant.OBJECT)
                    || rootElement.get(BasicConstant.TYPE).toString().equals("\"object\"")) {

                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot("root", roots));
                }};

            } else {
                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot(BasicConstant.ARRAY, roots));
                }};
            }
        }
        return roots;
    }
}
