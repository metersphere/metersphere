package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanReportDetailEditRequest {

	@Schema(description = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan_report_id.not_blank}")
	private String id;

	@Schema(description = "组件ID; {默认布局时使用报告总结枚举值作为ID}")
	private String componentId;

	@Schema(description = "报告总结")
	private String componentValue;

	@Schema(description = "富文本临时文件ID(图片)")
	private List<String> richTextTmpFileIds;
}
