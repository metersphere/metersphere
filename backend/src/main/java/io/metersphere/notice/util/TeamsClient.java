package io.metersphere.notice.util;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.utils.LogUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class TeamsClient {
    public static SendResult send(String webhook, String title, JSONObject context) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        SendResult sendResult = new SendResult();
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(webhook);
            // 创建请求内容
            StringEntity entity = new StringEntity(context.toJSONString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
           /* if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject obj = JSONObject.parseObject(result);
                Integer errcode = obj.getInteger("errcode");
                sendResult.setErrorCode(errcode);
                sendResult.setErrorMsg(obj.getString("errmsg"));
                sendResult.setIsSuccess(errcode.equals(0));
            }*/
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
        return sendResult;
    }
}
