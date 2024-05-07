package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class BaseAssociateCaseRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    /*@Schema(description = "类型：项目/测试计划/用例评审", allowableValues = {"PROJECT", "TEST_PLAN", "CASE_REVIEW"})
    private String associateType;

    @Schema(description = "类型id: 项目id/测试计划id/用例评审id")
    private String associateTypeId;

    @Schema(description = "用例类型: 功能用例/接口用例/接口场景用例", allowableValues = {"FUNCTIONAL", "API", "API_SCENARIO"})
    private List<String> associateCaseType;*/

    @Schema(description = "功能用例选中的ids")
    private List<String> functionalSelectIds;

    @Schema(description = "接口API用例选中的ids")
    private List<String> apiSelectIds;

    @Schema(description = "接口CASE选中的ids")
    private List<String> apiCaseSelectIds;

    @Schema(description = "接口场景用例选中的ids")
    private List<String> apiScenarioSelectIds;
}
