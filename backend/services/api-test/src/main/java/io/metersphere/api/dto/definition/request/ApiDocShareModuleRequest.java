package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class ApiDocShareModuleRequest extends ApiModuleRequest {

	@Schema(description = "分享ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.id.not_blank}")
	@Size(min = 1, max = 50, message = "{api_doc_share.id.length_range}")
	private String shareId;

	@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String orgId;
}
