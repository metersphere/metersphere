package io.metersphere.plan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseExecResult {
    private String id;
    private String execResult;

    //用例类型、对应执行的reportId
    private String caseType;
    private String reportId;
    private String caseName;
    private String testCaseId;
}
