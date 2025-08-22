package io.metersphere.notice.util;

import io.metersphere.commons.utils.JSON;
import io.metersphere.notice.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WxChatbotClient {

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .build();

    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(REQUEST_CONFIG)
            .build();

    public static SendResult send(String webhook, Message message) throws IOException {
        SendResult sendResult = new SendResult();

        if (StringUtils.isBlank(webhook)) {
            sendResult.setIsSuccess(false);
            sendResult.setErrorCode(-1);
            sendResult.setErrorMsg("webhook is blank");
            return sendResult;
        }
        if (message == null) {
            sendResult.setIsSuccess(false);
            sendResult.setErrorCode(-1);
            sendResult.setErrorMsg("message is null");
            return sendResult;
        }

        HttpPost httppost = new HttpPost(webhook);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        String payload = message.toJsonString();
        httppost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = HTTP_CLIENT.execute(httppost)) {
            int status = response.getStatusLine().getStatusCode();
            String body = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                    : "";

            if (status == HttpStatus.SC_OK) {
                try {
                    Map<?, ?> obj = JSON.parseObject(body, Map.class);
                    Object errObj = obj.get("errcode");
                    int errCode = (errObj instanceof Number) ? ((Number) errObj).intValue() : -1;
                    Object errorMsgObj = obj.get("errmsg");
                    String errorMsg = errorMsgObj != null ? String.valueOf(errorMsgObj) : "";

                    sendResult.setErrorCode(errCode);
                    sendResult.setErrorMsg(errorMsg);
                    sendResult.setIsSuccess(errCode == 0);
                } catch (Exception e) {
                    sendResult.setIsSuccess(false);
                    sendResult.setErrorCode(-1);
                    sendResult.setErrorMsg("parse response failed: " + e.getMessage());
                }
            } else {
                sendResult.setIsSuccess(false);
                sendResult.setErrorCode(status);
                String reason = response.getStatusLine().getReasonPhrase();
                sendResult.setErrorMsg("http " + status + " " + reason + (body.isEmpty() ? "" : (": " + body)));
            }
        }

        return sendResult;
    }
}