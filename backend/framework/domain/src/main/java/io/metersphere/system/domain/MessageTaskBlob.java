package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class MessageTaskBlob implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{message_task_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{message_task_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "消息模版")
    private String template;

    private static final long serialVersionUID = 1L;
}