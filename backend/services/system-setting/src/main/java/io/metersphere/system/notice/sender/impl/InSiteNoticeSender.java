package io.metersphere.system.notice.sender.impl;


import io.metersphere.project.domain.Notification;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import io.metersphere.system.service.NotificationService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class InSiteNoticeSender extends AbstractNoticeSender {

    @Resource
    private NotificationService notificationService;

    public void sendAnnouncement(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<Receiver> receivers = noticeModel.getReceivers();
        // 排除自己
        List<Receiver> realReceivers = new ArrayList<>();
        if (noticeModel.isExcludeSelf() ) {
            for (Receiver receiver : receivers) {
                if (!StringUtils.equals(receiver.getUserId(), noticeModel.getOperator())) {
                    LogUtils.info("发送人是自己不发");
                    realReceivers.add(receiver);
                }
            }
        }

        if (CollectionUtils.isEmpty(realReceivers)) {
            return;
        }

        LogUtils.info("发送站内通知: {}", realReceivers);
        realReceivers.forEach(receiver -> {

            Map<String, Object> paramMap = noticeModel.getParamMap();
            Notification notification = new Notification();
            notification.setSubject(noticeModel.getSubject());
            notification.setOperator(noticeModel.getOperator());
            notification.setOperation(noticeModel.getEvent());
            notification.setResourceId((String) paramMap.get("id"));
            notification.setResourceType(messageDetail.getTaskType());
            if (paramMap.get("name") != null) {
                notification.setResourceName((String) paramMap.get("name"));
            }
            if (paramMap.get("title") != null) {
                notification.setResourceName((String) paramMap.get("title"));
            }
            notification.setType(receiver.getType());
            notification.setStatus(NotificationConstants.Status.UNREAD.name());
            notification.setCreateTime(System.currentTimeMillis());
            notification.setReceiver(receiver.getUserId());
            notification.setContent(context);
            notificationService.sendAnnouncement(notification);
        });
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendAnnouncement(messageDetail, noticeModel, context);
    }

}
