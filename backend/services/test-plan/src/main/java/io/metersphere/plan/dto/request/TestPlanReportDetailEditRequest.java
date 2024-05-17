package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestPlanReportDetailEditRequest {

	@Schema(description = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan_report_id.not_blank}")
	private String id;

	@Schema(description = "报告内容")
	private String summary;
}
