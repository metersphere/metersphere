package io.metersphere.plan.dto.request;

import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanExecuteRequest {

    @Schema(description = "项目ID", required = true)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "执行ID")
    List<String> executeIds;

    @Schema(description = "执行模式", allowableValues = {"SERIAL", "PARALLEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String executeMode = ApiBatchRunMode.SERIAL.name();

}
