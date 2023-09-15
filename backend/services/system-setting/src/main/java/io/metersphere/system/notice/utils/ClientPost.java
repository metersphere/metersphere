package io.metersphere.system.notice.utils;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.Map;

public class ClientPost {

    public static void executeClient(String webhook, CloseableHttpClient httpClient,  Map<String, Object> mp) {
        CloseableHttpResponse response = null;
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(webhook);
            // 创建请求内容
            StringEntity entity = new StringEntity(JSON.toJSONString(mp), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            LogUtils.error(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                LogUtils.error(e);
            }
        }
    }

}
