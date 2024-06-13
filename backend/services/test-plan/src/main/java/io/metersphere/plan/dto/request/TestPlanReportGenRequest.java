package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestPlanReportGenRequest {

	@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan.project_id.not_blank}")
	private String projectId;

	@Schema(description = "计划ID/计划组ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan.id.not_blank}")
	private String testPlanId;

	@Schema(description = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SCHEDULE", "MANUAL", "API", "BATCH"})
	private String triggerMode;
}
