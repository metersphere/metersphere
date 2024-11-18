package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanReportDetailPageRequest extends BasePageRequest {

	@Schema(description = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan.report_id.not_blank}")
	private String reportId;

	@Schema(description = "是否分页", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Boolean startPager = true;

	@Schema(description = "测试集ID")
	private String collectionId;

	@Schema(description = "报告ID集合")
	private List<String> detailReportIds;
}
