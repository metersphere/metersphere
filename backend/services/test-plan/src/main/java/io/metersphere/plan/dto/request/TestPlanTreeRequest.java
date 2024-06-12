package io.metersphere.plan.dto.request;

import io.metersphere.plan.constants.TreeTypeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class TestPlanTreeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "类型：模块/计划集", allowableValues = {"MODULE","COLLECTION"},requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}")
    private String treeType = TreeTypeEnums.COLLECTION;

}
