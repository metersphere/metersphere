package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseAttachment implements Serializable {
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.functional_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.functional_case_id.length_range}", groups = {Created.class, Updated.class})
    private String functionalCaseId;

    @Schema(title = "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    private static final long serialVersionUID = 1L;
}