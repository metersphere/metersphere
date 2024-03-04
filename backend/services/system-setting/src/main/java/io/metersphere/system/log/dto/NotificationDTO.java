package io.metersphere.system.log.dto;

import io.metersphere.project.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotificationDTO extends Notification {

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String userName;
}
