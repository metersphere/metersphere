package io.metersphere.plan.request.api;

import io.metersphere.dto.RunModeConfigDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RunScenarioRequest {
    private String id;

    private String reportId;

    private String environmentId;

    private String projectId;

    private String triggerMode;

    private String executeType;

    private String runMode;

    /**
     * 测试情景和测试计划的关联ID
     */
    private String planScenarioId;

    private List<String> planCaseIds;

    private List<String> ids;

    private String reportUserID;

    private Map<String, String> scenarioTestPlanIdMap;

//    private ApiScenarioRequest condition;

    private RunModeConfigDTO config;

    private boolean isTestPlanScheduleJob = false;

    //生成测试报告：当isTestPlanScheduleJob为true时使用
    private String testPlanReportId;

    private String requestOriginator;

    // 失败重跑
    private boolean isRerun;
    private String serialReportId;
//    private Map<String, ApiScenarioReportResult> reportMap;

    private String testPlanId;
}

