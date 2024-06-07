package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.plan.serializer.CustomRateSerializer;
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
	private Integer successCount = 0;
	@Schema(description = "失败用例数量")
	private Integer errorCount = 0;
	@Schema(description = "误报用例数量")
	private Integer fakeErrorCount = 0;
	@Schema(description = "阻塞用例数量")
	private Integer blockCount = 0;
	@Schema(description = "未执行用例数量")
	private Integer pendingCount = 0;

	/**
	 * 用例数中用例数量统计
	 */
	@Schema(description = "用例总数")
	private Integer caseTotal = 0;
	@Schema(description = "功能用例数量")
	private Integer functionalCaseCount = 0;
	@Schema(description = "接口用例数量")
	private Integer apiCaseCount = 0;
	@Schema(description = "接口场景数量")
	private Integer apiScenarioCount = 0;
	@Schema(description = "缺陷数量")
	private Integer bugCount = 0;
	@Schema(description = "定时任务配置")
	private BaseScheduleConfigRequest scheduleConfig;
	@Schema(description = "定时任务下一次执行时间")
	private Long nextTriggerTime;
}
