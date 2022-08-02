package io.metersphere.xmind.parser;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.parse.EngineSourceParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONObject;
import org.json.XML;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmindLegacy {

    /**
     * 返回content.xml和comments.xml合并后的json
     *
     * @param xmlContent
     * @param xmlComments
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public static List<String> getContent(String xmlContent, String xmlComments) throws IOException, DocumentException {
        // 删除content.xml里面不能识别的字符串
        xmlContent = xmlContent.replace("xmlns=\"urn:xmind:xmap:xmlns:content:2.0\"", "");
        xmlContent = xmlContent.replace("xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"", "");

        try {
            xmlContent = removeTopicsFromString(xmlContent);
        } catch (Exception e) {
            LogUtil.error("移除xml中的Topic出错：", e);
        }
        // 去除title中svg:width属性
        xmlContent = xmlContent.replaceAll("<title svg:width=\"[0-9]*\">", "<title>");
        Document document = EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xmlContent.getBytes("utf-8")));// 读取XML文件,获得document对象
        Element root = document.getRootElement();
        List<Node> topics = root.selectNodes("//topic");

        if (xmlComments != null) {
            // 删除comments.xml里面不能识别的字符串
            xmlComments = xmlComments.replace("xmlns=\"urn:xmind:xmap:xmlns:comments:2.0\"", "");

            // 添加评论到content中
            Document commentDocument = EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xmlComments.getBytes("utf-8")));
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
            JSONObject xmlJSONObj = XML.toJSONObject(res);
            JSONObject jsonObject = xmlJSONObj.getJSONObject("sheet");
            sheets.add(jsonObject.toString(4));
        }
        // 设置缩进
        return sheets;
    }

    /**
     * 删除topics节点
     *
     * @param xmlContent
     * @return
     * @throws Exception
     */
    private static String removeTopicsFromString(String xmlContent) throws Exception {
        Document doc = EngineSourceParserFactory.getDocument(new ByteArrayInputStream(xmlContent.getBytes("utf-8")));
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
     * @param element
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
