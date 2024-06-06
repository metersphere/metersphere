package io.metersphere.plan.dto.request;

import io.metersphere.plan.constants.TreeTypeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanApiCaseModuleRequest extends TestPlanApiCaseRequest{

    @Schema(description = "类型：模块/计划集", allowableValues = {"MODULE","COLLECTION"},requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}")
    private String treeType = TreeTypeEnums.COLLECTION;
}
