package io.metersphere.xmind.parser;

import io.metersphere.performance.parse.EngineSourceParserFactory;
import org.dom4j.*;
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
        // 删除<topic>节点
        xmlContent = xmlContent.replace("<topics type=\"attached\">", "");
        xmlContent = xmlContent.replace("</topics>", "");

        // 去除title中svg:width属性
        xmlContent = xmlContent.replaceAll("<title svg:width=\"[0-9]*\">", "<title>");
        //去除自由风格主题
        xmlContent = xmlContent.replaceAll("<topics type=\"detached\">", "");

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
}
