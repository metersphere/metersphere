package io.metersphere.issue.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFieldIssues implements Serializable {
    @Schema(title = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_issues.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_issues.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_issues.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_issues.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "字段值")
    private String value;

    @Schema(title = "文本类型字段值")
    private byte[] textValue;

    private static final long serialVersionUID = 1L;
}