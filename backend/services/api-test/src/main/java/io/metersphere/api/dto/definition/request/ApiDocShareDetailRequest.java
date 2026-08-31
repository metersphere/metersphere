package io.metersphere.api.dto.definition.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author metersphere
 */
@Data
public class ApiDocShareDetailRequest {

	@Schema(description = "接口定义ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_definition.id.not_blank}")
	@Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
	private String id;

	@Schema(description = "分享ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.id.not_blank}")
	@Size(min = 1, max = 50, message = "{api_doc_share.id.length_range}")
	private String shareId;

	@Schema(description = "分享密码")
	private String password;
}
