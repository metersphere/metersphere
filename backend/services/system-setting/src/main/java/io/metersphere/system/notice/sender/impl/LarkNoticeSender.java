package io.metersphere.system.notice.sender.impl;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import io.metersphere.system.notice.utils.LarkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LarkNoticeSender extends AbstractNoticeSender {

    public void sendLark(MessageDetail messageDetail, NoticeModel noticeModel, String context, String subjectText) {
        List<Receiver> receivers = super.getReceivers(noticeModel.getReceivers(), noticeModel.isExcludeSelf(), noticeModel.getOperator());
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        List<String> userIds = receivers.stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<User> users = super.getUsers(userIds, messageDetail.getProjectId(), true);
        List<String> collect = users.stream()
                .map(ud -> "<at email=\"" + ud.getEmail() + "\">" + ud.getName() + "</at>")
                .toList();

        LogUtils.info("飞书收件人: {}", userIds);
        context += StringUtils.join(collect, StringUtils.SPACE);
        LarkClient.send(messageDetail.getWebhook(), subjectText + ": \n" + context);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        String subjectText = super.getSubjectText(messageDetail, noticeModel);
        sendLark(messageDetail, noticeModel, context, subjectText);
    }
}
