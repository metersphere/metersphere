package io.metersphere.functional.xmind.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMindLegacy {

    /**
     * 返回content.xml和comments.xml合并后的json
     *
     */
    public static List<String> getContent(String xmlContent, String xmlComments) throws IOException, DocumentException {
        // 删除content.xml里面不能识别的字符串
        xmlContent = xmlContent.replace("xmlns=\"urn:xmind:xmap:xmlns:content:2.0\"", StringUtils.EMPTY);
        xmlContent = xmlContent.replace("xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"", StringUtils.EMPTY);
        try {
            xmlContent = removeTopicsFromString(xmlContent);
        } catch (Exception e) {
            LogUtils.error("移除xml中的Topic出错：", e);
        }
        // 去除title中svg:width属性
        xmlContent = xmlContent.replaceAll("<title svg:width=\"[0-9]*\">", "<title>");
        Document document = DocumentHelper.parseText(xmlContent);// 读取XML文件,获得document对象
        Element root = document.getRootElement();
        List<Node> topics = root.selectNodes("//topic");

        if (xmlComments != null) {
            // 删除comments.xml里面不能识别的字符串
            xmlComments = xmlComments.replace("xmlns=\"urn:xmind:xmap:xmlns:comments:2.0\"", StringUtils.EMPTY);

            // 添加评论到content中
            Document commentDocument = DocumentHelper.parseText(xmlComments);
            List<Node> commentsList = commentDocument.selectNodes("//comment");

            for (Node topic : topics) {
                for (Node commentNode : commentsList) {
                    Element commentElement = (Element) commentNode;
                    Element topicElement = (Element) topic;
                    if (topicElement.attribute("id").getValue()
                            .equals(commentElement.attribute("object-id").getValue())) {
                        Element comment = topicElement.addElement("comments");
                        comment.addAttribute("creationTime", commentElement.attribute("time").getValue());
                        comment.addAttribute("author", commentElement.attribute("author").getValue());
                        comment.addAttribute("content", commentElement.element("content").getText());
                    }
                }

            }
        }

        // 第一个topic转换为json中的rootTopic
        List<Node> rootTopics = root.selectNodes("/xmap-content/sheet/topic");
        for (Node rootTopic : rootTopics) {
            rootTopic.setName("rootTopic");

            // 将xml中topic节点转换为attached节点
            List<Node> topicList = rootTopic.selectNodes("//topic");
            for (Node node : topicList) {
                node.setName("attached");
            }

        }

        List<String> sheets = new ArrayList<>();
        for (Element sheet : root.elements("sheet")) {
            String res = sheet.asXML();
            // 将xml转为json
            JsonNode xmlJSONObj = XMLUtils.xmlConvertJson(res);
            ObjectMapper objectMapper = new ObjectMapper();
            sheets.add(objectMapper.writeValueAsString(xmlJSONObj));
        }
        // 设置缩进
        return sheets;
    }


    /**
     * 删除topics节点
     *
     */
    private static String removeTopicsFromString(String xmlContent) throws Exception {
        Document doc = DocumentHelper.parseText(xmlContent);
        if (doc != null) {
            Element root = doc.getRootElement();
            List<Element> childrenElement = root.elements();
            for (Element child : childrenElement) {
                removeTopicsFromElement(child);
            }
            xmlContent = doc.asXML();
        }
        return xmlContent;
    }

    /**
     * 递归删除topics节点
     *
     */
    private static void removeTopicsFromElement(Element element) {
        if (element != null) {
            List<Element> childrenElement = element.elements();
            List<Element> removeElements = new ArrayList<>();
            List<Element> addElements = new ArrayList<>();
            for (Element child : childrenElement) {
                if (StringUtils.equalsIgnoreCase("topics", child.getName()) && StringUtils.equalsAnyIgnoreCase(child.attributeValue("type"), "attached", "detached")) {
                    removeElements.add(child);
                    addElements.addAll(child.elements());
                }
            }
            removeElements.forEach(item -> {
                item.getParent().remove(item);
            });
            addElements.forEach(item -> {
                item.setParent(null);
                element.add(item);
            });
            childrenElement = element.elements();
            for (Element child : childrenElement) {
                removeTopicsFromElement(child);
            }
        }

    }
}
