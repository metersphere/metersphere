package io.metersphere.system.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class BatchExecTaskPageRequest extends BasePageRequest {

	@Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String taskId;
	@Schema(description = "批量任务类型", requiredMode = Schema.RequiredMode.REQUIRED,
			allowableValues = {"API_CASE_BATCH", "API_SCENARIO_BATCH", "TEST_PLAN_API_CASE_BATCH", "TEST_PLAN_API_SCENARIO_BATCH"})
	private String batchType;
}
