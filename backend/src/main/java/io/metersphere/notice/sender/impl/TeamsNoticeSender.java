package io.metersphere.notice.sender.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.util.TeamsClient;
import org.springframework.stereotype.Component;

@Component
public class TeamsNoticeSender extends AbstractNoticeSender {
    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        String title = noticeModel.getSubject();
        JSONObject jsonContext = new JSONObject();
        jsonContext.put("title", title);
        jsonContext.put("text", context);

        TeamsClient.send(messageDetail.getWebhook(), title, jsonContext);
    }
}
