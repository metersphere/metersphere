package io.metersphere.commons.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.utils.DocumentUtils;
import net.sf.json.util.JSONTokener;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.XML;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class JSONToDocumentUtils {

    public static void jsonDataFormatting(JSONArray array, List<DocumentElement> children) {
        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                children.add(new DocumentElement("", BasicConstant.OBJECT, "", childrenElements));
                jsonDataFormatting((JSONObject) value, childrenElements);
            } else if (value instanceof JSONArray) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                DocumentElement documentElement = new DocumentElement("", BasicConstant.ARRAY, "", childrenElements);
                documentElement.setArrayVerification(true);
                children.add(documentElement);
                jsonDataFormatting((JSONArray) value, childrenElements);
            } else {
                String type = BasicConstant.STRING;
                if (value != null) {
                    type = DocumentUtils.getType(value);
                }
                children.add(new DocumentElement("", type, value, null));
            }
        }
    }

    public static void jsonDataFormatting(JSONObject object, List<DocumentElement> children) {
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (value instanceof JSONObject) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                children.add(new DocumentElement(key, BasicConstant.OBJECT, "", childrenElements));
                jsonDataFormatting((JSONObject) value, childrenElements);
            } else if (value instanceof JSONArray) {
                List<DocumentElement> childrenElements = new LinkedList<>();
                DocumentElement documentElement = new DocumentElement(key, BasicConstant.ARRAY, "", childrenElements);
                documentElement.setArrayVerification(true);
                children.add(documentElement);
                jsonDataFormatting((JSONArray) value, childrenElements);
            } else {
                String type = BasicConstant.STRING;
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
                roots.add(new DocumentElement().newRoot(BasicConstant.ARRAY, children));
                JSONArray array = JSON.parseArray(json);
                jsonDataFormatting(array, children);
            } else {
                JSONArray array = JSON.parseArray(json);
                jsonDataFormatting(array, roots);
            }
        } else {
            if (StringUtils.equals(type, "JSON")) {
                roots.add(new DocumentElement().newRoot(BasicConstant.OBJECT, children));
                JSONObject object = JSON.parseObject(json);
                jsonDataFormatting(object, children);
            } else {
                JSONObject object = JSON.parseObject(json);
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
                    this.add(new DocumentElement().newRoot(BasicConstant.OBJECT, null));
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
            String type = BasicConstant.STRING;
            if (StringUtils.isNotEmpty(node.getTextTrim())) {
                type = DocumentUtils.getType(node.getTextTrim());
            }
            children.add(new DocumentElement(node.getName(), type, node.getTextTrim(), null));
        }
        for (Element element : listElement) {//遍历所有一级子节点
            List<Element> elementNodes = element.elements();
            if (elementNodes.size() > 0) {
                List<DocumentElement> elements = new LinkedList<>();
                children.add(new DocumentElement(element.getName(), BasicConstant.OBJECT, element.getTextTrim(), elements));
                getNodes(element, elements);//递归
            } else {
                getNodes(element, children);//递归
            }
        }
    }

    public static List<DocumentElement> getXmlDocument(String xml) {
        List<DocumentElement> roots = new LinkedList<>();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            // 创建saxReader对象
            SAXReader reader = new SAXReader();
            // 通过read方法读取一个文件 转换成Document对象
            Document document = reader.read(is);
            //获取根节点元素对象
            getNodes(document.getRootElement(), roots);
            // 未能处理root补偿root 节点
            if (roots.size() > 1) {
                Element node = document.getRootElement();
                List<DocumentElement> newRoots = new LinkedList<>();
                newRoots.add(new DocumentElement("root", node.getName(), BasicConstant.OBJECT, node.getTextTrim(), roots));
                return newRoots;
            } else if (roots.size() == 1) {
                roots.get(0).setId("root");
            }
            return roots;
        } catch (Exception ex) {
            LogUtil.error(ex);
            return roots;
        }
    }
}
