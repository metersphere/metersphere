package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanCaseBugDTO {

	@Schema(description = "关联关系ID")
	private String id;
	@Schema(description = "缺陷业务ID")
	private String num;
	@Schema(description = "缺陷标题")
	private String title;
	@Schema(description = "缺陷状态")
	private String status;
	@Schema(description = "计划缺陷关系ID")
	private String planCaseRefId;
}
