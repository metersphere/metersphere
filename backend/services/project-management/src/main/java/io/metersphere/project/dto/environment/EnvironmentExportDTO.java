package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentExportDTO {

    @Schema(description = "是否是全局参数")
    private Boolean globalParam = false;
    @Schema(description = "是否是环境")
    private Boolean environment = false;
    @Schema(description = "数据")
    private String data;

}
