package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseBlob implements Serializable {
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用例步骤（JSON)，step_model 为 Step 时启用")
    private String steps;

    @Schema(title = "步骤描述，step_model 为 Text 时启用")
    private String stepDescription;

    @Schema(title = "预期结果，step_model 为 Text  时启用")
    private String expectedResult;

    @Schema(title = "前置条件")
    private String prerequisite;

    @Schema(title = "备注")
    private String description;

    private static final long serialVersionUID = 1L;
}