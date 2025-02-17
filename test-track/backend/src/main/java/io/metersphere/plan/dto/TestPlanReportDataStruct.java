package io.metersphere.plan.dto;


import io.metersphere.base.domain.TestPlanReportContent;
import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.JSON;
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
    private String status;
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

    public TestPlanReportDataStruct() {
    }

    public TestPlanReportDataStruct(TestPlanReportContentWithBLOBs testPlanReportContent) {
        if (testPlanReportContent == null) {
            return;
        }
        this.setExecuteRate(testPlanReportContent.getExecuteRate());
        this.setPassRate(testPlanReportContent.getPassRate());
        if (StringUtils.isNotEmpty(testPlanReportContent.getSummary())) {
            this.setSummary(testPlanReportContent.getSummary());
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getConfig())) {
            this.setConfig(testPlanReportContent.getConfig());
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getFunctionResult())) {
            TestPlanFunctionResultReportDTO dto = this.parseJsonStringToObj(testPlanReportContent.getFunctionResult(), TestPlanFunctionResultReportDTO.class);
            this.setFunctionResult(dto);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getApiResult())) {
            TestPlanApiResultReportDTO dto = this.parseJsonStringToObj(testPlanReportContent.getApiResult(), TestPlanApiResultReportDTO.class);
            this.setApiResult(dto);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getLoadResult())) {
            TestPlanLoadResultReportDTO dto = this.parseJsonStringToObj(testPlanReportContent.getLoadResult(), TestPlanLoadResultReportDTO.class);
            this.setLoadResult(dto);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getUiResult())) {
            TestPlanUiResultReportDTO dto = this.parseJsonStringToObj(testPlanReportContent.getUiResult(), TestPlanUiResultReportDTO.class);
            this.setUiResult(dto);
        }

        if (StringUtils.isNotEmpty(testPlanReportContent.getFunctionAllCases())) {
            List<TestPlanCaseDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getFunctionAllCases(), TestPlanCaseDTO.class);
            this.setFunctionAllCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getIssueList())) {
            List<IssuesDao> dtoList = this.parseJsonStringToArray(testPlanReportContent.getIssueList(), IssuesDao.class);
            this.setIssueList(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getLoadAllCases())) {
            List<TestPlanLoadCaseDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getLoadAllCases(), TestPlanLoadCaseDTO.class);
            this.setLoadAllCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getLoadFailureCases())) {
            List<TestPlanLoadCaseDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getLoadFailureCases(), TestPlanLoadCaseDTO.class);
            this.setLoadFailureCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getApiAllCases())) {
            List<TestPlanApiDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getApiAllCases(), TestPlanApiDTO.class);
            this.setApiAllCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getErrorReportCases())) {
            List<TestPlanApiDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getErrorReportCases(), TestPlanApiDTO.class);
            this.setErrorReportCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getApiFailureCases())) {
            List<TestPlanApiDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getApiFailureCases(), TestPlanApiDTO.class);
            this.setApiFailureCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getUnExecuteCases())) {
            List<TestPlanApiDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getUnExecuteCases(), TestPlanApiDTO.class);
            this.setUnExecuteCases(dtoList);
        }

        if (StringUtils.isNotEmpty(testPlanReportContent.getScenarioAllCases())) {
            List<TestPlanScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getScenarioAllCases(), TestPlanScenarioDTO.class);
            this.setScenarioAllCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getErrorReportScenarios())) {
            List<TestPlanScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getErrorReportScenarios(), TestPlanScenarioDTO.class);
            this.setErrorReportScenarios(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getScenarioFailureCases())) {
            List<TestPlanScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getScenarioFailureCases(), TestPlanScenarioDTO.class);
            this.setScenarioFailureCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getUnExecuteScenarios())) {
            List<TestPlanScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getUnExecuteScenarios(), TestPlanScenarioDTO.class);
            this.setUnExecuteScenarios(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getUiAllCases())) {
            List<TestPlanUiScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getUiAllCases(), TestPlanUiScenarioDTO.class);
            this.setUiAllCases(dtoList);
        }
        if (StringUtils.isNotEmpty(testPlanReportContent.getUiFailureCases())) {
            List<TestPlanUiScenarioDTO> dtoList = this.parseJsonStringToArray(testPlanReportContent.getUiFailureCases(), TestPlanUiScenarioDTO.class);
            this.setUiFailureCases(dtoList);
        }
    }

    private <T> T parseJsonStringToObj(String jsonString, Class<T> valueType) {
        try {
            return JSON.parseObject(jsonString, valueType);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> List<T> parseJsonStringToArray(String jsonString, Class<T> valueType) {
        try {
            return JSON.parseArray(jsonString, valueType);
        } catch (Exception e) {
            return null;
        }
    }
}
