package io.metersphere.api.dto;

import io.metersphere.api.dto.automation.APIScenarioReportResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.jorphan.collections.HashTree;

@Getter
@Setter
public class RunModeDataDTO {
    // 执行HashTree
    private HashTree hashTree;
    // 测试场景/测试用例
    private String testId;
    // 初始化报告
    private APIScenarioReportResult report;
    //
    private String apiCaseId;

    public RunModeDataDTO(String testId, String apiCaseId) {
        this.testId = testId;
        this.apiCaseId = apiCaseId;
    }

    public RunModeDataDTO(HashTree hashTree, String apiCaseId) {
        this.hashTree = hashTree;
        this.apiCaseId = apiCaseId;
    }

    public RunModeDataDTO(String testId, APIScenarioReportResult report) {
        this.testId = testId;
        this.report = report;
    }

    public RunModeDataDTO(HashTree hashTree, APIScenarioReportResult report) {
        this.hashTree = hashTree;
        this.report = report;
    }
}
