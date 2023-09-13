package io.metersphere.sdk.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MessageTaskRequest {

    @Schema(description = "消息配置所在项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.project_id.not_blank}", groups = {Created.class, Updated.class})
    public String projectId;

    @Schema(description = "消息配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.id.not_blank}", groups = {Updated.class})
    public String id;

    @Schema(description = "消息配置功能", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.taskType.not_blank}", groups = {Created.class, Updated.class})
    public String taskType;

    @Schema(description = "消息配置场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.event.not_blank}", groups = {Created.class, Updated.class})
    public String event;

    @Schema(description = "消息配置接收人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{message_task.receivers.not_empty}", groups = {Created.class, Updated.class})
    private List<String> receiverIds;

    @Schema(description = "具体测试的ID")
    public String testId;

    @Schema(description = "消息配置机器人id")
    @NotBlank(message = "{message_task.robotId.not_blank}", groups = {Created.class, Updated.class})
    public String robotId;

    @Schema(description = "消息配置机器人是否开启")
    @NotNull(message = "{message_task.enable.not_blank}", groups = {Created.class, Updated.class})
    public Boolean enable;

    @Schema(description = "消息配置消息模版")
    @NotBlank(message = "{message_task.robotId.not_blank}", groups = {Created.class, Updated.class})
    public String template;

}
