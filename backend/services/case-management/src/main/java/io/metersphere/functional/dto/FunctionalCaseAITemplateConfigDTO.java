package io.metersphere.functional.dto;

import lombok.Data;

@Data
public class FunctionalCaseAITemplateConfigDTO {

    //文本描述
    private Boolean textDescription;
    //步骤描述
    private Boolean stepDescription;
    //用例名称
    private Boolean caseName;
    //前置条件
    private Boolean preCondition;
    //用例步骤
    private Boolean caseSteps;
    //预期结果
    private Boolean expectedResult;
    //备注
    private Boolean remark;
}
