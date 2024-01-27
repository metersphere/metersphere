package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseModuleDTO extends FunctionalCaseModule {
    @Schema(description = "项目名称")
    private String projectName;
}
