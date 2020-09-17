package com.chendb.commons.wx.chatbot.message;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author yanceyzhang
 *
 */
public class ImageMessage implements Message {

    private String base64;
    private String md5;

    public ImageMessage(String base64,String md5) {
        this.base64 = base64;
        this.md5 = md5;
    }

	public String toJsonString() {
        Map<String, Object> items = new HashMap<String, Object>();
        items.put("msgtype", "image");

        Map<String, Object> textContent = new HashMap<String, Object>();
        if (StringUtils.isBlank(base64)) {
            throw new IllegalArgumentException("base64 should not be blank");
        }
        textContent.put("base64", base64);
        textContent.put("md5", md5);
        items.put("image", textContent);
        return JSON.toJSONString(items);
    }
}
