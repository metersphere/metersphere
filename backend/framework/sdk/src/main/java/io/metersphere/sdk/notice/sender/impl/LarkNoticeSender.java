package io.metersphere.sdk.notice.sender.impl;

import io.metersphere.sdk.notice.Receiver;
import io.metersphere.sdk.notice.MessageDetail;
import io.metersphere.sdk.notice.NoticeModel;
import io.metersphere.sdk.notice.sender.AbstractNoticeSender;
import io.metersphere.sdk.notice.utils.LarkClient;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LarkNoticeSender extends AbstractNoticeSender {

    public void sendLark(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<String> userIds = noticeModel.getReceivers().stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = super.getUsers(userIds);
        List<String> collect = users.stream()
                .map(ud -> "<at email=\"" + ud.getEmail() + "\">" + ud.getName() + "</at>")
                .toList();

        LogUtils.info("飞书收件人: {}", userIds);
        context += StringUtils.join(collect, StringUtils.SPACE);
        LarkClient.send(messageDetail.getWebhook(), "消息通知: \n" + context);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendLark(messageDetail, noticeModel, context);
    }
}
