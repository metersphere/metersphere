package io.metersphere.functional.dto;

import io.metersphere.project.dto.ModuleCountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseModuleCountDTO extends ModuleCountDTO {
    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "项目名称")
    private String projectName;


}
