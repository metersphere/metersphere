package io.metersphere.plan.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestPlanReportComponentSaveRequest {

	@Schema(title = "组件名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan_report_component.name.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 50, message = "{test_plan_report_component.name.length_range}", groups = {Created.class, Updated.class})
	private String name;

	@Schema(title = "组件标题", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan_report_component.label.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 255, message = "{test_plan_report_component.label.length_range}", groups = {Created.class, Updated.class})
	private String label;

	@Schema(title = "组件分类", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{test_plan_report_component.type.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 50, message = "{test_plan_report_component.type.length_range}", groups = {Created.class, Updated.class})
	private String type;

	@Schema(title = "组件内容")
	private String value;

	@Schema(title = "自定义排序，1开始整数递增", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "{test_plan_report_component.pos.not_blank}", groups = {Created.class})
	private Long pos;
}
