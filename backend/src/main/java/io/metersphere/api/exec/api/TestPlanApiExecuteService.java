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
import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiCaseExample;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
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
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiExecuteService {
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;

    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<String> ids = request.getPlanIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedList<>();
        }
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
        // 开始选择执行模式
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            Map<TestPlanApiCase, ApiDefinitionExecResult> executeQueue = new LinkedHashMap<>();
            //记录案例线程结果以及执行失败的案例ID
            Map<String, String> executeThreadIdMap = new HashMap<>();

            planApiCases.forEach(testPlanApiCase -> {
                ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, APITestStatus.Waiting.name(), batchMapper);
                executeQueue.put(testPlanApiCase, report);
                executeThreadIdMap.put(testPlanApiCase.getId(), report.getId());
                responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
            });

            //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
            if (TestPlanReportExecuteCatch.containsReport(request.getPlanReportId())) {
                if (!executeThreadIdMap.isEmpty()) {
                    TestPlanReportExecuteCatch.updateTestPlanThreadInfo(request.getPlanReportId(), executeThreadIdMap, null, null);
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
            // 开始串行执行
            String runMode = request.getTriggerMode();
            DBTestQueue executionQueue = apiExecutionQueueService.add(executeQueue, request.getConfig() != null ? request.getConfig().getResourcePoolId() : null, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), null, runMode);
            if (executionQueue != null) {
                if (executionQueue.getQueue() != null) {
                    apiScenarioSerialService.serial(executionQueue, executionQueue.getQueue());
                }
            }
        } else {
            Map<String, TestPlanApiCase> executeQueue = new HashMap<>();
            //记录案例线程结果以及执行失败的案例ID
            Map<String, String> executeThreadIdMap = new HashMap<>();
            planApiCases.forEach(testPlanApiCase -> {
                ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, APITestStatus.Running.name(), batchMapper);
                executeQueue.put(report.getId(), testPlanApiCase);
                executeThreadIdMap.put(testPlanApiCase.getId(), report.getId());
                responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
            //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
            if (TestPlanReportExecuteCatch.containsReport(request.getPlanReportId())) {
                if (!executeThreadIdMap.isEmpty()) {
                    TestPlanReportExecuteCatch.updateTestPlanThreadInfo(request.getPlanReportId(), executeThreadIdMap, null, null);
                }
            }
            // 开始并发执行
            this.parallel(executeQueue, request);
        }

        return responseDTOS;
    }

    private void parallel(Map<String, TestPlanApiCase> executeQueue, BatchRunDefinitionRequest request) {
        List<String> executeErrorList = new ArrayList<>();
        String poolId = request.getConfig() != null ? request.getConfig().getResourcePoolId() : null;
        String mode = request.getConfig() != null ? request.getConfig().getMode() : null;
        ApiExecutionQueue executionQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), mode, request.getTriggerMode());
        if (executionQueue != null) {
            for (String reportId : executeQueue.keySet()) {
                TestPlanApiCase testPlanApiCase = executeQueue.get(reportId);
                try {
                    HashTree hashTree = null;
                    if (request.getConfig() == null || !GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()).isPool()) {
                        hashTree = apiScenarioSerialService.generateHashTree(testPlanApiCase.getId());
                    }
                    JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testPlanApiCase.getId(), reportId, request.getTriggerMode(), hashTree);
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
                } catch (Exception e) {
                    executeErrorList.add(testPlanApiCase.getId());
                }
            }
            //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
            TestPlanReportExecuteCatch.set(request.getPlanReportId(), executeErrorList);
        }
    }
}
