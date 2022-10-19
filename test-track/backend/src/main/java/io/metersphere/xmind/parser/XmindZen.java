package io.metersphere.xmind.parser;

import io.metersphere.commons.utils.JSON;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XmindZen {

    /**
     * @param jsonContent
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public static List<String> getContent(String jsonContent) {
        List jsonArray = JSON.parseArray(jsonContent);//.getJSONObject(0);
        List<String> contents = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            Map<String, Map> jsonObject = (Map) jsonArray.get(i);
            Map<String, Map> rootTopic = jsonObject.get("rootTopic");
            transferNotes(rootTopic);
            Map children = rootTopic.get("children");
            recursionChildren(children);
            contents.add(JSON.toJSONString(jsonObject));
        }
        return contents;
    }

    /**
     * 递归转换children
     *
     * @param children
     */
    private static void recursionChildren(Map<String, List> children) {
        if (children == null) {
            return;
        }
        List<Map> attachedArray = children.get("attached");
        if (attachedArray == null) {
            return;
        }
        for (Object attached : attachedArray) {
            Map<String, Map> attachedObject = (Map) attached;
            transferNotes(attachedObject);
            Map<String, List> childrenObject = attachedObject.get("children");
            if (childrenObject == null) {
                continue;
            }
            recursionChildren(childrenObject);
        }
    }

    private static void transferNotes(Map object) {
        Map notes = (Map) object.get("notes");
        if (notes == null) {
            return;
        }
        Map plain = (Map) notes.get("plain");
        if (plain != null) {
            String content = plain.get("content").toString();
            notes.remove("plain");
            notes.put("content", content);
        } else {
            notes.put("content", null);
        }
    }

}
