package io.metersphere.api.service;

import io.metersphere.api.dto.RunRequest;
import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class RemakeReportService {
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionExecResultMapper execResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestCaseReviewApiCaseMapper testCaseReviewApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    public void remake(RunRequest runRequest, RunModeConfig config, String reportId) {
        if (MessageCache.cache.get(config.getAmassReport()) != null
                && MessageCache.cache.get(config.getAmassReport()).getReportIds() != null) {
            MessageCache.cache.get(config.getAmassReport()).getReportIds().remove(reportId);
        }
        // 重置报告状态
        if (StringUtils.isNotEmpty(reportId)) {
            // 清理零时报告
            if (StringUtils.equalsAnyIgnoreCase(runRequest.getRunMode(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                ApiDefinitionExecResult result = execResultMapper.selectByPrimaryKey(reportId);
                if (result != null) {
                    result.setStatus("error");
                    result.setEndTime(System.currentTimeMillis());
                    execResultMapper.updateByPrimaryKeySelective(result);

                    TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(runRequest.getTestId());
                    if (testPlanApiCase != null) {
                        testPlanApiCase.setStatus("error");
                        testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                        testPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
                    }
                    TestCaseReviewApiCase testCaseReviewApiCase = testCaseReviewApiCaseMapper.selectByPrimaryKey(runRequest.getTestId());
                    if (testCaseReviewApiCase != null) {
                        testCaseReviewApiCase.setStatus("error");
                        testCaseReviewApiCase.setUpdateTime(System.currentTimeMillis());
                        testCaseReviewApiCaseMapper.updateByPrimaryKeySelective(testCaseReviewApiCase);
                    }
                }
            } else if (StringUtils.equals(runRequest.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
                if (report != null) {
                    report.setEndTime(System.currentTimeMillis());
                    report.setStatus(APITestStatus.Error.name());

                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
                    if (testPlanApiScenario != null) {
                        testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                        testPlanApiScenario.setPassRate("0%");
                        testPlanApiScenario.setReportId(report.getId());
                        testPlanApiScenario.setUpdateTime(report.getCreateTime());
                        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);

                        report.setTestPlanScenarioId(testPlanApiScenario.getId());
                    }
                    apiScenarioReportMapper.updateByPrimaryKey(report);
                }
            } else if (StringUtils.equalsAny(runRequest.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
                if (report != null) {
                    report.setEndTime(System.currentTimeMillis());
                    report.setStatus(APITestStatus.Error.name());
                    String planScenarioId = report.getScenarioId();
                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(planScenarioId);
                    if (testPlanApiScenario != null) {
                        report.setScenarioId(testPlanApiScenario.getApiScenarioId());
                        report.setTestPlanScenarioId(planScenarioId);
                        report.setEndTime(System.currentTimeMillis());

                        testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                        testPlanApiScenario.setPassRate("0%");
                        testPlanApiScenario.setReportId(report.getId());
                        testPlanApiScenario.setUpdateTime(report.getCreateTime());
                        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
                    }
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
            } else {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(reportId);
                if (report != null) {
                    report.setStatus(APITestStatus.Error.name());
                    apiScenarioReportMapper.updateByPrimaryKey(report);
                }
                if (StringUtils.isNotEmpty(runRequest.getTestId())) {
                    ApiScenarioWithBLOBs scenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(runRequest.getTestId());
                    if (scenarioWithBLOBs != null) {
                        scenarioWithBLOBs.setLastResult("Fail");
                        scenarioWithBLOBs.setPassRate("0%");
                        scenarioWithBLOBs.setReportId(report.getId());
                        scenarioWithBLOBs.setExecuteTimes(1);
                        apiScenarioMapper.updateByPrimaryKey(scenarioWithBLOBs);
                    }
                }
            }
            MessageCache.batchTestCases.remove(reportId);
            MessageCache.executionQueue.remove(reportId);
        }
    }

    public void remakeScenario(String runMode, String testId, RunModeConfig config, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiScenarioReport report) {
        if (MessageCache.cache.get(config.getAmassReport()) != null
                && MessageCache.cache.get(config.getAmassReport()).getReportIds() != null) {
            MessageCache.cache.get(config.getAmassReport()).getReportIds().remove(report.getId());
        }
        // 生成失败报告
        if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name())) {
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testId);
            if (testPlanApiScenario != null) {
                report.setScenarioId(scenarioWithBLOBs.getId());
                report.setTestPlanScenarioId(testId);
                report.setEndTime(System.currentTimeMillis());

                testPlanApiScenario.setLastResult(ScenarioStatus.Fail.name());
                testPlanApiScenario.setPassRate("0%");
                testPlanApiScenario.setReportId(report.getId());
                testPlanApiScenario.setUpdateTime(report.getCreateTime());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
            }
        } else {
            scenarioWithBLOBs.setLastResult("Fail");
            scenarioWithBLOBs.setPassRate("0%");
            scenarioWithBLOBs.setReportId(report.getId());
            scenarioWithBLOBs.setExecuteTimes(1);
            apiScenarioMapper.updateByPrimaryKey(scenarioWithBLOBs);
        }
        report.setStatus(APITestStatus.Error.name());
        apiScenarioReportMapper.insert(report);
        MessageCache.batchTestCases.remove(report.getId());
        MessageCache.executionQueue.remove(report.getId());
    }
}
