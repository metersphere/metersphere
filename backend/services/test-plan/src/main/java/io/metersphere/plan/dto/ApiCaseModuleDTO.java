package io.metersphere.plan.dto;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class ApiCaseModuleDTO extends ApiDefinitionModule {

    @Schema(description = "项目名称")
    private String projectName;
}
