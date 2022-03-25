package io.metersphere.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.performance.parse.EngineSourceParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.*;

public class XMLUtils {

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
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        StringBuffer nowTab = new StringBuffer(tab.toString());
        for (Map.Entry<String, Object> en : se) {
            if (en == null || en.getValue() == null) continue;
            if (en.getValue() instanceof JSONObject) {
                buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlStr(jo, buffer, nowTab.append("\t"));
                buffer.append(tab).append("</").append(en.getKey()).append(">\n");
            } else if (en.getValue() instanceof JSONArray) {
                JSONArray jarray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jarray.size(); i++) {
                    buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                    if (StringUtils.isNotBlank(jarray.getString(i))) {
                        JSONObject jsonobject = jarray.getJSONObject(i);
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
            jsonToXmlStr(jObj, buffer, new StringBuffer(""));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return buffer.toString();
    }

    //  传入完整的 xml 文本，转换成 json 对象
    public static JSONObject XmlToJson(String xml) {
        JSONObject result = new JSONObject();
        if (xml == null)
            return null;
        List<String> list = preProcessXml(xml);
        try {
            result = (JSONObject) XmlTagToJsonObject(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
//            MSException.throwException(Translator.get("illegal_xml_format"));
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
        return Arrays.asList(xml.split(" "));
    }

    //  传入预处理的列表，返回转换成功的 json 对象
    private static Object XmlTagToJsonObject(List<String> list) {
        if (list == null || list.size() == 0)
            return null;
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
        if (valueStack.empty())
            return null;
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
            return EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xml.getBytes("utf-8")));
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public static JSONObject stringToJSONObject(String xml) {
        try {
            return elementToJSONObject(stringToDocument(xml).getRootElement());
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                else {
                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
                        result.put(e.getName(), new JSONArray());// 没有则创建
                    ((JSONArray) result.get(e.getName())).add(elementToJSONObject(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }
}
