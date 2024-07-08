package io.metersphere.api.utils;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wx
 */
public class XMLUtil {

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
}
