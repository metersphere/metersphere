package io.metersphere.commons.utils;

import io.metersphere.api.exec.engine.EngineSourceParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLUtil {
    public static String formatXmlString(String xmlString) throws Exception {
        xmlString = StringUtils.replace(xmlString, StringUtils.LF, StringUtils.EMPTY);
        Document document = getDom4jDocumentByXmlString(xmlString);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(document.getXMLEncoding());
        StringWriter stringWriter = new StringWriter();
        XMLWriter writer = new XMLWriter(stringWriter, format);
        writer.write(document);
        writer.close();
        return stringWriter.toString();
    }

    public static Document getDom4jDocumentByXmlString(String xmlString) throws Exception {
        return EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8.name())));
    }

    public static void setExpandEntityReferencesFalse(DocumentBuilderFactory documentBuilderFactory) {
        try {
            String FEATURE = null;
            FEATURE = "http://javax.xml.XMLConstants/feature/secure-processing";
            documentBuilderFactory.setFeature(FEATURE, true);
            FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            documentBuilderFactory.setFeature(FEATURE, true);
            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            documentBuilderFactory.setFeature(FEATURE, false);
            FEATURE = "http://xml.org/sax/features/external-general-entities";
            documentBuilderFactory.setFeature(FEATURE, false);
            FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            documentBuilderFactory.setFeature(FEATURE, false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private static void jsonToXmlStr(JSONObject jObj, StringBuffer buffer, StringBuffer tab) {
        Map<String, Object> se = jObj.toMap();
        StringBuffer nowTab = new StringBuffer(tab.toString());
        for (Map.Entry<String, Object> en : se.entrySet()) {
            if (en == null || en.getValue() == null) continue;
            if (en.getValue() instanceof JSONObject) {
                buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                JSONObject jo = jObj.optJSONObject(en.getKey());
                jsonToXmlStr(jo, buffer, nowTab.append("\t"));
                buffer.append(tab).append("</").append(en.getKey()).append(">\n");
            } else if (en.getValue() instanceof JSONArray) {
                JSONArray array = jObj.optJSONArray(en.getKey());
                for (int i = 0; i < array.length(); i++) {
                    buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                    if (StringUtils.isNotBlank(array.optString(i))) {
                        JSONObject jsonobject = array.optJSONObject(i);
                        jsonToXmlStr(jsonobject, buffer, nowTab.append("\t"));
                        buffer.append(tab).append("</").append(en.getKey()).append(">\n");
                    }
                }
            } else if (en.getValue() instanceof String) {
                buffer.append(tab).append("<").append(en.getKey()).append(">").append(en.getValue());
                buffer.append("</").append(en.getKey()).append(">\n");
            }
        }
    }

    public static String jsonToXmlStr(JSONObject jObj) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        try {
            jsonToXmlStr(jObj, buffer, new StringBuffer(StringUtils.EMPTY));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return buffer.toString();
    }

    //  传入完整的 xml 文本，转换成 json 对象
    public static JSONObject XmlToJson(String xml) {
        JSONObject result = new JSONObject();
        if (xml == null) return null;
        List<String> list = preProcessXml(xml);
        try {
            result = (JSONObject) XmlTagToJsonObject(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return result;
    }

    //  预处理 xml 文本，转换成 tag + data 的列表
    private static List<String> preProcessXml(String xml) {
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
        return Arrays.asList(xml.split(StringUtils.SPACE));
    }

    //  传入预处理的列表，返回转换成功的 json 对象
    private static Object XmlTagToJsonObject(List<String> list) {
        if (list == null || list.size() == 0) return null;
        Stack<String> tagStack = new Stack<>(); //  tag 栈
        Stack<Object> valueStack = new Stack<>();   //  数据栈
        valueStack.push(new JSONObject());  //  最终结果将存放在第一个入栈的元素中
        for (String item : list) {
            String beginTag = isBeginTag(item), endTag = isEndTag(item);    //  判断当前 tag 是开始还是结尾
            if (beginTag != null) {
                tagStack.push(beginTag);
                valueStack.push(new JSONObject());
            } else if (endTag != null) {
                if (endTag.equals(tagStack.peek())) { //  是一对 tag
                    Object topValue = valueStack.peek();
                    if (topValue instanceof String) {    //  栈顶是纯数据 xml 节点
                        valueStack.pop();
                    }
                    valueStack.pop();
                    if (valueStack.peek() instanceof JSONObject) {
                        ((JSONObject) valueStack.peek()).put(tagStack.peek(), topValue);
                    }
                    tagStack.pop();
                }
            } else {
                valueStack.push(item);
            }
        }
        if (valueStack.empty()) return null;
        return valueStack.peek();
    }

    private static String isEndTag(String tagLine) {
        String rgex = "</(\\w*)>";
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式    
        Matcher m = pattern.matcher(tagLine);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String isBeginTag(String tagLine) {
        String rgex = "<(\\w*)>";
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式    
        Matcher m = pattern.matcher(tagLine);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Document stringToDocument(String xml) {
        try {
            return EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8.name())));
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public static JSONObject xmlStringToJSONObject(String xml) {
        try {
            return elementToJSONObject(stringToDocument(xml).getRootElement());
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();

        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            List<JSONObject> list = new LinkedList<>();
            for (Element e : listElement) {// 遍历所有一级子节点
                JSONObject jsonObject = elementToJSONObject(e);
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

}
