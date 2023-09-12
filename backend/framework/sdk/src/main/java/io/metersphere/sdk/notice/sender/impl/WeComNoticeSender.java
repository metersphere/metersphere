package io.metersphere.sdk.notice.sender.impl;

import io.metersphere.sdk.notice.MessageDetail;
import io.metersphere.sdk.notice.NoticeModel;
import io.metersphere.sdk.notice.Receiver;
import io.metersphere.sdk.notice.sender.AbstractNoticeSender;
import io.metersphere.sdk.notice.utils.WeComClient;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeComNoticeSender extends AbstractNoticeSender {

    public void sendWeCom(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<String> userIds = noticeModel.getReceivers().stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = super.getUsers(userIds);
        List<String> mobileList = users.stream().map(User::getPhone).toList();
        LogUtils.info("企业微信收件人: {}", userIds);
        context += StringUtils.join(mobileList, StringUtils.SPACE);
        WeComClient.send(messageDetail.getWebhook(), "消息通知: \n" + context, mobileList);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendWeCom(messageDetail, noticeModel, context);
    }

}
