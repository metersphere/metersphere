package io.metersphere.notice.sender.impl;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.notice.message.TextMessage;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.util.WxChatbotClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeComNoticeSender extends AbstractNoticeSender {


    public void sendWechatRobot(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<Receiver> receivers = noticeModel.getReceivers();

        TextMessage message = new TextMessage("消息通知: \n" + context);
        if (CollectionUtils.isNotEmpty(receivers)) {
            List<String> userIds = receivers.stream()
                    .map(Receiver::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> phoneList = super.getUserDetails(userIds).stream()
                    .map(UserDetail::getPhone)
                    .distinct()
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(phoneList)) {
                message.setMentionedMobileList(phoneList);
                LogUtil.info("企业微信收件人: {}", userIds);
            }
        }

        try {
            WxChatbotClient.send(messageDetail.getWebhook(), message);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendWechatRobot(messageDetail, noticeModel, context);
    }
}
