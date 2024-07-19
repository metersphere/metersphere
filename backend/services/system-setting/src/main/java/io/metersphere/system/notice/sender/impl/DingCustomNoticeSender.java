package io.metersphere.system.notice.sender.impl;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import io.metersphere.system.notice.utils.DingClient;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DingCustomNoticeSender extends AbstractNoticeSender {

    public void sendDingCustom(MessageDetail messageDetail, NoticeModel noticeModel, String context, String subjectText) {
        List<Receiver> receivers = super.getReceivers(noticeModel.getReceivers(), noticeModel.isExcludeSelf(), noticeModel.getOperator());
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        List<String> userIds = receivers.stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = super.getUsers(userIds, messageDetail.getProjectId(), true);
        List<String> mobileList = users.stream().map(User::getPhone).toList();

        LogUtils.info("钉钉自定义机器人收件人: {}", userIds);
        DingClient.send(messageDetail.getWebhook(), subjectText + ": \n" + context, mobileList);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        String subjectText = super.getSubjectText(messageDetail, noticeModel);
        sendDingCustom(messageDetail, noticeModel, context, subjectText);
    }

}
