package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.request.TableBatchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanReportBatchRequest extends TableBatchRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    private String projectId;
}
