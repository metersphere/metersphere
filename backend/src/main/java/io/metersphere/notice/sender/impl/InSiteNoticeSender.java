package io.metersphere.notice.sender.impl;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.Notification;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NotificationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class InSiteNoticeSender extends AbstractNoticeSender {

    @Resource
    private NotificationService notificationService;

    public void sendAnnouncement(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<Receiver> receivers = noticeModel.getReceivers();
        // 排除自己
        if (noticeModel.isExcludeSelf()) {
            receivers.removeIf(u -> StringUtils.equals(u.getUserId(), noticeModel.getOperator()));
        }
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }

        LogUtil.info("发送站内通知: {}", receivers);
        receivers.forEach(receiver -> {

            Map<String, Object> paramMap = noticeModel.getParamMap();
            Notification notification = new Notification();
            notification.setTitle(noticeModel.getSubject());
            notification.setContent(JSON.toJSONString(paramMap));
            notification.setOperator(noticeModel.getOperator());
            notification.setOperation(noticeModel.getEvent());
            notification.setResourceId((String) paramMap.get("id"));
            notification.setResourceType(messageDetail.getTaskType());
            //
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
            notificationService.sendAnnouncement(notification);
        });
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendAnnouncement(messageDetail, noticeModel, context);
    }
}
