package io.metersphere.plan.dto.request;

import io.metersphere.api.dto.ApiRunModeRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanApiScenarioBatchRunRequest extends BasePlanCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @Schema(description = "运行模式配置")
    private ApiRunModeRequest runModeConfig;
}
