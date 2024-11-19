package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.plan.dto.CaseCount;
import io.metersphere.system.serializer.CustomRateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class TestPlanTaskReportResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "报告ID")
	private String reportId;
	@Schema(description = "任务名称")
	private String taskName;
	@Schema(description = "执行结果")
	private String result;
	@Schema(description = "执行状态")
	private String status;
	@Schema(description = "操作人")
	private String createUser;
	@Schema(description = "任务发起时间")
	private Long createTime;
	@Schema(description = "任务开始起时间")
	private Long startTime;
	@Schema(description = "任务结束时间")
	private Long endTime;
	@Schema(description = "子计划列表")
	private List<ChildPlan> childPlans;
	@Schema(description = "执行用例统计(实时)")
	private CaseCount executeCaseCount;
	@Schema(description = "执行完成率(实时)")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double executeRate;
	@Schema(description = "接口明细总数")
	private Integer apiCaseTotal;
	@Schema(description = "场景明细总数")
	private Integer apiScenarioTotal;

	@Data
	public static class ChildPlan {
		private String id;
		private String name;
		private Integer apiCaseTotal;
		private Integer apiScenarioTotal;
	}
}
