package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模块", allowableValues = {"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"})
    private String module;

    @Schema(description = "是否启用", type = "boolean")
    private Boolean moduleEnable;

    public ModuleDTO(String module, Boolean moduleEnable) {
        this.module = module;
        this.moduleEnable = moduleEnable;
    }
}
