package io.metersphere.system.mapper;


import io.metersphere.project.domain.Notification;
import io.metersphere.system.dto.sdk.request.NotificationRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseNotificationMapper {

    List<Notification> listNotification(@Param("request") NotificationRequest notificationRequest);

}
