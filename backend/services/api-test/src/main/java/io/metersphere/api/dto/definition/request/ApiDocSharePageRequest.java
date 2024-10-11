package io.metersphere.api.dto.definition.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiDocSharePageRequest extends BasePageRequest {

	@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.project_id.not_blank}")
	@Size(min = 1, max = 50, message = "{api_doc_share.project_id.length_range}")
	private String projectId;
}
