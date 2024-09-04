package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportBugCountDTO {

	@Schema(description = "关联用例ID")
	private String refCaseId;
	@Schema(description = "缺陷数量")
	private Long bugCount;
}
