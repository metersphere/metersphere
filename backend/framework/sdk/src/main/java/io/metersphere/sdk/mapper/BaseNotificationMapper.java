package io.metersphere.sdk.mapper;


import io.metersphere.project.domain.Notification;
import io.metersphere.sdk.dto.request.NotificationRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseNotificationMapper {

    List<Notification> listNotification(@Param("notificationRequest") NotificationRequest notificationRequest);

    int countNotification(@Param("notification") Notification notification);

}
