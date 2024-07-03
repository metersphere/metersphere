package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 */
@Data
public class TestPlanCollectionAssociateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否选择所有模块")
    private boolean selectAllModule;

    @Schema(description = "模块下的id集合属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, ModuleSelectDTO>> moduleMaps;

    @Schema(description = "关联关系的type(功能：FUNCTIONAL/接口定义：API/接口用例：API_CASE/场景：API_SCENARIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String associateType;

    @Schema(description = "项目id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "是否同步添加功能用例的关联用例")
    private boolean syncCase = false;

    @Schema(description = "接口计划集id")
    private String apiCaseCollectionId;

    @Schema(description = "场景计划集id")
    private String apiScenarioCollectionId;


}
