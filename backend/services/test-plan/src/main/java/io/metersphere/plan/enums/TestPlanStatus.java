package io.metersphere.plan.enums;

import io.metersphere.sdk.util.Translator;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum TestPlanStatus {

	// 执行状态 (报告)

	/**
	 * 未执行
	 */
	PENDING("PENDING", "test_plan.status.pending"),
	/**
	 * 执行中
	 */
	RUNNING("RUNNING", "test_plan.status.running"),
	/**
	 * 已停止
	 */
	STOPPED("STOPPED", "test_plan.status.stopped"),
	/**
	 * 已完成
	 */
	COMPLETED("COMPLETED", "test_plan.status.completed"),

	// 结果状态 (报告)

	/**
	 * 成功
	 */
	SUCCESS("SUCCESS", "test_plan.status.success"),
	/**
	 * 失败
	 */
	ERROR("ERROR", "test_plan.status.error"),

	// 状态 (计划)

	/**
	 * 未开始
	 */
	PREPARED("PREPARED", "test_plan.status.prepared"),
	/**
	 * 进行中
	 */
	UNDERWAY("UNDERWAY", "test_plan.status.underway");

	@Getter
	private final String name;
	private final String i18nText;

	TestPlanStatus(String name, String i18nText) {
		this.name = name;
		this.i18nText = i18nText;
	}

	public String getI18nText() {
		return Translator.get(i18nText);
	}

	public static String getI18nText(String name) {
		for (TestPlanStatus status : TestPlanStatus.values()) {
			if (StringUtils.equals(status.getName(), name)) {
				return status.getI18nText();
			}
		}
		return name;
	}
}
