package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanDisassociationRequest {

	@Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String testPlanId;

	@Schema(description = "测试计划用例关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String id;
}
