package io.metersphere.api.dto.automation;

import io.metersphere.commons.vo.RunPlanScenarioVO;
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

    private String projectId;

    private String triggerMode;

    private String executeType;

    private String runMode;

    private List<String> ids;

    private String reportUserID;

    private ApiScenarioRequest condition;

    private RunModeConfigDTO config;

    //生成测试报告：当isTestPlanScheduleJob为true时使用
    private String testPlanReportId;

    private String requestOriginator;

    // 失败重跑
    private boolean isRerun;
    private String serialReportId;
    private Map<String, ApiScenarioReportResult> reportMap;

    private String testPlanId;

    // 过程数据，整个测试计划执行
    private RunPlanScenarioVO processVO;
    // 测试计划批量执行传入id
    private List<String> planScenarioIds;
}
