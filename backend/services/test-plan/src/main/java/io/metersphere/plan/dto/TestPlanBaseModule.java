package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class TestPlanBaseModule {

	/**
	 * 模块ID
	 */
	private String id;

	/**
	 * 模块路径
	 */
	private String name;

	/**
	 * 父级模块ID
	 */
	private String parentId;
}
