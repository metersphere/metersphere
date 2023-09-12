package io.metersphere.sdk.service;



import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.sdk.dto.request.NotificationRequest;
import io.metersphere.sdk.mapper.BaseNotificationMapper;
import io.metersphere.sdk.notice.constants.NotificationConstants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private BaseNotificationMapper baseNotificationMapper;

    public List<Notification> listNotification(NotificationRequest notificationRequest, String userId) {
        if (StringUtils.isNotBlank(notificationRequest.getTitle())) {
            notificationRequest.setTitle("%" + notificationRequest.getTitle() + "%");
        }
        if (StringUtils.isBlank(notificationRequest.getReceiver())) {
            notificationRequest.setReceiver(userId);
        }
        return baseNotificationMapper.listNotification(notificationRequest);
    }

    public int read(long id, String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andIdEqualTo(id).andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public int readAll(String userId) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andReceiverEqualTo(userId);
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public int countNotification(Notification notification, String userId) {
        if (StringUtils.isBlank(notification.getReceiver())) {
            notification.setReceiver(userId);
        }
        return baseNotificationMapper.countNotification(notification);
    }

    public void sendAnnouncement(Notification notification) {
        notificationMapper.insert(notification);
    }



}
