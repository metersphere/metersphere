package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanExecuteHisPageRequest extends BasePageRequest {

	@Schema(description = "测试计划/计划组ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String testPlanId;
}
