package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报告详情用例分页返回对象 (功能, 接口, 场景)
 */

@Data
public class ReportDetailCasePageDTO {

	@Schema(description = "用例ID")
	private String id;
	@Schema(description = "用例业务ID")
	private Long num;
	@Schema(description = "用例名称")
	private String name;
	@Schema(description = "所属模块")
	private String moduleName;
	@Schema(description = "用例等级")
	private String priority;
	@Schema(description = "执行结果")
	private String executeResult;
	@Schema(description = "执行人")
	private String executeUser;
	@Schema(description = "缺陷数")
	private Long bugCount;
	@Schema(description = "报告详情ID")
	private String reportId;
}
