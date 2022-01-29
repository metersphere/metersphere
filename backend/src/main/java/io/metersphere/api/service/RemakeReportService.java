package io.metersphere.api.service;

import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RemakeReportService {
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionExecResultMapper execResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestCaseReviewApiCaseMapper testCaseReviewApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    public void remake(JmeterRunRequestDTO request) {
        try {
            // 清理零时报告
            if (StringUtils.equalsAnyIgnoreCase(request.getRunMode(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                ApiDefinitionExecResult result = execResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus("error");
                    result.setEndTime(System.currentTimeMillis());
                    execResultMapper.updateByPrimaryKeySelective(result);

                    TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(request.getTestId());
                    if (testPlanApiCase != null) {
                        testPlanApiCase.setStatus("error");
                        testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                        testPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
                    }
                    TestCaseReviewApiCase testCaseReviewApiCase = testCaseReviewApiCaseMapper.selectByPrimaryKey(request.getTestId());
                    if (testCaseReviewApiCase != null) {
                        testCaseReviewApiCase.setStatus("error");
                        testCaseReviewApiCase.setUpdateTime(System.currentTimeMillis());
                        testCaseReviewApiCaseMapper.updateByPrimaryKeySelective(testCaseReviewApiCase);
                    }
                }
            } else if (StringUtils.equals(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
                ApiDefinitionExecResult result = execResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus("error");
                    result.setEndTime(System.currentTimeMillis());
                    execResultMapper.updateByPrimaryKeySelective(result);
                    ApiTestCaseWithBLOBs apiTestCase = apiTestCaseMapper.selectByPrimaryKey(request.getTestId());
                    if (apiTestCase != null) {
                        apiTestCase.setStatus("error");
                        apiTestCase.setUpdateTime(System.currentTimeMillis());
                        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
                    }
                }
            } else if (StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
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
                    }
                    apiScenarioReportMapper.updateByPrimaryKey(report);
                }
            } else if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                if (report != null) {
                    report.setEndTime(System.currentTimeMillis());
                    report.setStatus(APITestStatus.Error.name());
                    String planScenarioId = report.getScenarioId();
                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(planScenarioId);
                    if (testPlanApiScenario != null) {
                        report.setScenarioId(testPlanApiScenario.getApiScenarioId());
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
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                if (report != null) {
                    report.setStatus(APITestStatus.Error.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
                if (StringUtils.isNotEmpty(request.getTestId())) {
                    ApiScenarioWithBLOBs scenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(request.getTestId());
                    if (scenarioWithBLOBs != null) {
                        scenarioWithBLOBs.setLastResult("Fail");
                        scenarioWithBLOBs.setPassRate("0%");
                        scenarioWithBLOBs.setReportId(report.getId());
                        scenarioWithBLOBs.setExecuteTimes(1);
                        apiScenarioMapper.updateByPrimaryKeySelective(scenarioWithBLOBs);
                    }
                }
            }
            // 处理队列
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, request);
            dto.setQueueId(request.getQueueId());
            dto.setTestId(request.getTestId());
            CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void remakeScenario(String runMode, String scenarioId, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiScenarioReport report) {
        // 生成失败报告
        if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name())) {
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(scenarioId);
            if (testPlanApiScenario != null) {
                report.setScenarioId(scenarioWithBLOBs.getId());
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
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
    }
}
