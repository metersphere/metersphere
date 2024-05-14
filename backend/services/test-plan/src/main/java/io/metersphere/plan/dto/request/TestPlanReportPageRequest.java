package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanReportPageRequest extends BasePageRequest {

	@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan.project_id.not_blank}")
	private String projectId;

	@Schema(description = "类型", allowableValues = {"ALL: 全部", "TEST_PLAN: 独立", "GROUP: 聚合"}, requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan.type.not_blank}")
	private String type;
}
