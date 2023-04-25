package io.metersphere.plan.service;

import io.metersphere.base.domain.TestPlanWithBLOBs;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.plan.dto.ExecutionWay;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.service.remote.api.PlanApiAutomationService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.performance.PerfExecService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.service.remote.ui.PlanUiAutomationService;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TestPlanExecuteService {
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private PlanTestPlanApiCaseService planTestPlanApiCaseService;
    @Resource
    private PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;
    @Resource
    private PerfExecService perfExecService;
    @Resource
    private PlanTestPlanUiScenarioCaseService planTestPlanUiScenarioCaseService;
    @Resource
    private PlanApiAutomationService planApiAutomationService;
    @Resource
    private PlanUiAutomationService planUiAutomationService;

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    /**
     * 执行测试计划流程是会调用其它服务的执行方法，并通过kafka传递信息给test-track服务来判断测试计划是否执行结束
     * 执行方法采用单独的事务控制，执行完了就提交，让测试报告以及包括执行内容的数据及时入库
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String runTestPlan(String testPlanId, String projectId, String userId, String triggerMode, String planReportId, String executionWay, String apiRunConfig) {
        RunModeConfigDTO runModeConfig = null;
        try {
            runModeConfig = JSON.parseObject(apiRunConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (runModeConfig == null) {
            runModeConfig = this.buildRunModeConfigDTO();
        }

        //环境参数为空时，依据测试计划保存的环境执行
        if (((StringUtils.equals("GROUP", runModeConfig.getEnvironmentType()) && StringUtils.isBlank(runModeConfig.getEnvironmentGroupId()))
                || (!StringUtils.equals("GROUP", runModeConfig.getEnvironmentType()) && MapUtils.isEmpty(runModeConfig.getEnvMap()) && MapUtils.isEmpty(runModeConfig.getTestPlanDefaultEnvMap())))
                && !StringUtils.equals(executionWay, ExecutionWay.RUN.name())) {
            TestPlanWithBLOBs testPlanWithBLOBs = testPlanMapper.selectByPrimaryKey(testPlanId);
            if (StringUtils.isNotEmpty(testPlanWithBLOBs.getRunModeConfig())) {
                try {
                    Map json = JSON.parseMap(testPlanWithBLOBs.getRunModeConfig());
                    TestPlanRequestUtil.changeStringToBoolean(json);
                    TestPlanRunRequest testPlanRunRequest = JSON.parseObject(JSON.toJSONString(json), TestPlanRunRequest.class);
                    if (testPlanRunRequest != null) {
                        String envType = testPlanRunRequest.getEnvironmentType();
                        Map<String, String> envMap = testPlanRunRequest.getEnvMap();
                        String environmentGroupId = testPlanRunRequest.getEnvironmentGroupId();
                        runModeConfig = testPlanService.getRunModeConfigDTO(testPlanRunRequest, envType, envMap, environmentGroupId, testPlanId);
                        runModeConfig.setTestPlanDefaultEnvMap(testPlanRunRequest.getTestPlanDefaultEnvMap());
                        if (!testPlanRunRequest.isRunWithinResourcePool()) {
                            runModeConfig.setResourcePoolId(null);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("获取测试计划保存的环境信息出错!", e);
                }
            }
        }
        if (planReportId == null) {
            planReportId = UUID.randomUUID().toString();
        }
        if (testPlanService.haveExecCase(testPlanId, true)) {
            testPlanService.verifyPool(projectId, runModeConfig);
        }
        //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanService.genTestPlanReport(planReportId, testPlanId, userId, triggerMode, runModeConfig);

        LoggerUtil.info("预生成测试计划报告【" + reportInfoDTO.getTestPlanReport() != null ? reportInfoDTO.getTestPlanReport().getName() : StringUtils.EMPTY + "】计划报告ID[" + planReportId + "]");

        List<TestPlanApiDTO> apiTestCases = null;
        List<TestPlanScenarioDTO> scenarioCases = null;
        List<TestPlanUiScenarioDTO> uiScenarios = null;
        Map<String, String> loadCaseReportMap = null;
        if (MapUtils.isNotEmpty(reportInfoDTO.getApiTestCaseDataMap())) {
            try {
                apiTestCases = planTestPlanApiCaseService.getFailureListByIds(reportInfoDTO.getApiTestCaseDataMap().keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询接口用例失败!", e);
            }
        }
        if (MapUtils.isNotEmpty(reportInfoDTO.getPlanScenarioIdMap())) {
            try {
                scenarioCases = planTestPlanScenarioCaseService.getFailureListByIds(reportInfoDTO.getPlanScenarioIdMap().keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询场景用例失败!", e);
            }
        }
        if (MapUtils.isNotEmpty(reportInfoDTO.getUiScenarioIdMap())) {
            try {
                uiScenarios = planTestPlanUiScenarioCaseService.getFailureListByIds(reportInfoDTO.getUiScenarioIdMap().keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询UI用例失败!", e);
            }
        }
        boolean haveApiCaseExec = false, haveScenarioCaseExec = false, haveLoadCaseExec = false, haveUICaseExec = false;
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            //执行接口案例任务
            LoggerUtil.info("开始执行测试计划接口用例 " + planReportId);
            try {
                Map<String, String> apiCaseReportMap = testPlanService.executeApiTestCase(triggerMode, planReportId, userId, testPlanId, runModeConfig);
                if (MapUtils.isNotEmpty(apiCaseReportMap)) {
                    haveApiCaseExec = true;
                    for (TestPlanApiDTO dto : apiTestCases) {
                        dto.setReportId(apiCaseReportMap.get(dto.getId()));
                    }
                }
            } catch (Exception e) {
                apiTestCases = null;
                LoggerUtil.info("测试报告" + planReportId + "本次执行测试计划接口用例失败！ ", e);
            }
        }
        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            //执行场景执行任务
            LoggerUtil.info("开始执行测试计划场景用例 " + planReportId);
            try {
                Map<String, String> scenarioReportMap = testPlanService.executeScenarioCase(planReportId, testPlanId, projectId, runModeConfig, triggerMode, userId, reportInfoDTO.getPlanScenarioIdMap());
                if (MapUtils.isNotEmpty(scenarioReportMap)) {
                    haveScenarioCaseExec = true;
                    List<TestPlanScenarioDTO> removeDTO = new ArrayList<>();
                    for (TestPlanScenarioDTO dto : scenarioCases) {
                        if (scenarioReportMap.containsKey(dto.getId())) {
                            dto.setReportId(scenarioReportMap.get(dto.getId()));
                        } else {
                            removeDTO.add(dto);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(removeDTO)) {
                        scenarioCases.removeAll(removeDTO);
                    }
                }
            } catch (Exception e) {
                scenarioCases = null;
                LoggerUtil.info("测试报告" + planReportId + "本次执行测试计划场景用例失败！ ", e);
            }
        }

        if (MapUtils.isNotEmpty(reportInfoDTO.getPerformanceIdMap())) {
            //执行性能测试任务
            LoggerUtil.info("开始执行测试计划性能用例 " + planReportId);
            try {
                loadCaseReportMap = perfExecService.executeLoadCase(planReportId, runModeConfig, testPlanService.transformationPerfTriggerMode(triggerMode), reportInfoDTO.getPerformanceIdMap());
                if (MapUtils.isNotEmpty(loadCaseReportMap)) {
                    haveLoadCaseExec = true;
                }
            } catch (Exception e) {
                LoggerUtil.info("测试报告" + planReportId + "本次执行测试计划性能用例失败！ ", e);
            }
        }

        if (CollectionUtils.isNotEmpty(uiScenarios)) {
            //执行UI场景执行任务
            LoggerUtil.info("开始执行测试计划 UI 场景用例 " + planReportId);
            try {
                Map<String, String> uiScenarioReportMap = testPlanService.executeUiScenarioCase(planReportId, testPlanId, projectId, runModeConfig, triggerMode, userId, reportInfoDTO.getUiScenarioIdMap());
                if (MapUtils.isNotEmpty(uiScenarioReportMap)) {
                    haveUICaseExec = true;
                    for (TestPlanUiScenarioDTO dto : uiScenarios) {
                        dto.setReportId(uiScenarioReportMap.get(dto.getId()));
                    }
                }
            } catch (Exception e) {
                uiScenarios = null;
                LoggerUtil.info("测试报告" + planReportId + "本次执行测试计划 UI 用例失败！ ", e);
            }
        }

        LoggerUtil.info("开始生成测试计划报告内容 " + planReportId);
        testPlanReportService.createTestPlanReportContentReportIds(planReportId, apiTestCases, scenarioCases, uiScenarios, loadCaseReportMap);
        if (!haveApiCaseExec && !haveScenarioCaseExec && !haveLoadCaseExec && !haveUICaseExec) {
            //如果没有执行的自动化用例，调用结束测试计划的方法。 因为方法中包含着测试计划执行队列的处理逻辑。
            testPlanReportService.testPlanExecuteOver(planReportId, TestPlanReportStatus.COMPLETED.name());
        }
        return planReportId;
    }

    private RunModeConfigDTO buildRunModeConfigDTO() {
        RunModeConfigDTO runModeConfig = new RunModeConfigDTO();
        runModeConfig.setMode(RunModeConstants.SERIAL.name());
        runModeConfig.setReportType("iddReport");
        runModeConfig.setEnvMap(new HashMap<>());
        runModeConfig.setOnSampleError(false);
        return runModeConfig;
    }
}
