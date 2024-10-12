package io.metersphere.api.dto.definition.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */

@Data
public class ApiDocShareCheckRequest implements Serializable {

	@Schema(description = "分享ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String docShareId;
	@Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
	private String password;
}
