package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestPlanReportContentWithBLOBs extends TestPlanReportContent implements Serializable {
    private String config;

    private String summary;

    private String functionResult;

    private String apiResult;

    private String loadResult;

    private String functionAllCases;

    private String functionFailureCases;

    private String issueList;

    private String apiAllCases;

    private String apiFailureCases;

    private String scenarioAllCases;

    private String scenarioFailureCases;

    private String loadAllCases;

    private String loadFailureCases;

    private String planScenarioReportStruct;

    private String planApiCaseReportStruct;

    private String planLoadCaseReportStruct;

    private String errorReportCases;

    private String errorReportScenarios;

    private String unExecuteCases;

    private String unExecuteScenarios;

    private static final long serialVersionUID = 1L;
}