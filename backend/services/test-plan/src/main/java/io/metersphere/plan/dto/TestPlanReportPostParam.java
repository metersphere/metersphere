package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanReportPostParam {

	@Schema(description = "报告ID")
	private String reportId;

	@Schema(description = "计划开始执行时间")
	private Long executeTime;

	@Schema(description = "计划结束时间")
	private Long endTime;

	@Schema(description = "执行状态")
	private String execStatus;
}
