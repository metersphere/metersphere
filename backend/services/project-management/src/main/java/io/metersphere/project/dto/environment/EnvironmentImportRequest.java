package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentImportRequest {

    @Schema(description = "是否覆盖数据")
    private Boolean cover = false;

}
