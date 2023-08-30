package io.metersphere.commons.utils;

import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.utils.DocumentUtils;
import net.sf.json.util.JSONTokener;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.LinkedList;
import java.util.List;

public class JSONToDocumentUtil {

    public static void jsonDataFormatting(JSONArray array, List<DocumentElement> children) {
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                children.add(new DocumentElement(StringUtils.EMPTY, PropertyConstant.OBJECT, StringUtils.EMPTY, childrenElements));
                jsonDataFormatting((JSONObject) value, childrenElements);
            } else if (value instanceof JSONArray) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                DocumentElement documentElement = new DocumentElement(StringUtils.EMPTY, PropertyConstant.ARRAY, StringUtils.EMPTY, childrenElements);
                documentElement.setArrayVerification(true);
                documentElement.setName(String.valueOf(i));
                children.add(documentElement);
                jsonDataFormatting((JSONArray) value, childrenElements);
            } else {
                String type = PropertyConstant.STRING;
                if (value != null) {
                    type = DocumentUtils.getType(value);
                }
                children.add(new DocumentElement(String.valueOf(i), type, value, null));
            }
        }
    }

    public static void jsonDataFormatting(JSONObject object, List<DocumentElement> children) {
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (value instanceof JSONObject) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                children.add(new DocumentElement(key, PropertyConstant.OBJECT, StringUtils.EMPTY, childrenElements));
                jsonDataFormatting((JSONObject) value, childrenElements);
            } else if (value instanceof JSONArray) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                DocumentElement documentElement = new DocumentElement(key, PropertyConstant.ARRAY, StringUtils.EMPTY, childrenElements);
                documentElement.setArrayVerification(true);
                children.add(documentElement);
                jsonDataFormatting((JSONArray) value, childrenElements);
            } else {
                String type = PropertyConstant.STRING;
                if (value != null) {
                    type = DocumentUtils.getType(value);
                }
                children.add(new DocumentElement(key, type, value, null));
            }
        }
    }

    private static List<DocumentElement> getJsonDocument(String json, String type) {
        List<DocumentElement> roots = new LinkedList<>();
        List<DocumentElement> children = new LinkedList<>();
        Object typeObject = new JSONTokener(json).nextValue();
        if (typeObject instanceof net.sf.json.JSONArray) {
            if (StringUtils.equals(type, "JSON")) {
                roots.add(new DocumentElement().newRoot(PropertyConstant.ARRAY, children));
                JSONArray array = JSONUtil.parseArray(json);
                jsonDataFormatting(array, children);
            } else {
                roots.add(new DocumentElement().newRoot(PropertyConstant.ARRAY, children));
                JSONArray array = JSONUtil.parseArray(json);
                jsonDataFormatting(array, roots);
            }
        } else {
            if (StringUtils.equals(type, "JSON")) {
                roots.add(new DocumentElement().newRoot(PropertyConstant.OBJECT, children));
                JSONObject object = JSONUtil.parseObject(json);
                jsonDataFormatting(object, children);
            } else {
                roots.add(new DocumentElement().newRoot(PropertyConstant.OBJECT, children));
                JSONObject object = JSONUtil.parseObject(json);
                jsonDataFormatting(object, roots);
            }
        }
        return roots;
    }

    public static List<DocumentElement> getDocument(String json, String type) {
        try {
            if (StringUtils.equals(type, "JSON")) {
                return getJsonDocument(json, type);
            } else if (StringUtils.equals(type, "XML")) {
                org.json.JSONObject xmlJSONObj = XML.toJSONObject(json);
                String jsonPrettyPrintString = xmlJSONObj.toString(4);
                return getJsonDocument(jsonPrettyPrintString, type);
            } else {
                return new LinkedList<DocumentElement>() {{
                    this.add(new DocumentElement().newRoot(PropertyConstant.OBJECT, null));
                }};
            }
        } catch (Exception e) {
            LogUtil.error(e);
            return new LinkedList<>();
        }
    }

    /**
     * 从指定节点开始,递归遍历所有子节点
     */
    public static void getNodes(Element node, List<DocumentElement> children) {
        //递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();
        if (listElement.isEmpty()) {
            String type = PropertyConstant.STRING;
            if (StringUtils.isNotEmpty(node.getTextTrim())) {
                type = DocumentUtils.getType(node.getTextTrim());
            }
            children.add(new DocumentElement(node.getName(), type, node.getTextTrim(), null));
        }
        for (Element element : listElement) {//遍历所有一级子节点
            List<Element> elementNodes = element.elements();
            if (elementNodes.size() > 0) {
                List<DocumentElement> elements = new LinkedList<>();
                children.add(new DocumentElement(element.getName(), PropertyConstant.OBJECT, element.getTextTrim(), elements));
                getNodes(element, elements);//递归
            } else {
                getNodes(element, children);//递归
            }
        }
    }
}
