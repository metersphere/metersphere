package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RunScenarioRequest extends ApiScenarioWithBLOBs {

    private String reportId;

    private String environmentId;

    private String triggerMode;

    private String executeType;

    private String runMode;

    /**测试情景和测试计划的关联ID*/
    private String planScenarioId;

    private List<String> planCaseIds;

    private List<String> ids;

    private String reportUserID;

    private Map<String, String> scenarioTestPlanIdMap;

    private ApiScenarioRequest condition;

    private RunModeConfig config;

    private boolean isTestPlanScheduleJob = false;
    //生成测试报告：当isTestPlanScheduleJob为ture时使用
    private String testPlanReportId;
}
