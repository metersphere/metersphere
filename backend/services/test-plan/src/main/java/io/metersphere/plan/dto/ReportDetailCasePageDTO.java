package io.metersphere.plan.dto;

import lombok.Data;

/**
 * 报告详情用例分页返回对象 (功能, 接口, 场景)
 */

@Data
public class ReportDetailCasePageDTO {

	// ID、用例名称、所属模块、用例等级、执行人、执行结果、缺陷数

	/**
	 * 用例ID
	 */
	private String id;
	/**
	 * 用例业务ID
	 */
	private Long num;
	/**
	 * 用例名称
	 */
	private String name;
	/**
	 * 所属模块
	 */
	private String moduleName;
	/**
	 * 用例等级
	 */
	private String priority;
	/**
	 * 执行结果
	 */
	private String executeResult;
	/**
	 * 执行人
	 */
	private String executeUser;
	/**
	 * 缺陷数
	 */
	private Long bugCount;
}
