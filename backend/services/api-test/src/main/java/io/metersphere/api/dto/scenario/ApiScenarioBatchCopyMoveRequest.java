package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchCopyMoveRequest extends ApiScenarioBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "复制的目标模块ID")
    @NotBlank
    @Size(max = 50, message = "{api_scenario.target_module_id.length_range}")
    private String targetModuleId;

}
