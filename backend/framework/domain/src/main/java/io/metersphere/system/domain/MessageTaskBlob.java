package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class MessageTaskBlob implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 255]")
    @NotBlank(message = "{message_task_blob.message_task_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{message_task_blob.message_task_id.length_range}", groups = {Created.class, Updated.class})
    private String messageTaskId;

    @Schema(title = "消息模版")
    private String template;

    private static final long serialVersionUID = 1L;
}