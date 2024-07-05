package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanReportManualRequest extends TestPlanReportGenRequest{

	@Schema(description = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String reportName;

	@Schema(description = "报告组件集合")
	private List<TestPlanReportComponentSaveRequest> components;

	@Schema(description = "富文本组件临时生成的文件ID(图片)")
	private List<String> richTextTmpFileIds;
}
