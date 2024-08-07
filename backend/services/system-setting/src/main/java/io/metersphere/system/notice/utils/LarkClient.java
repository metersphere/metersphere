package io.metersphere.system.notice.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LarkClient {

    public static void send(String webhook, String context) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Map<String, Object> mp = new LinkedHashMap<>();
        Map<String, String> js = new HashMap<>();
        js.put("text", context);
        mp.put("msg_type", "text");
        mp.put("content", js);
        ClientPost.executeClient(webhook, httpClient, mp);
    }
}
