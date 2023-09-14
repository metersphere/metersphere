package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRobotConfigDTO {

    @Schema(description = "消息配置机器人id")
    public String robotId;

    @Schema(description = "消息配置机器人是否开启")
    public Boolean enable;

    @Schema(description = "消息配置机器人发送模版")
    public String template;
}
