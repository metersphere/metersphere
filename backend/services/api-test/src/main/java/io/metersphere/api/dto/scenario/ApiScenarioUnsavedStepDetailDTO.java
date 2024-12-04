package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiScenarioUnsavedStepDetailDTO {

    @Schema(description = "步骤详情")
    private Object detail;

    @Schema(description = "复制的步骤中的本地文件ID集合")
    private List<String> uploadIds;
}
