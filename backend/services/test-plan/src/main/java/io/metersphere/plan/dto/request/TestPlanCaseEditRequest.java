package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class TestPlanCaseEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{id.not_blank}")
    private String id;

    @Schema(description = "最终执行结果")
    @NotBlank(message = "{test_plan.last_exec_result.not_blank}")
    private String lastExecResult;

}
