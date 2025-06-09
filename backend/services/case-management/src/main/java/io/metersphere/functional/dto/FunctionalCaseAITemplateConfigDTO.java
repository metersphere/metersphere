package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAITemplateConfigDTO {
    /**
     * 编辑模式：步骤模式/文本模式
     */
    @Schema(description = "编辑模式：步骤模式：STEP/文本模式：TEXT")
    private String caseEditType;
    //用例名称
    @Schema(description = "用例名称")
    private Boolean caseName;
    //前置条件
    @Schema(description = "前置条件")
    private Boolean preCondition;
    //用例步骤
    @Schema(description = "用例步骤")
    private Boolean caseSteps;
    //预期结果
    @Schema(description = "预期结果")
    private Boolean expectedResult;
    //备注
    @Schema(description = "备注")
    private Boolean remark;

    public FunctionalCaseAITemplateConfigDTO() {
    }

    public FunctionalCaseAITemplateConfigDTO(String caseEditType, Boolean caseName, Boolean preCondition, Boolean caseSteps, Boolean expectedResult, Boolean remark) {
        this.caseEditType = caseEditType;
        this.caseName = caseName;
        this.preCondition = preCondition;
        this.caseSteps = caseSteps;
        this.expectedResult = expectedResult;
        this.remark = remark;
    }
}
