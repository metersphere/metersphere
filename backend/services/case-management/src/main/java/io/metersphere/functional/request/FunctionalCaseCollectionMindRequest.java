package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FunctionalCaseCollectionMindRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "测试集ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String collectionId;

    @Min(value = 1, message = "当前页码必须大于0")
    @Schema(description = "当前页码")
    private int current;

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_functional_case.test_plan_id.not_blank}")
    private String planId;


}
