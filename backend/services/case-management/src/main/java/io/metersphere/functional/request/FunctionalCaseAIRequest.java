package io.metersphere.functional.request;

import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
public class FunctionalCaseAIRequest extends AIChatRequest implements Serializable {

	@Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String configId;
}
