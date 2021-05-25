package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SchedulePlanScenarioExecuteRequest {

    private String id;

    private String reportId;

    private String projectId;

    private String environmentId;

    private String triggerMode;

    private String executeType;

    private String runMode;

    //测试情景和测试计划的关联ID
    private String testPlanID;

    private List<String> planCaseIds;

    private String reportUserID;

    //key: test_plan.id, value: test_plan_api_scenario <->scenarioValue
    private Map<String,Map<String,String>> testPlanScenarioIDMap;

    private String testPlanReportId;

    private RunModeConfig config;
}
