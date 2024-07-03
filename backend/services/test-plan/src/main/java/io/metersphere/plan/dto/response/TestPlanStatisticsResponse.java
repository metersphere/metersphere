package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.plan.serializer.CustomRateSerializer;
import io.metersphere.plan.utils.RateCalculateUtils;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试计划统计详情
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanStatisticsResponse {

	@Schema(description = "测试计划ID")
	private String id;
	@Schema(description = "测试计划状态")
	private String status;
	@Schema(description = "测试计划通过阈值{0-100}")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double passThreshold;

	@Schema(description = "测试计划: 通过率 {成功用例/全部用例}")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double passRate;

	@Schema(description = "测试计划: 执行进度||测试进度 {已执行用例/全部用例}")
	@JsonSerialize(using = CustomRateSerializer.class)
	private Double executeRate;

	/**
	 * 执行进度中的用例数量统计
	 */
	@Schema(description = "成功用例数量")
	private long successCount = 0;
	@Schema(description = "失败用例数量")
	private long errorCount = 0;
	@Schema(description = "误报用例数量")
	private long fakeErrorCount = 0;
	@Schema(description = "阻塞用例数量")
	private long blockCount = 0;
	@Schema(description = "未执行用例数量")
	private long pendingCount = 0;

	/**
	 * 用例数中用例数量统计
	 */
	@Schema(description = "用例总数")
	private long caseTotal = 0;
	@Schema(description = "功能用例数量")
	private long functionalCaseCount = 0;
	@Schema(description = "接口用例数量")
	private long apiCaseCount = 0;
	@Schema(description = "接口场景数量")
	private long apiScenarioCount = 0;
	@Schema(description = "缺陷数量")
	private long bugCount = 0;
	@Schema(description = "定时任务配置")
	private BaseScheduleConfigRequest scheduleConfig;
	@Schema(description = "定时任务下一次执行时间")
	private Long nextTriggerTime;

	/**
	 * 计算测试计划状态
	 * 未开始：执行进度0%
	 * 进行中：执行进度不到100%
	 * 已完成：执行进度100%
	 */
	public void calculateStatus() {
		if (this.successCount == 0 && errorCount == 0 && fakeErrorCount == 0 && blockCount == 0) {
			this.status = TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
		} else if (this.pendingCount == 0) {
			this.status = TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
		} else {
			this.status = TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
		}
	}

	public void calculateCaseTotal() {
		this.caseTotal = this.functionalCaseCount + this.apiCaseCount + this.apiScenarioCount;
	}

	public void calculatePassRate() {
		this.passRate = RateCalculateUtils.divWithPrecision(this.successCount, this.caseTotal, 2);
	}

	public void calculateExecuteRate() {
		this.executeRate = RateCalculateUtils.divWithPrecision(this.successCount - this.pendingCount, this.caseTotal, 2);
	}

	public void calculateAllNumber(TestPlanStatisticsResponse childResponse) {
		this.functionalCaseCount += childResponse.getFunctionalCaseCount();
		this.apiCaseCount += childResponse.getApiCaseCount();
		this.apiScenarioCount += childResponse.getApiScenarioCount();
		this.successCount += childResponse.getSuccessCount();
		this.errorCount += childResponse.getErrorCount();
		this.fakeErrorCount += childResponse.getFakeErrorCount();
		this.blockCount += childResponse.getBlockCount();
		this.pendingCount += childResponse.getPendingCount();
	}
}
