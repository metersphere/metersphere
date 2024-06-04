package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanAllocationTypeDTO {

	@Schema(description = "测试集类型ID")
	private String id;

	@Schema(description = "测试集名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "测试集类型", allowableValues = {"FUNCTIONAL", "API", "SCENARIO"}, requiredMode = Schema.RequiredMode.REQUIRED)
	private String type;

	@Schema(description = "执行方式", allowableValues = {"SERIAL-串行", "PARALLEL-并行"}, requiredMode = Schema.RequiredMode.REQUIRED)
	private String executeMethod;

	@Schema(description = "位置, 从1开始递增的整数即可", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long pos;

}
