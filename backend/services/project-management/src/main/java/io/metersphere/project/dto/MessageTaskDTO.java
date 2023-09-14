package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTaskDTO {

    @Schema(description = "消息配置所在项目ID")
    public String projectId;

    @Schema(description = "消息配置功能")
    public String taskType;

    @Schema(description = "消息配置场景")
    public String event;

    @Schema(description = "消息配置接收人")
    private List<String> receiverIds;

    @Schema(description = "消息配置机器人设置")
    private List<ProjectRobotConfigDTO> projectRobotConfigList;
}
