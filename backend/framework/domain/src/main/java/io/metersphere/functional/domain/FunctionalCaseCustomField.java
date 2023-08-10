package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseCustomField implements Serializable {
    @Schema(description =  "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_custom_field.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_custom_field.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description =  "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_custom_field.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_custom_field.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(description =  "字段值")
    private String value;

    @Schema(description =  "")
    private String textValue;

    private static final long serialVersionUID = 1L;
}