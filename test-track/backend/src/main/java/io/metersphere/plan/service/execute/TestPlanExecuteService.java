package io.metersphere.plan.service.execute;

import com.esotericsoftware.minlog.Log;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.TestPlanWithBLOBs;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.commons.constants.TestPlanExecuteCaseType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.dto.ExecutionWay;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.performance.PerfExecService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.plan.utils.TestPlanRequestUtil;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
@Transactional(rollbackFor = Exception.class)
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
    private TestPlanMapper testPlanMapper;

    @Resource
    private RedisTemplateService redisTemplateService;

    /**
     * 执行测试计划流程是会调用其它服务的执行方法，并通过kafka传递信息给test-track服务来判断测试计划是否执行结束
     * 执行方法采用单独的事务控制，执行完了就提交，让测试报告以及包括执行内容的数据及时入库
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String runTestPlan(String testPlanId, String projectId, String userId, String triggerMode, String planReportId, String executionWay, String apiRunConfig) {
        //获取运行模式
        RunModeConfigDTO runModeConfig = this.getRunModeConfig(apiRunConfig, executionWay, testPlanId);
        if (StringUtils.isEmpty(planReportId)) {
            planReportId = UUID.randomUUID().toString();
        }
        TestPlanReport testPlanReport = null;
        try {
            this.checkTestPlanCanRunning(testPlanId, projectId, runModeConfig, triggerMode);
            //创建测试报告，然后返回的ID重新赋值为resourceID，作为后续的参数
            TestPlanScheduleReportInfoDTO reportInfoDTO = testPlanService.genTestPlanReport(planReportId, testPlanId, userId, triggerMode, runModeConfig);
            testPlanReport = reportInfoDTO.getTestPlanReport();
            LoggerUtil.info("预生成测试计划报告【" + (reportInfoDTO.getTestPlanReport() != null ? reportInfoDTO.getTestPlanReport().getName() : StringUtils.EMPTY) + "】计划报告ID[" + planReportId + "]");
            this.execute(reportInfoDTO, runModeConfig, triggerMode, projectId, userId);
        } catch (Exception e) {
            //如果执行失败，要保证执行队列是否不被影响
            if (testPlanReport == null) {
                testPlanReport = new TestPlanReport();
                testPlanReport.setId(planReportId);
            }
            testPlanReportService.testPlanUnExecute(testPlanReport);
            Log.error("执行测试计划失败!", e);
        }
        return planReportId;
    }

    private void checkTestPlanCanRunning(String testPlanId, String projectId, RunModeConfigDTO runModeConfig, String triggerMode) throws Exception {

        // 校验测试计划是否在执行中
        if (StringUtils.startsWith(triggerMode, "SCHEDULE") && testPlanService.checkTestPlanIsRunning(testPlanId)) {

            LogUtil.info("当前测试计划正在执行中，请稍后再试", testPlanId);
            MSException.throwException(Translator.get("test_plan_run_message"));
        }
        //检查执行资源池
        if (testPlanService.haveExecCase(testPlanId, true)) {
            testPlanService.verifyPool(projectId, runModeConfig);
        }
    }

    private void execute(TestPlanScheduleReportInfoDTO reportInfoDTO, RunModeConfigDTO runModeConfig, String triggerMode, String projectId, String executeUser) throws Exception {
        CaseExecuteResult caseExecuteResult = new CaseExecuteResult();

        CountDownLatch countDownLatch = this.countDownExecute(reportInfoDTO, caseExecuteResult, runModeConfig, triggerMode, projectId, executeUser);
        countDownLatch.await();
        LoggerUtil.info("开始生成测试计划报告内容 " + reportInfoDTO.getTestPlanReport().getId());
        testPlanReportService.createTestPlanReportContentReportIds(reportInfoDTO.getTestPlanReport().getId(),
                caseExecuteResult.getApiCaseDTO(), caseExecuteResult.getScenarioCases(), caseExecuteResult.getUiScenarios(), caseExecuteResult.getLoadCaseReportMap());
        if (!caseExecuteResult.isExecuting()) {
            MSException.throwException("测试计划执行失败，不存在可执行的用例！报告ID:[" + reportInfoDTO.getTestPlanReport().getTestPlanId() + "]");
        }
    }

    private CountDownLatch countDownExecute(TestPlanScheduleReportInfoDTO reportInfoDTO, CaseExecuteResult caseExecuteResult, RunModeConfigDTO runModeConfig, String triggerMode, String projectId, String executeUser) {
        CountDownLatch countDownLatch = new CountDownLatch(4);
        try {
            this.executeApiCase(caseExecuteResult, reportInfoDTO.getApiTestCaseDataMap(), triggerMode,
                    reportInfoDTO.getTestPlanReport().getId(), reportInfoDTO.getTestPlanReport().getTestPlanId(), executeUser, runModeConfig);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            countDownLatch.countDown();
        }

        try {
            this.executeScenarioCase(caseExecuteResult, reportInfoDTO.getPlanScenarioIdMap(), triggerMode,
                    reportInfoDTO.getTestPlanReport().getId(), reportInfoDTO.getTestPlanReport().getTestPlanId(), projectId, executeUser, runModeConfig);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            countDownLatch.countDown();
        }
        try {
            this.executeUiCase(caseExecuteResult, reportInfoDTO.getUiScenarioIdMap(), triggerMode,
                    reportInfoDTO.getTestPlanReport().getId(), reportInfoDTO.getTestPlanReport().getTestPlanId(), projectId, executeUser, runModeConfig);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            countDownLatch.countDown();
        }
        try {
            this.executeLoadCase(caseExecuteResult, reportInfoDTO.getPerformanceIdMap(), triggerMode,
                    reportInfoDTO.getTestPlanReport().getId(), runModeConfig);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            countDownLatch.countDown();
        }
        return countDownLatch;
    }

    private void executeApiCase(CaseExecuteResult executeResult, Map<String, String> executeCase, String triggerMode, String testPlanReportId, String testPlanId, String executeUser, RunModeConfigDTO runModeConfig) {
        boolean executing = false;
        List<TestPlanApiDTO> apiTestCases = null;
        if (MapUtils.isNotEmpty(executeCase)) {
            try {
                apiTestCases = planTestPlanApiCaseService.getFailureListByIds(executeCase.keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询接口用例失败!", e);
            }
        }
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            //执行接口案例任务
            LoggerUtil.info("开始执行测试计划接口用例 " + testPlanReportId);
            try {
                redisTemplateService.lock(testPlanReportId, TestPlanExecuteCaseType.API_CASE.name(), testPlanReportId);
                Map<String, String> apiCaseReportMap = testPlanService.executeApiTestCase(triggerMode, testPlanReportId, executeUser, testPlanId, runModeConfig);
                if (MapUtils.isNotEmpty(apiCaseReportMap)) {
                    executing = true;
                    for (TestPlanApiDTO dto : apiTestCases) {
                        dto.setReportId(apiCaseReportMap.get(dto.getId()));
                    }
                } else {
                    redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.API_CASE.name(), testPlanReportId);
                    executing = false;
                    apiTestCases.clear();
                }
            } catch (Exception e) {
                redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.API_CASE.name(), testPlanReportId);
                apiTestCases = null;
                executing = false;
                LoggerUtil.info("测试报告" + testPlanReportId + "本次执行测试计划接口用例失败！ ", e);
            }
        }
        executeResult.setApiCaseExecuting(executing);
        executeResult.setApiCaseDTO(apiTestCases);
    }

    private void executeScenarioCase(CaseExecuteResult executeResult, Map<String, String> executeCase, String triggerMode, String testPlanReportId, String testPlanId, String projectId, String executeUser, RunModeConfigDTO runModeConfig) {
        boolean executing = false;
        List<TestPlanScenarioDTO> scenarioCases = null;
        if (MapUtils.isNotEmpty(executeCase)) {
            try {
                scenarioCases = planTestPlanScenarioCaseService.getFailureListByIds(executeCase.keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询场景用例失败!", e);
            }
        }

        if (CollectionUtils.isNotEmpty(scenarioCases)) {
            //执行场景执行任务
            LoggerUtil.info("开始执行测试计划场景用例 " + testPlanReportId);
            try {

                redisTemplateService.lock(testPlanReportId, TestPlanExecuteCaseType.SCENARIO.name(), testPlanReportId);
                Map<String, String> scenarioReportMap = testPlanService.executeScenarioCase(testPlanReportId, testPlanId, projectId, runModeConfig, triggerMode, executeUser, executeCase);
                if (MapUtils.isNotEmpty(scenarioReportMap)) {
                    executing = true;
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
                } else {
                    //如果没有执行的用例，解锁数据，并设置执行数据为空
                    redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.SCENARIO.name(), testPlanReportId);
                    executing = false;
                    scenarioCases.clear();
                }
            } catch (Exception e) {
                redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.SCENARIO.name(), testPlanReportId);
                scenarioCases = null;
                executing = false;
                LoggerUtil.info("测试报告" + testPlanReportId + "本次执行测试计划场景用例失败！ ", e);
            }
        }
        executeResult.setScenarioCases(scenarioCases);
        executeResult.setScenarioExecuting(executing);
    }

    private void executeUiCase(CaseExecuteResult executeResult, Map<String, String> executeCase, String triggerMode, String testPlanReportId, String testPlanId, String projectId, String executeUser, RunModeConfigDTO runModeConfig) {
        boolean executing = false;
        List<TestPlanUiScenarioDTO> uiScenarios = null;
        if (MapUtils.isNotEmpty(executeCase)) {
            try {
                uiScenarios = planTestPlanUiScenarioCaseService.getFailureListByIds(executeCase.keySet());
            } catch (Exception e) {
                LogUtil.error("测试计划执行查询UI用例失败!", e);
            }
        }
        if (CollectionUtils.isNotEmpty(uiScenarios)) {
            //执行UI场景执行任务
            LoggerUtil.info("开始执行测试计划 UI 场景用例 " + testPlanReportId);
            try {
                redisTemplateService.lock(testPlanReportId, TestPlanExecuteCaseType.UI_SCENARIO.name(), testPlanReportId);
                Map<String, String> uiScenarioReportMap = testPlanService.executeUiScenarioCase(testPlanReportId, testPlanId, projectId, runModeConfig, triggerMode, executeUser, executeCase);
                if (MapUtils.isNotEmpty(uiScenarioReportMap)) {
                    executing = true;
                    for (TestPlanUiScenarioDTO dto : uiScenarios) {
                        dto.setReportId(uiScenarioReportMap.get(dto.getId()));
                    }
                } else {
                    redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.UI_SCENARIO.name(), testPlanReportId);
                    uiScenarios.clear();
                    executing = false;
                }
            } catch (Exception e) {
                redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.UI_SCENARIO.name(), testPlanReportId);
                uiScenarios = null;
                executing = false;
                LoggerUtil.info("测试报告" + testPlanReportId + "本次执行测试计划 UI 用例失败！ ", e);
            }
        }
        executeResult.setUiScenarios(uiScenarios);
        executeResult.setUiScenarioExecuting(executing);
    }

    private void executeLoadCase(CaseExecuteResult executeResult, Map<String, String> executeCase, String triggerMode, String testPlanReportId, RunModeConfigDTO runModeConfig) {
        boolean executing = false;

        Map<String, String> loadCaseReportMap = null;

        if (MapUtils.isNotEmpty(executeCase)) {
            //执行性能测试任务
            LoggerUtil.info("开始执行测试计划性能用例 " + testPlanReportId);
            try {
                redisTemplateService.lock(testPlanReportId, TestPlanExecuteCaseType.LOAD_CASE.name(), testPlanReportId);
                loadCaseReportMap = perfExecService.executeLoadCase(testPlanReportId, runModeConfig, testPlanService.transformationPerfTriggerMode(triggerMode), executeCase);
                if (MapUtils.isNotEmpty(loadCaseReportMap)) {
                    executing = true;
                } else {
                    redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.LOAD_CASE.name(), testPlanReportId);
                    executing = false;
                }
            } catch (Exception e) {
                executing = false;
                redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.LOAD_CASE.name(), testPlanReportId);
                LoggerUtil.info("测试报告" + testPlanReportId + "本次执行测试计划性能用例失败！ ", e);
            }
        }

        executeResult.setLoadCaseReportMap(loadCaseReportMap);
        executeResult.setLoadCaseExecuting(executing);
    }

    private RunModeConfigDTO getRunModeConfig(String apiRunConfig, String executionWay, String testPlanId) {
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
        return runModeConfig;
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

@Data
class CaseExecuteResult {
    private boolean apiCaseExecuting;
    private boolean scenarioExecuting;
    private boolean uiScenarioExecuting;
    private boolean loadCaseExecuting;


    private List<TestPlanApiDTO> apiCaseDTO;
    private List<TestPlanScenarioDTO> scenarioCases;
    private List<TestPlanUiScenarioDTO> uiScenarios;
    private Map<String, String> loadCaseReportMap;

    public boolean isExecuting() {
        return apiCaseExecuting || scenarioExecuting || uiScenarioExecuting || loadCaseExecuting;
    }
}
