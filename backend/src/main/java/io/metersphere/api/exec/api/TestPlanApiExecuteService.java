package io.metersphere.api.exec.api;

import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiCaseExample;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiExecuteService {
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private JMeterService jMeterService;

    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<String> ids = request.getPlanIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedList<>();
        }
        LoggerUtil.debug("开始查询测试计划用例");
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        example.setOrderByClause("`order` DESC");
        List<TestPlanApiCase> planApiCases = testPlanApiCaseMapper.selectByExample(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionExecResultMapper batchMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.API_PLAN.name());
        }

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        Map<TestPlanApiCase, ApiDefinitionExecResult> executeQueue = new HashMap<>();
        //记录案例线程结果以及执行失败的案例ID
        Map<String, String> executeThreadIdMap = new HashMap<>();
        String status = request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();
        planApiCases.forEach(testPlanApiCase -> {
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, status, batchMapper);
            executeQueue.put(testPlanApiCase, report);
            executeThreadIdMap.put(testPlanApiCase.getId(), report.getId());
            responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

        LoggerUtil.debug("开始生成测试计划队列");
        String reportType = request.getConfig() != null ? request.getConfig().getReportType() : null;
        String poolId = request.getConfig() != null ? request.getConfig().getResourcePoolId() : null;
        String runMode = StringUtils.equals(request.getTriggerMode(), TriggerMode.MANUAL.name()) ? ApiRunMode.API_PLAN.name() : ApiRunMode.SCHEDULE_API_PLAN.name();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), reportType, runMode);

        //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
        if (TestPlanReportExecuteCatch.containsReport(request.getPlanReportId())) {
            if (!executeThreadIdMap.isEmpty()) {
                TestPlanReportExecuteCatch.updateTestPlanThreadInfo(request.getPlanReportId(), executeThreadIdMap, null, null);
            }
        }
        // 开始选择执行模式
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            LoggerUtil.debug("开始串行执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                apiScenarioSerialService.serial(deQueue, deQueue.getQueue());
            }
        } else {
            LoggerUtil.debug("开始并发执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                parallel(executeQueue, request, deQueue);
            }
        }
        return responseDTOS;
    }

    private void parallel(Map<TestPlanApiCase, ApiDefinitionExecResult> executeQueue, BatchRunDefinitionRequest request, DBTestQueue executionQueue) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    Thread.currentThread().setName("测试计划入列线程");
                    for (TestPlanApiCase testPlanApiCase : executeQueue.keySet()) {
                        ApiDefinitionExecResult result = executeQueue.get(testPlanApiCase);
                        String reportId = result.getId();
                        HashTree hashTree = null;
                        if (request.getConfig() == null || !GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()).isPool()) {
                            hashTree = apiScenarioSerialService.generateHashTree(testPlanApiCase.getId());
                        }
                        String runMode = StringUtils.equals(request.getTriggerMode(), TriggerMode.MANUAL.name()) ? ApiRunMode.API_PLAN.name() : ApiRunMode.SCHEDULE_API_PLAN.name();
                        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testPlanApiCase.getId(), reportId, runMode, hashTree);
                        if (request.getConfig() != null) {
                            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()));
                            runRequest.setPoolId(request.getConfig().getResourcePoolId());
                        }
                        runRequest.setTestPlanReportId(request.getPlanReportId());
                        runRequest.setReportType(executionQueue.getReportType());
                        runRequest.setTestPlanReportId(request.getPlanReportId());
                        runRequest.setRunType(RunModeConstants.PARALLEL.toString());
                        runRequest.setQueueId(executionQueue.getId());
                        jMeterService.run(runRequest);
                    }
                } catch (Exception e) {
                    LoggerUtil.error("并发执行测试计划用例失败：" + e.getMessage());
                }
            }
        });
        thread.start();
    }
}
