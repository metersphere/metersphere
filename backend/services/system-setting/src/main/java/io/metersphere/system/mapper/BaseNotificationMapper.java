package io.metersphere.system.mapper;


import io.metersphere.system.dto.sdk.request.NotificationRequest;
import io.metersphere.system.log.dto.NotificationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseNotificationMapper {

    List<NotificationDTO> listNotification(@Param("request") NotificationRequest notificationRequest);

    void deleteByTime(@Param("timestamp") long timestamp);

}
