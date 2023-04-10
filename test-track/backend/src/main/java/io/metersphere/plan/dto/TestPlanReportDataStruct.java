package io.metersphere.plan.dto;


import io.metersphere.base.domain.TestPlanReportContent;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.xpack.track.dto.IssuesDao;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;


@Getter
@Setter
public class TestPlanReportDataStruct extends TestPlanReportContent {
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
    private String resourcePool;

    private List<String> resourcePools;
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

    List<TestPlanApiDTO> apiAllCases;
    List<TestPlanApiDTO> errorReportCases;
    List<TestPlanApiDTO> apiFailureCases;
    List<TestPlanApiDTO> unExecuteCases;

    List<TestPlanScenarioDTO> scenarioAllCases;
    List<TestPlanScenarioDTO> errorReportScenarios;
    List<TestPlanScenarioDTO> scenarioFailureCases;
    List<TestPlanScenarioDTO> unExecuteScenarios;

    List<TestPlanUiScenarioDTO> uiAllCases;
    List<TestPlanUiScenarioDTO> uiFailureCases;

    public boolean hasRunningCase() {
        //判断是否含有运行中的用例。  方法内针对集合不开流是为了不影响后续业务中使用stream
        if (CollectionUtils.isNotEmpty(apiAllCases)) {
            List<TestPlanApiDTO> runningCaseList =
                    apiAllCases.stream().filter(
                            dto -> StringUtils.equalsAnyIgnoreCase(dto.getExecResult(),
                                    ApiReportStatus.RERUNNING.name(),
                                    ApiReportStatus.RUNNING.name())).toList();
            if (runningCaseList.size() > 0) {
                return true;
            }
        }
        if (CollectionUtils.isNotEmpty(scenarioAllCases)) {
            List<TestPlanScenarioDTO> runningCaseList =
                    scenarioAllCases.stream().filter(
                            dto -> StringUtils.equalsAnyIgnoreCase(dto.getLastResult(),
                                    ApiReportStatus.RERUNNING.name(),
                                    ApiReportStatus.RUNNING.name())).toList();
            if (runningCaseList.size() > 0) {
                return true;
            }
        }
        if (CollectionUtils.isNotEmpty(loadAllCases)) {
            List<TestPlanLoadCaseDTO> runningCaseList =
                    loadAllCases.stream().filter(
                            dto -> StringUtils.equalsAnyIgnoreCase(dto.getStatus(),
                                    PerformanceTestStatus.Reporting.name())).toList();
            if (runningCaseList.size() > 0) {
                return true;
            }
        }
        if (CollectionUtils.isNotEmpty(uiAllCases)) {
            List<TestPlanUiScenarioDTO> runningCaseList =
                    uiAllCases.stream().filter(
                            dto -> StringUtils.equalsAnyIgnoreCase(dto.getLastResult(),
                                    ApiReportStatus.RERUNNING.name(),
                                    ApiReportStatus.RUNNING.name())).toList();
            if (runningCaseList.size() > 0) {
                return true;
            }
        }
        return false;
    }
}
