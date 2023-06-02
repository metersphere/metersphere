package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiCustomCommandBlob implements Serializable {
    @Schema(title = "指令ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景定义")
    private byte[] scenarioDefinition;

    @Schema(title = "自定义结构")
    private byte[] commandViewStruct;

    private static final long serialVersionUID = 1L;
}