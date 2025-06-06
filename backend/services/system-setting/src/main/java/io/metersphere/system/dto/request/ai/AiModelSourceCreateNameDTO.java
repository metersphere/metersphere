package io.metersphere.system.dto.request.ai;

import io.metersphere.system.domain.AiModelSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AiModelSourceCreateNameDTO extends AiModelSource {
    @Schema(description = "创建人名称")
    private String createUserName;
}
