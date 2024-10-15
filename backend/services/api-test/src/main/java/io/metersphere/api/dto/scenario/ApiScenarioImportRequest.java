package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiScenarioImportRequest {

    @Schema(description = "导入的模块id")
    private String moduleId;

    @Schema(description = "导入的项目id")
    @NotBlank
    private String projectId;

    @Schema(description = "导入的类型  METERSPHERE/JMETER")
    @NotBlank
    private String type;

    @Schema(description = "是否覆盖数据")
    private boolean coverData;

    @Schema(description = "操作人")
    private String operator;
}
