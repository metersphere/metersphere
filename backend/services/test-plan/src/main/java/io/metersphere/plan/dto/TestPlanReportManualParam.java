package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestPlanReportManualParam {

	@Schema(description = "目标计划ID")
	private String targetId;

	@Schema(description = "手动生成的报告名称")
	private String manualName;
}
