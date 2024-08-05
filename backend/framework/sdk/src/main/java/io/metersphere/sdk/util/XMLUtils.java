package io.metersphere.sdk.util;


import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLUtils {
    public static final boolean IS_TRANS = false;

    public static Document getDocument(InputStream source) throws DocumentException {
        SAXReader reader = new SAXReader();
        try {
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (Exception e) {
            LogUtils.error(e);
        }
        if (!IS_TRANS) {
            reader.setXMLFilter(XMLUtils.getFilter());
        }
        return reader.read(source);
    }

    public static byte[] getBytes(Document document) throws Exception {
        OutputFormat format = new OutputFormat();
        format.setIndentSize(2);
        format.setNewlines(true);
        format.setPadText(true);
        format.setTrimText(false);
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            // 删除空白的行
            List<Node> nodes = document.selectNodes("//text()[normalize-space(.)='']");
            nodes.forEach(node -> {
                if (node.getText().contains(StringUtils.LF)) {
                    node.setText(StringUtils.EMPTY);
                }
            });

            XMLWriter xw = new XMLWriter(out, format);
            xw.setEscapeText(IS_TRANS);
            xw.write(document);
            xw.flush();
            xw.close();
            return out.toByteArray();
        } catch (IOException e) {
            LogUtils.error(e);
        }
        return new byte[0];
    }

    public static XMLFilterImpl getFilter() {
        return new XMLFilterImpl() {
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String text = new String(ch, start, length);
                if (length == 1) {
                    if (StringUtils.equals("&", text)) {
                        char[] escape = "&amp;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("\"", text)) {
                        char[] escape = "&quot;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("'", text)) {
                        char[] escape = "&apos;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("<", text)) {
                        char[] escape = "&lt;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals(">", text)) {
                        char[] escape = "&gt;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                }
                super.characters(ch, start, length);
            }
        };
    }

    public static Document stringToDocument(String xml) throws Exception {
        return getDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public static Map<String, Object> xmlStringToJson(String xml) {
        try {
            return elementToMap(stringToDocument(xml).getRootElement());
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return new LinkedHashMap<>();
    }

    public static Map<String, Object> elementToMap(Element node) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Element e : listElement) {// 遍历所有一级子节点
                Map<String, Object> jsonObject = elementToMap(e);
                list.add(jsonObject);
            }
            if (list.size() == 1) {
                result.put(node.getName(), list.getFirst());
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
     * 递归清空元素的文本内容
     */
    public static void clearElementText(Element element) {
        // 清空当前元素的文本内容
        element.setText(StringUtils.EMPTY);

        // 递归处理子元素
        Iterator<Element> iterator = element.elementIterator();
        while (iterator.hasNext()) {
            clearElementText(iterator.next());
        }
    }

    /**
     * 使用正则清空元素的文本内容
     */
    public static String clearElementText(String text) {
        // 正则表达式匹配 XML 标签中的内容
        String regex = "(<[^<>]+>)([^<>]*)(</[^<>]+>)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // 替换内容为空字符串
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group(1) + StringUtils.EMPTY + matcher.group(3));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
