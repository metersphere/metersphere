package io.metersphere.project.dto;

import io.metersphere.sdk.dto.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTaskDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息配置场景")
    public String event;

    @Schema(description = "消息配置场景名称")
    public String eventName;

    @Schema(description = "消息配置接收人")
    private List<OptionDTO> receivers;

    @Schema(description = "消息配置机器人设置")
    private List<ProjectRobotConfigDTO> projectRobotConfigList;
}
