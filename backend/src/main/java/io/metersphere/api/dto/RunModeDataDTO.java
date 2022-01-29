package io.metersphere.api.dto;

import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RunModeDataDTO {
    // 执行HashTree
    private ApiScenarioWithBLOBs scenario;
    // 测试场景/测试用例
    private String testId;

    private String reportId;
    // 初始化报告
    private APIScenarioReportResult report;
    //
    private String apiCaseId;

    private Map<String, String> planEnvMap;

    public RunModeDataDTO() {

    }

    public RunModeDataDTO(APIScenarioReportResult report, String testId) {
        this.report = report;
        this.testId = testId;
    }
}
