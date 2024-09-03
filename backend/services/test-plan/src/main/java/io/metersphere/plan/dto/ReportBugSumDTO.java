package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportBugSumDTO {

	@Schema(description = "用例类型")
	private String caseType;
	@Schema(description = "缺陷数量")
	private Long bugCount;
}
