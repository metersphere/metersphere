package io.metersphere.notice.service;


import io.metersphere.base.domain.Notification;
import io.metersphere.base.domain.NotificationExample;
import io.metersphere.base.mapper.NotificationMapper;
import io.metersphere.base.mapper.ext.ExtNotificationMapper;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private ExtNotificationMapper extNotificationMapper;

    public void sendAnnouncement(String subject, String content, String receiver) {
        Notification notification = new Notification();
        notification.setTitle(subject);
        notification.setContent(content);
        notification.setType(NotificationConstants.Type.SYSTEM_NOTICE.name());
        notification.setStatus(NotificationConstants.Status.UNREAD.name());
        notification.setCreateTime(System.currentTimeMillis());
        notification.setReceiver(receiver);
        notificationMapper.insert(notification);
    }

    public void sendAnnouncement(Notification notification) {
        notificationMapper.insert(notification);
    }

    public Notification getNotification(int id) {
        return extNotificationMapper.getNotification(id, SessionUtils.getUser().getId());
    }

    public int readAll() {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andReceiverEqualTo(SessionUtils.getUser().getId());
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public int countNotification(Notification notification) {
        if (StringUtils.isBlank(notification.getReceiver())) {
            notification.setReceiver(SessionUtils.getUser().getId());
        }
        return extNotificationMapper.countNotification(notification);
    }

    public int read(long id) {
        Notification record = new Notification();
        record.setStatus(NotificationConstants.Status.READ.name());
        NotificationExample example = new NotificationExample();
        example.createCriteria().andIdEqualTo(id).andReceiverEqualTo(SessionUtils.getUser().getId());
        return notificationMapper.updateByExampleSelective(record, example);
    }

    public List<Notification> listNotification(Notification notification) {
        if (StringUtils.isNotBlank(notification.getTitle())) {
            notification.setTitle("%" + notification.getTitle() + "%");
        }
        if (StringUtils.isBlank(notification.getReceiver())) {
            notification.setReceiver(SessionUtils.getUser().getId());
        }
        return extNotificationMapper.listNotification(notification);
    }

    public List<Notification> listReadNotification(Notification notification) {
        String search = null;
        if (StringUtils.isNotBlank(notification.getTitle())) {
            search = "%" + notification.getTitle() + "%";
        }
        return extNotificationMapper.listReadNotification(search, SessionUtils.getUser().getId());
    }

    public List<Notification> listUnreadNotification(Notification notification) {
        String search = null;
        if (StringUtils.isNotBlank(notification.getTitle())) {
            search = "%" + notification.getTitle() + "%";
        }
        return extNotificationMapper.listUnreadNotification(search, SessionUtils.getUser().getId());
    }


}
