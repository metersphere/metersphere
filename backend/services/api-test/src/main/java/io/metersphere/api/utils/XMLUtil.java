package io.metersphere.api.utils;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wx
 */
public class XMLUtil {

    private static final String ENCODING = "UTF-8";

    @Nullable
    public static String delXmlHeader(String xml) {
        int begin = xml.indexOf("?>");
        if (begin != -1) {
            if (begin + 2 >= xml.length()) {
                return null;
            }
            xml = xml.substring(begin + 2);
        }   //  <?xml version="1.0" encoding="utf-8"?> 若存在，则去除
        String rgex = ">";
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(xml);
        xml = m.replaceAll("> ");
        rgex = "\\s*</";
        pattern = Pattern.compile(rgex);
        m = pattern.matcher(xml);
        xml = m.replaceAll(" </");
        return xml;
    }

    //  传入完整的 xml 文本，转换成 json 对象
    public static JSONObject xmlConvertJson(String xml) {
        if (StringUtils.isBlank(xml)) return null;
        xml = delXmlHeader(xml);
        if (xml == null) return null;
        if (stringToDocument(xml) == null) {
            LogUtils.error("xml内容转换失败！");
            return null;
        }
        Element node = stringToDocument(xml).getRootElement();
        JSONObject result = getJsonObjectByDC(node);
        return result;
    }

    public static Document stringToDocument(String xml) {
        try {
            return XMLUtils.getDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8.name())));
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    private static JSONObject getJsonObjectByDC(Element node) {
        JSONObject result = new JSONObject();
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            List<JSONObject> list = new LinkedList<>();
            for (Element e : listElement) {// 遍历所有一级子节点
                JSONObject jsonObject = getJsonObjectByDC(e);
                //加xml标签上的属性 eg: <field length="2" scale="0" type="string">RB</field>
                //这里添加 length scale type
                if (!e.attributes().isEmpty()) {
                    JSONObject attributeJson = new JSONObject();
                    for (Attribute attribute : e.attributes()) {
                        attributeJson.put(attribute.getName(), attribute.getValue());
                    }
                    jsonObject.append("attribute", attributeJson);
                }
                list.add(jsonObject);
            }
            if (list.size() == 1) {
                result.put(node.getName(), list.get(0));
            } else {
                result.put(node.getName(), list);
            }
        } else {
            if (!StringUtils.isAllBlank(node.getName(), node.getText())) {
                result.put(node.getName(), node.getText());
            }
        }
        return result;
    }

    /**
     * JSON对象转xml字符串
     *
     * @param json JSON对象
     * @return xml字符串
     */
    public static String jsonToPrettyXml(JSONObject json) {
        Document document = jsonToDocument(json);

        /* 格式化xml */
        OutputFormat format = OutputFormat.createPrettyPrint();

        // 设置缩进为4个空格
        format.setIndent(StringUtils.SPACE);
        format.setIndentSize(4);

        StringWriter formatXml = new StringWriter();
        XMLWriter writer = new XMLWriter(formatXml, format);
        try {
            writer.write(document);
        } catch (IOException e) {
            LogUtils.error("This object can not convert to xml", e);
        }

        return formatXml.toString();
    }

    /**
     * JSON对象转Document对象
     *
     * @param json JSON对象
     * @return Document对象
     */
    public static Document jsonToDocument(JSONObject json) {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding(ENCODING);
        setDocument(json, document);
        return document;
    }

    private static void setDocument(JSONObject json, Document document) {
        for (String key : json.keySet()) {
            if (json.get(key) instanceof LinkedList<?>) {
                for (Object o : ((LinkedList<?>) json.get(key))) {
                    setDocument((JSONObject) o, document);
                }
            } else {
                Element root = jsonToElement(json.get(key), key);
                document.add(root);
            }
        }
    }

    /**
     * JSON对象转Element对象
     *
     * @param json     JSON对象
     * @param nodeName 节点名称
     * @return Element对象
     */
    public static Element jsonToElement(Object json, String nodeName) {
        Element node = DocumentHelper.createElement(nodeName);
        if (json instanceof JSONObject) {
            delObject(json, node);
        }
        if (json instanceof List<?>) {
            ((List<?>) json).forEach(t -> {
                jsonToElement(t, nodeName);
            });
        }
        if (json instanceof String) {
            Element element = DocumentHelper.createElement(json.toString());
            element.setText(json.toString());
            node.add(element);
        }
        return node;
    }

    private static void delObject(Object json, Element node) {
        for (String key : ((JSONObject) json).keySet()) {
            Object child = ((JSONObject) json).get(key);
            if (child instanceof JSONObject) {
                node.add(jsonToElement(((JSONObject) json).get(key), key));
            } else if (child instanceof LinkedList<?>) {
                ((LinkedList<?>) child).forEach(t -> {
                    node.add(jsonToElement(t, key));
                });
            } else {
                if (StringUtils.equals(key, "example")) {
                    node.setText(((JSONObject) json).get(key).toString());
                }
            }
        }
    }

}
