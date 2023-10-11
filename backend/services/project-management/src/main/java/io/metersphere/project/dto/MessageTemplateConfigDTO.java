package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTemplateConfigDTO extends ProjectRobotConfigDTO{

    @Schema(description = "消息配置功能名称")
    public String taskTypeName ;

    @Schema(description = "消息配置场景名称")
    public String eventName;

    @Schema(description = "消息配置接收人")
    private List<String> receiverIds;

    @Schema(description = "具体测试的ID")
    public String testId;
}
