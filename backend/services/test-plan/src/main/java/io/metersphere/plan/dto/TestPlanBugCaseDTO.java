package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanBugCaseDTO {

	@Schema(description = "用例ID")
	private String id;
	@Schema(description = "用例业务ID")
	private String num;
	@Schema(description = "用例类型")
	private String type;
	@Schema(description = "缺陷ID")
	private String bugId;
	@Schema(description = "用例名称")
	private String name;
	@Schema(description = "项目ID")
	private String projectId;
}
