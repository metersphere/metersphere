package io.metersphere.plan.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestPlanReportShareRequest {

	@Schema(description = "分享类型 资源的类型 Single, Batch, API_SHARE_REPORT, TEST_PLAN_SHARE_REPORT")
	private String shareType;

	@Schema(description = "语言")
	private String lang;

	@Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{share_info.project_id.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 50, message = "{share_info.project_id.length_range}", groups = {Created.class, Updated.class})
	private String projectId;

	@Schema(description = "分享扩展数据 资源的ID" ,requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{share_info.project_id.not_blank}")
	private String reportId;
}
