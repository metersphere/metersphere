package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ModuleSettingDTO {

    @Schema(description =  "接口测试")
    private Boolean apiTest;
    @Schema(description =  "性能测试")
    private Boolean loadTest;
    @Schema(description =  "UI测试")
    private Boolean uiTest;
    @Schema(description =  "测试计划")
    private Boolean testPlan;
    @Schema(description =  "工作台")
    private Boolean workstation;
    @Schema(description =  "缺陷管理")
    private Boolean bugManagement;
    @Schema(description =  "功能测试")
    private Boolean caseManagement;
}
