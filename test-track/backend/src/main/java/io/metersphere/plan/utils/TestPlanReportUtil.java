package io.metersphere.plan.utils;

import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.ApiPlanReportDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.service.ServiceUtils;
import io.metersphere.xpack.track.dto.IssuesDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestPlanReportUtil {
    public static void initCaseExecuteInfoToDTO(TestPlanSimpleReportDTO testPlanReportDTO, TestPlanReportContentWithBLOBs testPlanReportContent) {
        testPlanReportDTO.setFunctionAllCases(
                getReportContentResultArray(testPlanReportContent.getFunctionAllCases(), TestPlanCaseDTO.class)
        );
        testPlanReportDTO.setIssueList(
                getReportContentResultArray(testPlanReportContent.getIssueList(), IssuesDao.class)
        );
        testPlanReportDTO.setFunctionResult(
                getReportContentResultObject(testPlanReportContent.getFunctionResult(), TestPlanFunctionResultReportDTO.class)
        );
        testPlanReportDTO.setApiResult(
                getReportContentResultObject(testPlanReportContent.getApiResult(), TestPlanApiResultReportDTO.class)
        );
        testPlanReportDTO.setLoadResult(
                getReportContentResultObject(testPlanReportContent.getLoadResult(), TestPlanLoadResultReportDTO.class)
        );
        testPlanReportDTO.setFunctionAllCases(
                getReportContentResultArray(testPlanReportContent.getFunctionAllCases(), TestPlanCaseDTO.class)
        );
        testPlanReportDTO.setIssueList(
                getReportContentResultArray(testPlanReportContent.getIssueList(), IssuesDao.class)
        );
        testPlanReportDTO.setApiAllCases(
                getReportContentResultArray(testPlanReportContent.getApiAllCases(), TestPlanApiDTO.class)
        );
        testPlanReportDTO.setApiFailureCases(
                getReportContentResultArray(testPlanReportContent.getApiFailureCases(), TestPlanApiDTO.class)
        );
        testPlanReportDTO.setScenarioAllCases(
                getReportContentResultArray(testPlanReportContent.getScenarioAllCases(), TestPlanScenarioDTO.class)
        );
        testPlanReportDTO.setScenarioFailureCases(
                getReportContentResultArray(testPlanReportContent.getScenarioFailureCases(), TestPlanScenarioDTO.class)
        );
        testPlanReportDTO.setLoadAllCases(
                getReportContentResultArray(testPlanReportContent.getLoadAllCases(), TestPlanLoadCaseDTO.class)
        );
        testPlanReportDTO.setLoadFailureCases(
                getReportContentResultArray(testPlanReportContent.getLoadFailureCases(), TestPlanLoadCaseDTO.class)
        );
        testPlanReportDTO.setErrorReportCases(
                getReportContentResultArray(testPlanReportContent.getErrorReportCases(), TestPlanApiDTO.class)
        );
        testPlanReportDTO.setErrorReportScenarios(
                getReportContentResultArray(testPlanReportContent.getErrorReportScenarios(), TestPlanScenarioDTO.class)
        );
        testPlanReportDTO.setUnExecuteCases(
                getReportContentResultArray(testPlanReportContent.getUnExecuteCases(), TestPlanApiDTO.class)
        );
        testPlanReportDTO.setUnExecuteScenarios(
                getReportContentResultArray(testPlanReportContent.getUnExecuteScenarios(), TestPlanScenarioDTO.class)
        );
        testPlanReportDTO.setUiResult(
                getReportContentResultObject(testPlanReportContent.getUiResult(), TestPlanUiResultReportDTO.class)
        );
        testPlanReportDTO.setUiAllCases(
                getReportContentResultArray(testPlanReportContent.getUiAllCases(), TestPlanUiScenarioDTO.class)
        );
        testPlanReportDTO.setUiFailureCases(
                getReportContentResultArray(testPlanReportContent.getUiFailureCases(), TestPlanUiScenarioDTO.class)
        );
    }

    public static boolean checkReportConfig(Map config, String key, String subKey) {
        return ServiceUtils.checkConfigEnable(config, key, subKey);
    }

    public static void screenApiCaseByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanApiDTO> apiAllCases, Map reportConfig) {
        if (!CollectionUtils.isEmpty(apiAllCases)) {
            List<TestPlanApiDTO> apiFailureCases = new ArrayList<>();
            List<TestPlanApiDTO> apiErrorReportCases = new ArrayList<>();
            List<TestPlanApiDTO> apiUnExecuteCases = new ArrayList<>();
            for (TestPlanApiDTO apiDTO : apiAllCases) {
                if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.ERROR.name())) {
                    apiFailureCases.add(apiDTO);
                } else if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    apiErrorReportCases.add(apiDTO);
                } else if (StringUtils.equalsAnyIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    apiUnExecuteCases.add(apiDTO);
                }
            }

            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setApiFailureCases(apiFailureCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportCases(apiErrorReportCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteCases(apiUnExecuteCases);
            }
        }
    }

    public static void screenScenariosByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanScenarioDTO> scenarios, Map reportConfig) {
        if (!CollectionUtils.isEmpty(scenarios)) {
            List<TestPlanScenarioDTO> failureScenarios = new ArrayList<>();
            List<TestPlanScenarioDTO> errorReportScenarios = new ArrayList<>();
            List<TestPlanScenarioDTO> unExecuteScenarios = new ArrayList<>();
            for (TestPlanScenarioDTO scenario : scenarios) {
                if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.ERROR.name())) {
                    failureScenarios.add(scenario);
                } else if (StringUtils.equalsIgnoreCase(scenario.getLastResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    errorReportScenarios.add(scenario);
                } else if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    unExecuteScenarios.add(scenario);
                }
            }
            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setScenarioFailureCases(failureScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportScenarios(errorReportScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteScenarios(unExecuteScenarios);
            }
        }
    }

    private static <T> T getReportContentResultObject(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseObject(contentStr, clazz);
        }
        return null;
    }

    private static <T> List<T> getReportContentResultArray(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseArray(contentStr, clazz);
        }
        return null;
    }
}
