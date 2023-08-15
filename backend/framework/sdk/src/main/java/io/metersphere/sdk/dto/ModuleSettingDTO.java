package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ModuleSettingDTO {

    @Schema(description =  "接口测试")
    private boolean apiTest;
    @Schema(description =  "性能测试")
    private boolean loadTest;
    @Schema(description =  "UI测试")
    private boolean uiTest;
    @Schema(description =  "测试计划")
    private boolean testPlan;
    @Schema(description =  "工作台")
    private boolean workstation;
    @Schema(description =  "缺陷管理")
    private boolean bugManagement;
    @Schema(description =  "功能测试")
    private boolean caseManagement;
}
