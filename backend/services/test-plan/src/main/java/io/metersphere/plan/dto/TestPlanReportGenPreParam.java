package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanReportGenPreParam {

	@Schema(description = "项目ID")
	private String projectId;

	@Schema(description = "计划ID")
	private String testPlanId;

	@Schema(description = "计划名称")
	private String testPlanName;

	@Schema(description = "计划开始时间")
	private Long startTime;

	@Schema(description = "触发方式")
	private String triggerMode;

	@Schema(description = "执行状态")
	private String execStatus;

	@Schema(description = "结果状态")
	private String resultStatus;

	@Schema(description = "是否集成报告")
	private Boolean integrated;

	@Schema(description = "子计划, 集成报告需要")
	private List<TestPlan> childPlans;

	@Schema(description = "计划组报告ID, 独立报告需要")
	private String groupReportId;

	@Schema(description = "是否手动生成报告")
	private Boolean useManual;

	@Schema(description = "执行的任务ID")
	private String taskId;
}
