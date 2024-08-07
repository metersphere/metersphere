package io.metersphere.system.notice.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeComClient {

    public static void send(String webhook, String context, List<String>mobileList) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Map<String, Object> mp = new LinkedHashMap<>();
        Map<String, Object> js = new HashMap<>();
        js.put("content", context);
        js.put("mentioned_mobile_list", mobileList);
        mp.put("msgtype", "text");
        mp.put("text", js);
        ClientPost.executeClient(webhook, httpClient, mp);
    }

}
