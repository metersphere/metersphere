package io.metersphere.xmind.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmindZen {

    /**
     * @param jsonContent
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public static List<String> getContent(String jsonContent) {
        JSONArray jsonArray = JSONArray.parseArray(jsonContent);//.getJSONObject(0);
        List<String> contents = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONObject rootTopic = jsonObject.getJSONObject("rootTopic");
            transferNotes(rootTopic);
            JSONObject children = rootTopic.getJSONObject("children");
            recursionChildren(children);
            contents.add(jsonObject.toString());
        }
        return contents;
    }

    /**
     * 递归转换children
     *
     * @param children
     */
    private static void recursionChildren(JSONObject children) {
        if (children == null) {
            return;
        }
        JSONArray attachedArray = children.getJSONArray("attached");
        if (attachedArray == null) {
            return;
        }
        for (Object attached : attachedArray) {
            JSONObject attachedObject = (JSONObject) attached;
            transferNotes(attachedObject);
            JSONObject childrenObject = attachedObject.getJSONObject("children");
            if (childrenObject == null) {
                continue;
            }
            recursionChildren(childrenObject);
        }
    }

    private static void transferNotes(JSONObject object) {
        JSONObject notes = object.getJSONObject("notes");
        if (notes == null) {
            return;
        }
        JSONObject plain = notes.getJSONObject("plain");
        if (plain != null) {
            String content = plain.getString("content");
            notes.remove("plain");
            notes.put("content", content);
        } else {
            notes.put("content", null);
        }
    }

}
