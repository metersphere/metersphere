package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanCollectionInitDTO {

	@Schema(description = "测试集节点ID")
	private String id;

	@Schema(description = "所属测试集类型ID, 类型ID空时传递具体测试集类型名称即可", requiredMode = Schema.RequiredMode.REQUIRED)
	private String testCollectionTypeId;

	@Schema(description = "测试集名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "执行方式", allowableValues = {"SERIAL-串行", "PARALLEL-并行"}, requiredMode = Schema.RequiredMode.REQUIRED)
	private String executeMethod;

	@Schema(description = "是否使用环境组", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean grouped;

	@Schema(description = "环境ID/环境组ID, 根据是否环境组来传递相应的值", requiredMode = Schema.RequiredMode.REQUIRED)
	private String environmentId;

	@Schema(description = "位置, 从1开始递增的整数即可", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long pos;
}
