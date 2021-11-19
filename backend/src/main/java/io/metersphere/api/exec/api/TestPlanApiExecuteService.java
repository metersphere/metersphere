package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.exec.queue.SerialBlockingQueueUtil;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiCaseExample;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
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
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private RemakeReportService remakeReportService;

    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<String> ids = request.getPlanIds();
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
        // 资源池
        GenerateHashTreeUtil.setPoolResource(request.getConfig());
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
            serial(executeQueue, request);
        } else {
            Map<String, TestPlanApiCase> executeQueue = new HashMap<>();
            //记录案例线程结果以及执行失败的案例ID
            Map<String, String> executeThreadIdMap = new HashMap<>();
            planApiCases.forEach(testPlanApiCase -> {
                ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, APITestStatus.Running.name(), batchMapper);
                executeQueue.put(report.getId(), testPlanApiCase);
                executeThreadIdMap.put(testPlanApiCase.getId(), report.getId());
                MessageCache.caseExecResourceLock.put(report.getId(), report);
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

    private void serial(Map<TestPlanApiCase, ApiDefinitionExecResult> executeQueue, BatchRunDefinitionRequest request) {
        // 开始串行执行
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("TestPlanCase串行执行线程");
                    List<String> executeErrorList = new ArrayList<>();
                    for (TestPlanApiCase testPlanApiCase : executeQueue.keySet()) {
                        try {
                            ApiDefinitionExecResult execResult = executeQueue.get(testPlanApiCase);
                            execResult.setStatus(APITestStatus.Running.name());
                            apiDefinitionExecResultMapper.updateByPrimaryKey(execResult);

                            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testPlanApiCase.getId(), execResult.getId(), request.getTriggerMode(), null);
                            runRequest.setConfig(request.getConfig());
                            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig()));
                            runRequest.setTestPlanReportId(request.getPlanReportId());
                            try {
                                if (!GenerateHashTreeUtil.isResourcePool(request.getConfig()).isPool()) {
                                    runRequest.setHashTree(generateHashTree(testPlanApiCase.getId()));
                                }
                            } catch (Exception e) {
                                remakeReportService.remake(runRequest);
                                continue;
                            }
                            // 初始化等待队列
                            SerialBlockingQueueUtil.init(runRequest.getReportId(), 1);
                            LoggerUtil.info("TestPlan Serial run 【 " + runRequest.getReportId() + "】start");
                            // 开始执行
                            jMeterService.run(runRequest);

                            Object reportObj = SerialBlockingQueueUtil.take(runRequest.getReportId());
                            LoggerUtil.info("TestPlan Serial run 【 " + runRequest.getReportId() + "】end");
                            // 如果开启失败结束执行，则判断返回结果状态
                            if (request.getConfig().isOnSampleError()) {
                                if (reportObj != null) {
                                    ApiDefinitionExecResult result = (ApiDefinitionExecResult) reportObj;
                                    if (result == null || !result.getStatus().equals("Success")) {
                                        break;
                                    }
                                }
                            }

                        } catch (Exception e) {
                            executeErrorList.add(testPlanApiCase.getId());
                            LogUtil.error("执行终止：" + e.getMessage());
                            break;
                        }
                    }
                    //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
                    TestPlanReportExecuteCatch.set(request.getPlanReportId(), executeErrorList);

                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        });
        thread.start();
    }

    private void parallel(Map<String, TestPlanApiCase> executeQueue, BatchRunDefinitionRequest request) {
        List<String> executeErrorList = new ArrayList<>();
        for (String reportId : executeQueue.keySet()) {
            TestPlanApiCase testPlanApiCase = executeQueue.get(reportId);
            try {
                HashTree hashTree = null;
                if (!GenerateHashTreeUtil.isResourcePool(request.getConfig()).isPool()) {
                    hashTree = generateHashTree(testPlanApiCase.getId());
                }
                JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testPlanApiCase.getId(), reportId, request.getTriggerMode(), hashTree);
                runRequest.setConfig(request.getConfig());
                runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig()));
                runRequest.setTestPlanReportId(request.getPlanReportId());
                jMeterService.run(runRequest);
            } catch (Exception e) {
                executeErrorList.add(testPlanApiCase.getId());
            }
        }

        //如果是测试计划生成报告的执行，则更新执行信息、执行线程信息。
        TestPlanReportExecuteCatch.set(request.getPlanReportId(), executeErrorList);
    }

    public HashTree generateHashTree(String testId) {
        TestPlanApiCase apiCase = testPlanApiCaseMapper.selectByPrimaryKey(testId);
        if (apiCase != null) {
            ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(apiCase.getApiCaseId());
            HashTree jmeterHashTree = new HashTree();
            MsTestPlan testPlan = new MsTestPlan();
            testPlan.setHashTree(new LinkedList<>());
            if (caseWithBLOBs != null) {
                try {
                    MsThreadGroup group = new MsThreadGroup();
                    group.setLabel(caseWithBLOBs.getName());
                    group.setName(caseWithBLOBs.getName());
                    MsTestElement testElement = parse(caseWithBLOBs, testId);
                    group.setHashTree(new LinkedList<>());
                    group.getHashTree().add(testElement);
                    testPlan.getHashTree().add(group);
                } catch (Exception ex) {
                    MSException.throwException(ex.getMessage());
                }
            }
            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
            return jmeterHashTree;
        }
        return null;
    }

    private MsTestElement parse(ApiTestCaseWithBLOBs caseWithBLOBs, String planId) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String api = caseWithBLOBs.getRequest();
            JSONObject element = JSON.parseObject(api);
            ElementUtil.dataFormatting(element);

            LinkedList<MsTestElement> list = new LinkedList<>();
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                list.addAll(elements);
            }
            TestPlanApiCase apiCase = testPlanApiCaseMapper.selectByPrimaryKey(planId);
            if (element.getString("type").equals("HTTPSamplerProxy")) {
                MsHTTPSamplerProxy httpSamplerProxy = JSON.parseObject(api, MsHTTPSamplerProxy.class);
                httpSamplerProxy.setHashTree(list);
                httpSamplerProxy.setName(planId);
                httpSamplerProxy.setUseEnvironment(apiCase.getEnvironmentId());
                return httpSamplerProxy;
            }
            if (element.getString("type").equals("TCPSampler")) {
                MsTCPSampler msTCPSampler = JSON.parseObject(api, MsTCPSampler.class);
                msTCPSampler.setUseEnvironment(apiCase.getEnvironmentId());
                msTCPSampler.setHashTree(list);
                msTCPSampler.setName(planId);
                return msTCPSampler;
            }
            if (element.getString("type").equals("DubboSampler")) {
                MsDubboSampler dubboSampler = JSON.parseObject(api, MsDubboSampler.class);
                dubboSampler.setUseEnvironment(apiCase.getEnvironmentId());
                dubboSampler.setHashTree(list);
                dubboSampler.setName(planId);
                return dubboSampler;
            }
            if (element.getString("type").equals("JDBCSampler")) {
                MsJDBCSampler jDBCSampler = JSON.parseObject(api, MsJDBCSampler.class);
                jDBCSampler.setUseEnvironment(apiCase.getEnvironmentId());
                jDBCSampler.setHashTree(list);
                jDBCSampler.setName(planId);
                return jDBCSampler;
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }
}
