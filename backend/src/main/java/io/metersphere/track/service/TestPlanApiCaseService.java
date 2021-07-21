package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.service.task.ParallelApiExecTask;
import io.metersphere.track.service.task.SerialApiExecTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Lazy
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper mapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    public TestPlanApiCase getInfo(String caseId, String testPlanId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(caseId).andTestPlanIdEqualTo(testPlanId);
        return testPlanApiCaseMapper.selectByExample(example).get(0);
    }

    public List<TestPlanApiCaseDTO> list(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<TestPlanApiCaseDTO> apiTestCases = extTestPlanApiCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public void buildUserInfo(List<? extends TestPlanApiCaseDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getCreateUser).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUpdateUser).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUserId).collect(Collectors.toList()));
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            apiTestCases.forEach(caseResult -> {
                caseResult.setCreatorName(userMap.get(caseResult.getCreateUser()));
                caseResult.setUpdateName(userMap.get(caseResult.getUpdateUser()));
                caseResult.setPrincipalName(userMap.get(caseResult.getUserId()));
            });
        }
    }

    public List<String> selectIds(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<String> idList = extTestPlanApiCaseMapper.selectIds(request);
        return idList;
    }

    public List<String> getExecResultByPlanId(String plan) {
        return extTestPlanApiCaseMapper.getExecResultByPlanId(plan);
    }

    public Pager<List<ApiTestCaseDTO>> relevanceList(int goPage, int pageSize, ApiTestCaseRequest request) {
        List<String> ids = apiTestCaseService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        if (CollectionUtils.isEmpty(ids)) {
            return PageUtils.setPageInfo(page, new ArrayList<>());
        }
        request.setIds(ids);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiTestCaseService.listSimple(request));
    }

    public int delete(String id) {
        apiDefinitionExecResultService.deleteByResourceId(id);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteByPlanId(String planId) {
        List<String> ids = extTestPlanApiCaseMapper.getIdsByPlanId(planId);
        apiDefinitionExecResultService.deleteByResourceIds(ids);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        List<String> deleteIds = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            deleteIds = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                deleteIds.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        apiDefinitionExecResultService.deleteByResourceIds(deleteIds);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdIn(deleteIds)
                .andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiCaseMapper.deleteByExample(example);
    }

    public List<TestPlanApiCase> getCasesByPlanId(String planId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.selectByExample(example);
    }

    public TestPlanApiCase getById(String id) {
        return testPlanApiCaseMapper.selectByPrimaryKey(id);
    }

    public void setExecResult(String id, String status, Long time) {
        TestPlanApiCase apiCase = new TestPlanApiCase();
        apiCase.setId(id);
        apiCase.setStatus(status);
        apiCase.setUpdateTime(time);
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void updateByPrimaryKeySelective(TestPlanApiCase apiCase) {
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        request.setPlanId(planId);
        request.setIds(extTestPlanApiCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        deleteApiCaseBath(request);
    }

    public void batchUpdateEnv(TestPlanApiCaseBatchRequest request) {
        // 批量修改用例环境
        Map<String, String> rows = request.getSelectRows();
        Set<String> ids = rows.keySet();
        request.setIds(new ArrayList<>(ids));
        Map<String, String> env = request.getProjectEnvMap();
        if (env != null && !env.isEmpty()) {
            ids.forEach(id -> {
                TestPlanApiCase apiCase = new TestPlanApiCase();
                apiCase.setId(id);
                apiCase.setEnvironmentId(env.get(rows.get(id)));
                testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
            });
        }
    }

    public String getState(String id) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(id);
        return testPlanApiCaseMapper.selectByExample(example).get(0).getStatus();

    }

    public List<TestPlanApiCaseDTO> selectAllTableRows(TestPlanApiCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        ApiTestCaseRequest selectReq = new ApiTestCaseRequest();
        selectReq.setIds(ids);
        List<TestPlanApiCaseDTO> returnList = extTestPlanApiCaseMapper.list(selectReq);
        return returnList;
    }

    public String getLogDetails(String id) {
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(id);
        if (testPlanApiCase != null) {
            ApiTestCaseWithBLOBs testCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanApiCase.getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testCase.getProjectId(), testCase.getName(), testPlanApiCase.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiCase> nodes = testPlanApiCaseMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
            testCaseExample.createCriteria().andIdIn(nodes.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList()));
            List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(testCases)) {
                List<String> names = testCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testCases.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }


    private MsTestElement parse(ApiTestCaseWithBLOBs caseWithBLOBs, String planId) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String api = caseWithBLOBs.getRequest();
            JSONObject element = JSON.parseObject(api);
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
            e.printStackTrace();
            LogUtil.error(e.getMessage());
        }
        return null;
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

    private ApiDefinitionExecResult addResult(BatchRunDefinitionRequest request, TestPlanApiCase key, String status, ApiDefinitionExecResultMapper batchMapper) {
        ApiDefinitionExecResult apiResult = new ApiDefinitionExecResult();
        apiResult.setId(UUID.randomUUID().toString());
        apiResult.setCreateTime(System.currentTimeMillis());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setEndTime(System.currentTimeMillis());
        ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(key.getApiCaseId());
        if (caseWithBLOBs != null) {
            apiResult.setName(caseWithBLOBs.getName());
        }
        apiResult.setTriggerMode(TriggerMode.BATCH.name());
        apiResult.setActuator("LOCAL");
        if (request.getConfig() != null && StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
            apiResult.setActuator(request.getConfig().getResourcePoolId());
        }
        apiResult.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiResult.setResourceId(key.getApiCaseId());
        apiResult.setStartTime(System.currentTimeMillis());
        apiResult.setType(ApiRunMode.API_PLAN.name());
        apiResult.setStatus(status);
        batchMapper.insert(apiResult);
        return apiResult;
    }

    public String modeRun(BatchRunDefinitionRequest request) {
        List<String> ids = request.getPlanIds();
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiCase> planApiCases = testPlanApiCaseMapper.selectByExample(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionExecResultMapper batchMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        // 开始选择执行模式
        ExecutorService executorService = Executors.newFixedThreadPool(planApiCases.size());
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            Map<TestPlanApiCase, ApiDefinitionExecResult> executeQueue = new HashMap<>();
            planApiCases.forEach(testPlanApiCase -> {
                ApiDefinitionExecResult report = addResult(request, testPlanApiCase, APITestStatus.Waiting.name(), batchMapper);
                executeQueue.put(testPlanApiCase, report);
            });
            sqlSession.flushStatements();
            List<String> reportIds = new LinkedList<>();
            // 开始串行执行
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (TestPlanApiCase testPlanApiCase : executeQueue.keySet()) {
                        try {
                            ApiDefinitionExecResult execResult = executeQueue.get(testPlanApiCase);
                            execResult.setId(executeQueue.get(testPlanApiCase).getId());
                            execResult.setStatus(APITestStatus.Running.name());
                            mapper.updateByPrimaryKey(execResult);
                            reportIds.add(execResult.getId());
                            RunModeDataDTO modeDataDTO;
                            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                                modeDataDTO = new RunModeDataDTO(testPlanApiCase.getId(), UUID.randomUUID().toString());
                            } else {
                                // 生成报告和HashTree
                                HashTree hashTree = generateHashTree(testPlanApiCase.getId());
                                modeDataDTO = new RunModeDataDTO(hashTree, UUID.randomUUID().toString());
                            }
                            modeDataDTO.setApiCaseId(execResult.getId());
                            Future<ApiDefinitionExecResult> future = executorService.submit(new SerialApiExecTask(jMeterService, mapper, modeDataDTO, request.getConfig(), ApiRunMode.API_PLAN.name()));
                            ApiDefinitionExecResult report = future.get();
                            // 如果开启失败结束执行，则判断返回结果状态
                            if (request.getConfig().isOnSampleError()) {
                                if (report == null || !report.getStatus().equals("Success")) {
                                    reportIds.remove(execResult.getId());
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            reportIds.remove(executeQueue.get(testPlanApiCase).getId());
                            LogUtil.error("执行终止：" + e.getMessage());
                            break;
                        }
                    }
                    // 清理未执行的队列
                    if (reportIds.size() < executeQueue.size()) {
                        List<String> removeList = executeQueue.entrySet().stream()
                                .filter(map -> !reportIds.contains(map.getValue().getId()))
                                .map(map -> map.getValue().getId()).collect(Collectors.toList());
                        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
                        example.createCriteria().andIdIn(removeList);
                        mapper.deleteByExample(example);
                    }
                }
            });
            thread.start();
        } else {
            // 开始并发执行
            for (TestPlanApiCase key : planApiCases) {
                RunModeDataDTO modeDataDTO = null;
                if (StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                    modeDataDTO = new RunModeDataDTO(key.getId(), UUID.randomUUID().toString());
                } else {
                    // 生成报告和HashTree
                    HashTree hashTree = generateHashTree(key.getId());
                    modeDataDTO = new RunModeDataDTO(hashTree, UUID.randomUUID().toString());
                }
                ApiDefinitionExecResult report = addResult(request, key, APITestStatus.Running.name(), batchMapper);
                modeDataDTO.setApiCaseId(report.getId());
                executorService.submit(new ParallelApiExecTask(jMeterService, mapper, modeDataDTO, request.getConfig(), ApiRunMode.API_PLAN.name()));
            }
            sqlSession.flushStatements();
        }
        return request.getId();
    }

    /**
     * 测试执行
     *
     * @param request
     * @return
     */
    public String run(BatchRunDefinitionRequest request) {
        if (request.getConfig() != null) {
            if (request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                // 校验并发数量
                int count = 50;
                BaseSystemConfigDTO dto = systemParameterService.getBaseInfo();
                if (StringUtils.isNotEmpty(dto.getConcurrency())) {
                    count = Integer.parseInt(dto.getConcurrency());
                }
                if (request.getPlanIds().size() > count) {
                    MSException.throwException("并发数量过大，请重新选择！");
                }
            }
            return this.modeRun(request);
        }
        return request.getId();
    }

    public Boolean hasFailCase(String planId, List<String> apiCaseIds) {
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return false;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andApiCaseIdIn(apiCaseIds)
                .andStatusEqualTo("error");
        return testPlanApiCaseMapper.countByExample(example) > 0 ? true : false;
    }

    public ApiTestCaseWithBLOBs getApiTestCaseById(String testPlanApiCaseId) {
        return  extTestPlanApiCaseMapper.getApiTestCaseById(testPlanApiCaseId);
    }
}
