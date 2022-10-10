package io.metersphere.service;

import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.cache.JMeterEngineCache;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.commons.utils.FixedCapacityUtil;
import io.metersphere.utils.LoggerUtil;
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
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionExecResultMapper execResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    public void remake(JmeterRunRequestDTO request) {
        try {
            // 清理零时报告
            if (StringUtils.equalsAnyIgnoreCase(request.getRunMode(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                ApiDefinitionExecResultWithBLOBs result = execResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus(ApiReportStatus.ERROR.name());
                    result.setEndTime(System.currentTimeMillis());
                    execResultMapper.updateByPrimaryKeySelective(result);
                    TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(request.getTestId());
                    if (testPlanApiCase != null) {
                        testPlanApiCase.setStatus(ApiReportStatus.ERROR.name());
                        testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                        testPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
                    }
                }
            } else if (StringUtils.equals(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
                ApiDefinitionExecResultWithBLOBs result = execResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus(ApiReportStatus.ERROR.name());
                    result.setEndTime(System.currentTimeMillis());
                    execResultMapper.updateByPrimaryKeySelective(result);
                    ApiTestCaseWithBLOBs apiTestCase = apiTestCaseMapper.selectByPrimaryKey(request.getTestId());
                    if (apiTestCase != null) {
                        apiTestCase.setStatus(ApiReportStatus.ERROR.name());
                        apiTestCase.setUpdateTime(System.currentTimeMillis());
                        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
                        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
                        if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                            apiDefinitionWithBLOBs.setToBeUpdated(true);
                            apiDefinitionWithBLOBs.setToBeUpdateTime(System.currentTimeMillis());
                            apiDefinitionMapper.updateByPrimaryKey(apiDefinitionWithBLOBs);
                        }
                    }
                }
            } else if (StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                if (report != null) {
                    report.setEndTime(System.currentTimeMillis());
                    report.setStatus(ApiReportStatus.ERROR.name());
                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
                    if (testPlanApiScenario != null) {
                        testPlanApiScenario.setLastResult(ApiReportStatus.ERROR.name());
                        testPlanApiScenario.setPassRate("0%");
                        testPlanApiScenario.setReportId(report.getId());
                        testPlanApiScenario.setUpdateTime(report.getCreateTime());
                        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
                    }
                    apiScenarioReportMapper.updateByPrimaryKey(report);
                }
            } else if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                if (report != null) {
                    report.setEndTime(System.currentTimeMillis());
                    report.setStatus(ApiReportStatus.ERROR.name());
                    String planScenarioId = report.getScenarioId();
                    TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(planScenarioId);
                    if (testPlanApiScenario != null) {
                        report.setScenarioId(testPlanApiScenario.getApiScenarioId());
                        report.setEndTime(System.currentTimeMillis());

                        testPlanApiScenario.setLastResult(ApiReportStatus.ERROR.name());
                        testPlanApiScenario.setPassRate("0%");
                        testPlanApiScenario.setReportId(report.getId());
                        testPlanApiScenario.setUpdateTime(report.getCreateTime());
                        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
                    }
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
            } else {
                ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                if (report != null) {
                    report.setStatus(ApiReportStatus.ERROR.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
                if (StringUtils.isNotEmpty(request.getTestId())) {
                    ApiScenarioWithBLOBs scenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(request.getTestId());
                    if (scenarioWithBLOBs != null) {
                        scenarioWithBLOBs.setLastResult(ApiReportStatus.ERROR.name());
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

    public void remakeScenario(String runMode, String scenarioId, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiScenarioReportWithBLOBs report) {
        // 生成失败报告
        if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name())) {
            TestPlanApiScenario testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(scenarioId);
            if (testPlanApiScenario != null) {
                report.setScenarioId(scenarioWithBLOBs.getId());
                report.setEndTime(System.currentTimeMillis());

                testPlanApiScenario.setLastResult(ApiReportStatus.ERROR.name());
                testPlanApiScenario.setPassRate("0%");
                testPlanApiScenario.setReportId(report.getId());
                testPlanApiScenario.setUpdateTime(report.getCreateTime());
                testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
            }
        } else {
            scenarioWithBLOBs.setLastResult(ApiReportStatus.ERROR.name());
            scenarioWithBLOBs.setPassRate("0%");
            scenarioWithBLOBs.setReportId(report.getId());
            scenarioWithBLOBs.setExecuteTimes(1);
            apiScenarioMapper.updateByPrimaryKey(scenarioWithBLOBs);
        }
        report.setStatus(ApiReportStatus.ERROR.name());
        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
    }

    public void testEnded(JmeterRunRequestDTO request, String errorMsg) {
        try {
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, request);
            dto.setQueueId(request.getQueueId());
            dto.setTestId(request.getTestId());

            if (JMeterEngineCache.runningEngine.containsKey(dto.getReportId())) {
                JMeterEngineCache.runningEngine.remove(dto.getReportId());
            }
            LoggerUtil.info("进入异常结果处理：" + dto.getRunMode() + " 整体处理完成", dto.getReportId());
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            String consoleMsg = FixedCapacityUtil.getJmeterLogger(dto.getReportId(), true);
            dto.setConsole(consoleMsg + "\n" + errorMsg);
            // 整体执行结束更新资源状态
            CommonBeanFactory.getBean(TestResultService.class).testEnded(dto);

            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
            }
            // 更新测试计划报告
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                LoggerUtil.info("Check Processing Test Plan report status：" + dto.getQueueId() + "，" + dto.getTestId(), dto.getReportId());
                CommonBeanFactory.getBean(ApiExecutionQueueService.class).testPlanReportTestEnded(dto.getTestPlanReportId());
            }
        } catch (Exception e) {
            LoggerUtil.error("回退报告异常", request.getReportId(), e);
        }
    }
}
