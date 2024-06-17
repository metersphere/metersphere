package io.metersphere.plan.dto.request;

import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanBatchExecuteRequest {

    @Schema(description = "项目ID")
    @NotBlank(message = "project_is_not_exist")
    private String projectId;

    @Schema(description = "执行ID")
    @NotEmpty(message = "test_plan.not.exist")
    private List<String> executeIds;

    @Schema(description = "执行模式", allowableValues = {"SERIAL", "PARALLEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String runMode = ApiBatchRunMode.SERIAL.name();

}
