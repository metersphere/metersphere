package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchCopyRequest extends ApiScenarioBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "复制的目标模块ID")
    @NotBlank
    private String targetModuleId;

}
