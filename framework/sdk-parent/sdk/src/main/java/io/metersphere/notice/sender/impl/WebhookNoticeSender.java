package io.metersphere.notice.sender.impl;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WebhookNoticeSender extends AbstractNoticeSender {


    private void send(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<Receiver> receivers = noticeModel.getReceivers();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        if (CollectionUtils.isNotEmpty(receivers)) {
            List<String> userIds = receivers.stream()
                    .map(Receiver::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            LogUtil.info("Webhook收件人: {}", userIds);
        }

        try {
            HttpPost httpPost = new HttpPost(messageDetail.getWebhook());
            // 创建请求内容
            StringEntity entity = new StringEntity(context, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        send(messageDetail, noticeModel, context);
    }
}
