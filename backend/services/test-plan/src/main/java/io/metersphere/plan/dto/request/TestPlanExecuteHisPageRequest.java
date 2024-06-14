package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanExecuteHisPageRequest extends BasePageRequest {

	@Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String testPlanId;
}
