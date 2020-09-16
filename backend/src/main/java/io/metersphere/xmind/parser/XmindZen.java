package io.metersphere.xmind.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;

public class XmindZen {

	/**
	 * @param jsonContent
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static String getContent(String jsonContent) {
		JSONObject jsonObject = JSONArray.parseArray(jsonContent).getJSONObject(0);
		JSONObject rootTopic = jsonObject.getJSONObject("rootTopic");
		transferNotes(rootTopic);
		JSONObject children = rootTopic.getJSONObject("children");
		recursionChildren(children);
		return jsonObject.toString();
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
