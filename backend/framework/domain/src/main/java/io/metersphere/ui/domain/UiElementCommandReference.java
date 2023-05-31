package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiElementCommandReference implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_command_reference.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element_command_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "元素ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_command_reference.element_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_command_reference.element_id.length_range}", groups = {Created.class, Updated.class})
    private String elementId;

    @Schema(title = "元素模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_command_reference.element_module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_command_reference.element_module_id.length_range}", groups = {Created.class, Updated.class})
    private String elementModuleId;

    @Schema(title = "指令ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_command_reference.command_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_command_reference.command_id.length_range}", groups = {Created.class, Updated.class})
    private String commandId;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_command_reference.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_command_reference.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;
}