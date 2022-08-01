package io.metersphere.track.dto;

import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.TestPlanReportContent;
import io.metersphere.dto.TestPlanUiScenarioDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
public class TestPlanSimpleReportDTO extends TestPlanReportContent {
    private String name;

    private int executeCount;
    private int passCount;
    private String summary;
    private String config;

    /**
     * 运行环境信息
     * runMode:运行模式  并行/串行
     * envGroupName： 用户组名称
     * projectEnvMap: <项目,运行环境>
     */
    private String runMode;
    private String envGroupName;
    private Map<String, List<String>> projectEnvMap;

    /**
     * 导出保存国际化
     */
    private String lang;

    private TestPlanFunctionResultReportDTO functionResult;
    private TestPlanApiResultReportDTO apiResult;
    private TestPlanLoadResultReportDTO loadResult;
    private TestPlanUiResultReportDTO uiResult;

    List<TestPlanCaseDTO> functionAllCases;
    List<IssuesDao> issueList;

    List<TestPlanLoadCaseDTO> loadAllCases;
    List<TestPlanLoadCaseDTO> loadFailureCases;

    List<TestPlanFailureApiDTO> apiAllCases;
    List<TestPlanFailureApiDTO> errorReportCases;
    List<TestPlanFailureApiDTO> apiFailureCases;
    List<TestPlanFailureApiDTO> unExecuteCases;

    List<TestPlanFailureScenarioDTO> scenarioAllCases;
    List<TestPlanFailureScenarioDTO> errorReportScenarios;
    List<TestPlanFailureScenarioDTO> scenarioFailureCases;
    List<TestPlanFailureScenarioDTO> unExecuteScenarios;

    List<TestPlanUiScenarioDTO> uiAllCases;
    List<TestPlanUiScenarioDTO> uiFailureCases;
}
