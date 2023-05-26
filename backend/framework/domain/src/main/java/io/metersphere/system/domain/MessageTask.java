package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class MessageTask implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{message_task.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "通知事件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.event.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{message_task.event.length_range}", groups = {Created.class, Updated.class})
    private String event;

    @Schema(title = "接收人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.receiver.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.receiver.length_range}", groups = {Created.class, Updated.class})
    private String receiver;

    @Schema(title = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.task_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{message_task.task_type.length_range}", groups = {Created.class, Updated.class})
    private String taskType;

    @Schema(title = "webhook地址")
    private String webhook;

    @Schema(title = "具体测试的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{message_task.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;
}