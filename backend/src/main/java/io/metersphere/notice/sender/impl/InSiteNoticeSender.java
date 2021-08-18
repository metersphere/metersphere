package io.metersphere.notice.sender.impl;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NotificationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class InSiteNoticeSender extends AbstractNoticeSender {

    @Resource
    private NotificationService notificationService;

    public void sendAnnouncement(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<String> userIds = messageDetail.getUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        userIds.forEach(receiver -> {
            LogUtil.debug("发送站内通知: {}, 内容: {}", receiver, context);
            notificationService.sendAnnouncement(noticeModel.getSubject(), context, receiver);
        });
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendAnnouncement(messageDetail, noticeModel, context);
    }
}
