package io.metersphere.api.dto.definition;

import io.metersphere.project.dto.environment.http.SelectModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiModuleDTO extends SelectModule {
    @Schema(description = "是否禁用")
    private Boolean disabled = false;
}
