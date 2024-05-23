package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.plan.serializer.CustomRateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanReportDetailResponse {

	@Schema(description = "报告ID")
	private String id;
	@Schema(description = "报告名称")
	private String name;
	@Schema(description = "报告开始时间")
	private Long startTime;
	@Schema(description = "报告执行开始时间")
	private Long executeTime;
	@Schema(description = "报告结束(执行)时间")
	private Long endTime;
	@Schema(description = "报告内容")
	private String summary;

	/**
	 * 报告分析
	 */
	@Schema(description = "通过阈值")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double passThreshold;
	@Schema(description = "通过率")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double passRate;
	@Schema(description = "执行完成率")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double executeRate;
	@Schema(description = "缺陷总数")
	private Integer bugCount;


	@Schema(description = "用例总数")
	private Integer caseTotal = 0;
	/**
	 * 执行分析
	 */
	@Schema(description = "执行分析-用例数")
	private CaseCount executeCount;
	/**
	 * 功能用例分析
	 */
	@Schema(description = "功能用例分析-用例数")
	private CaseCount functionalCount;
	/**
	 * 接口用例分析
	 */
	@Schema(description = "接口用例分析-用例数")
	private CaseCount apiCaseCount;
	/**
	 * 接口场景用例分析
	 */
	@Schema(description = "接口场景用例分析-用例数")
	private CaseCount apiScenarioCount;


	/**
	 * 功能, 接口, 场景
	 */
	@Data
	public static class CaseCount {
		@Schema(description = "成功用例数量")
		private Integer success = 0;
		@Schema(description = "失败用例数量")
		private Integer error = 0;
		@Schema(description = "误报用例数量")
		private Integer fakeError = 0;
		@Schema(description = "阻塞用例数量")
		private Integer block = 0;
		@Schema(description = "未执行用例数量")
		private Integer pending = 0;
	}
}
