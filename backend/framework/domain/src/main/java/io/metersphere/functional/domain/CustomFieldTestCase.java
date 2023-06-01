package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFieldTestCase implements Serializable {
    @Schema(title = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_test_case.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_test_case.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_test_case.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_test_case.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "字段值")
    private String value;

    @Schema(title = "")
    private String textValue;

    private static final long serialVersionUID = 1L;
}