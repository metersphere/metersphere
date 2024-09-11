package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanReportDetailCollectionResponse {

	@Schema(description = "测试集ID")
	private String id;
	@Schema(description = "测试集名称")
	private String name;
	@Schema(description = "测试集用例数量")
	private Long count;
	@Schema(description = "计划名称")
	private String planName;
}
