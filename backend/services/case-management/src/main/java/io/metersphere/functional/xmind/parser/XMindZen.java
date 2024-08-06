package io.metersphere.functional.xmind.parser;

import io.metersphere.sdk.util.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XMindZen {

    /**
     * 返回content.json 解析后的的json
     */
    public static List<String> getContent(String jsonContent) {
        List jsonArray = JSON.parseArray(jsonContent);//.getJSONObject(0);
        List<String> contents = new ArrayList<>();
        for (Object object : jsonArray) {
            Map<String, Map> jsonObject = (Map) object;
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
