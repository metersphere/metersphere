package io.metersphere.base.domain;

import lombok.Data;

/**
 * 统一对模块的操作
 */
@Data
public class ModuleNode extends TestCaseNode {
    private Integer caseNum;
    private String protocol;
    private String modulePath;
    private String scenarioType;
}
