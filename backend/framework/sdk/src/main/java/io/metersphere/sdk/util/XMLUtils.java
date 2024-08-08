package io.metersphere.sdk.util;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();
    public static final int DEFAULT_MAX_STRING_LEN = Integer.MAX_VALUE;

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 使用BigDecimal来序列化
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        // 设置JSON处理字符长度限制
        objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(DEFAULT_MAX_STRING_LEN).build());
        // 处理时间格式
        objectMapper.registerModule(new JavaTimeModule());
    }



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

    //  传入完整的 xml 文本，转换成 json 对象
    public static JsonNode xmlConvertJson(String xml) {
        if (StringUtils.isBlank(xml)) return null;
        // 创建一个XmlMapper对象
        ObjectMapper xmlMapper = new XmlMapper();
        // 将XML字符串转换为JsonNode对象
        try {
            return xmlMapper.readTree(xml.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
