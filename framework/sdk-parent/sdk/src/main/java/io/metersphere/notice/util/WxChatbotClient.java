package io.metersphere.notice.util;


import io.metersphere.commons.utils.JSON;
import io.metersphere.notice.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *
 */
public class WxChatbotClient {

    static HttpClient httpclient = HttpClients.createSystem();

    public static SendResult send(String webhook, Message message) throws IOException {

        if (StringUtils.isBlank(webhook)) {
            return new SendResult();
        }
        HttpPost httppost = new HttpPost(webhook);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity se = new StringEntity(message.toJsonString(), StandardCharsets.UTF_8.name());
        httppost.setEntity(se);

        SendResult sendResult = new SendResult();
        HttpResponse response = httpclient.execute(httppost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity());
            Map obj = JSON.parseObject(result, Map.class);

            Integer errcode = (Integer) obj.get("errcode");
            sendResult.setErrorCode(errcode);
            sendResult.setErrorMsg((String) obj.get("errmsg"));
            sendResult.setIsSuccess(errcode.equals(0));
        }

        return sendResult;
    }

}


