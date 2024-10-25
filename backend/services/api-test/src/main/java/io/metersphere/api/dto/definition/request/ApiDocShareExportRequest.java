package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.ApiDefinitionBatchExportRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDocShareExportRequest extends ApiDefinitionBatchExportRequest {

	@Schema(description = "分享ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String shareId;

	@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String orgId;
}
