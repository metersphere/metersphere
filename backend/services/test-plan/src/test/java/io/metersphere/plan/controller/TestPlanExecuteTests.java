package io.metersphere.plan.controller;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.service.TestPlanExecuteService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestService;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanExecuteTests extends BaseTest {
    private static Project project;
    private static List<TestPlan> testPlanGroupList = new ArrayList<>();
    private static TestPlan allSerialGroup;
    private static TestPlan allParallelGroup;
    private static TestPlan noGroupPlan;

    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanTestService testPlanTestService;
    @Resource
    private TestPlanExecuteService testPlanExecuteService;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    public static final String[] EXECUTE_QUEUE_PREFIX = new String[]{
            "test-plan-batch-execute:", "test-plan-group-execute:", "test-plan-case-type-execute:", "test-plan-collection-execute:"
    };
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private TestPlanService testPlanService;

    @Test
    @Order(1)
    public void initData() throws Exception {
        AddProjectRequest initProject = new AddProjectRequest();
        initProject.setOrganizationId("100001");
        initProject.setName("测试计划执行专用项目");
        initProject.setDescription("建国创建的测试计划执行专用项目");
        initProject.setEnable(true);
        initProject.setUserIds(List.of("admin"));
        project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
        testPlanTestService.resetProjectModule(project, new String[]{"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"});

        for (int i = 0; i < 4; i++) {
            TestPlanCreateRequest request = new TestPlanCreateRequest();
            request.setProjectId(project.getId());
            request.setType(TestPlanConstants.TEST_PLAN_TYPE_GROUP);
            request.setName("testPlanGroupForExecute:" + i);
            request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            MvcResult mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
            String returnStr = mvcResult.getResponse().getContentAsString();
            ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
            String returnId = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();
            testPlanGroupList.add(testPlanMapper.selectByPrimaryKey(returnId));

            if (i == 0) {
                allSerialGroup = testPlanMapper.selectByPrimaryKey(returnId);
                //第一个全部都是串行
                request = new TestPlanCreateRequest();
                request.setProjectId(project.getId());
                request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
                request.setName("testPlanForExecute:" + i);
                request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                request.setGroupId(returnId);
                mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
                returnStr = mvcResult.getResponse().getContentAsString();
                holder = JSON.parseObject(returnStr, ResultHolder.class);
                returnId = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();

                TestPlanConfig testPlanConfig = new TestPlanConfig();
                testPlanConfig.setTestPlanId(returnId);
                testPlanConfig.setCaseRunMode(ApiBatchRunMode.SERIAL.name());
                testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);

                TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
                testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo(returnId);
                List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
                testPlanCollections.forEach(item -> {
                    item.setExecuteMethod(ApiBatchRunMode.SERIAL.name());
                    testPlanCollectionMapper.updateByPrimaryKeySelective(item);
                });

            } else if (i == 1) {
                allParallelGroup = testPlanMapper.selectByPrimaryKey(returnId);

                //第二个全部都是并行
                request = new TestPlanCreateRequest();
                request.setProjectId(project.getId());
                request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
                request.setName("testPlanForExecute:" + i);
                request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                request.setGroupId(returnId);
                mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
                returnStr = mvcResult.getResponse().getContentAsString();
                holder = JSON.parseObject(returnStr, ResultHolder.class);
                returnId = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();


                TestPlanConfig testPlanConfig = new TestPlanConfig();
                testPlanConfig.setTestPlanId(returnId);
                testPlanConfig.setCaseRunMode(ApiBatchRunMode.PARALLEL.name());
                testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);

                TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
                testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo(returnId);
                List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
                testPlanCollections.forEach(item -> {
                    item.setExecuteMethod(ApiBatchRunMode.PARALLEL.name());
                    testPlanCollectionMapper.updateByPrimaryKeySelective(item);
                });
            } else if (i == 2) {
                // 第三个走默认方法
                request = new TestPlanCreateRequest();
                request.setProjectId(project.getId());
                request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
                request.setName("testPlanForExecute:" + i);
                request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                request.setGroupId(returnId);
                mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
                returnStr = mvcResult.getResponse().getContentAsString();
                holder = JSON.parseObject(returnStr, ResultHolder.class);
                returnId = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();
            }
        }

        TestPlanCreateRequest request = new TestPlanCreateRequest();
        request.setProjectId(project.getId());
        request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        request.setName("testPlanForSingleExecute");
        request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        MvcResult mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
        String returnStr = mvcResult.getResponse().getContentAsString();
        ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
        String returnId = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();
        noGroupPlan = testPlanMapper.selectByPrimaryKey(returnId);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @Order(2)
    public void executeTest() throws Exception {
        if (CollectionUtils.isEmpty(testPlanGroupList)) {
            this.initData();
        }

        List<String> batchExecuteIds = new ArrayList<>();
        batchExecuteIds.add(noGroupPlan.getId());
        for (TestPlan group : testPlanGroupList) {
            batchExecuteIds.add(group.getId());
        }

        // 串行4个计划组和一个计划
        this.executeBatch(batchExecuteIds, ApiBatchRunMode.SERIAL.name());
        for (TestPlan group : testPlanGroupList) {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andTestPlanIdEqualTo(group.getId());
//            Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
            testPlanReportMapper.deleteByExample(example);
        }

        //并行4个计划组和一个计划
        this.executeBatch(batchExecuteIds, ApiBatchRunMode.PARALLEL.name());
        for (TestPlan group : testPlanGroupList) {
            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andTestPlanIdEqualTo(group.getId());
            Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
            testPlanReportMapper.deleteByExample(example);
        }

        //单独串行一个计划组
        this.executeOne(allSerialGroup.getId(), ApiBatchRunMode.SERIAL.name());
        TestPlanReportExample example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(allSerialGroup.getId());
//        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
        testPlanReportMapper.deleteByExample(example);

        //单独串行一个计划
        this.executeOne(noGroupPlan.getId(), ApiBatchRunMode.SERIAL.name());
        example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(noGroupPlan.getId());
//        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
        testPlanReportMapper.deleteByExample(example);

        //单独并行一个计划组
        this.executeOne(allParallelGroup.getId(), ApiBatchRunMode.PARALLEL.name());
        example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(allParallelGroup.getId());
        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
        testPlanReportMapper.deleteByExample(example);

        //单独并行一个计划
        this.executeOne(noGroupPlan.getId(), ApiBatchRunMode.PARALLEL.name());
        example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(noGroupPlan.getId());
        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
        testPlanReportMapper.deleteByExample(example);
    }

    private void executeBatch(List<String> execIds, String runMode) throws Exception {

        TestPlanBatchExecuteRequest batchExecuteRequest = new TestPlanBatchExecuteRequest();
        batchExecuteRequest.setExecuteIds(execIds);
        batchExecuteRequest.setProjectId(project.getId());
        batchExecuteRequest.setRunMode(runMode);
        testPlanExecuteService.batchExecuteTestPlan(batchExecuteRequest, "admin");

        //检查队列
        List<String> allQueueIds = new ArrayList<>();
        List<String> collectionQueueIdList = new ArrayList<>();
        for (String executeQueue : EXECUTE_QUEUE_PREFIX) {
            Set<String> keys = stringRedisTemplate.keys("*" + executeQueue + "*");
            allQueueIds.addAll(keys);
            for (String key : keys) {
                if (StringUtils.equalsIgnoreCase(executeQueue, QUEUE_PREFIX_TEST_PLAN_COLLECTION)) {
                    String[] keyArr = key.split(QUEUE_PREFIX_TEST_PLAN_COLLECTION);
                    collectionQueueIdList.add(keyArr[keyArr.length - 1]);
                }
            }
        }

        // todo @建国 测试集执行生成的key有变化
//        this.checkRedisKeyEmpty(allQueueIds, collectionQueueIdList);

    }

    private void executeOne(String id, String runMode) throws Exception {
        TestPlanExecuteRequest executeRequest = new TestPlanExecuteRequest();
        executeRequest.setExecuteId(id);
        executeRequest.setRunMode(runMode);

        testPlanExecuteService.singleExecuteTestPlan(executeRequest, IDGenerator.nextStr(), "admin");

        //检查队列
        List<String> allQueueIds = new ArrayList<>();
        List<String> collectionQueueIdList = new ArrayList<>();
        for (String executeQueue : EXECUTE_QUEUE_PREFIX) {
            Set<String> keys = stringRedisTemplate.keys("*" + executeQueue + "*");
            if (CollectionUtils.isEmpty(keys)) {
                continue;
            }
            allQueueIds.addAll(keys);
            for (String key : keys) {
                if (StringUtils.equalsIgnoreCase(executeQueue, QUEUE_PREFIX_TEST_PLAN_COLLECTION)) {
                    String[] keyArr = key.split(QUEUE_PREFIX_TEST_PLAN_COLLECTION);
                    collectionQueueIdList.add(keyArr[keyArr.length - 1]);
                }
            }
        }
//        this.checkRedisKeyEmpty(allQueueIds, collectionQueueIdList);

    }

    private void checkRedisKeyEmpty(List<String> allQueueIds, List<String> collectionQueueIdList) throws Exception {
        //本条测试用例中，最多传入的是5个批量执行。（4个测试计划组和1个测试计划， 有一个测试计划组下面没有测试计划），
        // 串行的话模拟其中8个测试集的回调。 既while中最多循环8次进行回调，每次回调只传入1个测试集，防止并行/串行的干扰
        int foreachIndex = 8;
        while (foreachIndex > 0 && !allQueueIds.isEmpty()) {

            String collectionFinishQueueIds = collectionQueueIdList.getFirst();
            //模拟执行完成之后的回调
            testPlanExecuteService.collectionExecuteQueueFinish(collectionFinishQueueIds, false);

            allQueueIds = new ArrayList<>();
            collectionQueueIdList = new ArrayList<>();

            for (String executeQueue : EXECUTE_QUEUE_PREFIX) {
                Set<String> keys = stringRedisTemplate.keys("*" + executeQueue + "*");
                allQueueIds.addAll(keys);
                for (String key : keys) {
                    if (StringUtils.equalsIgnoreCase(executeQueue, QUEUE_PREFIX_TEST_PLAN_COLLECTION)) {
                        String[] keyArr = key.split(QUEUE_PREFIX_TEST_PLAN_COLLECTION);
                        collectionQueueIdList.add(keyArr[keyArr.length - 1]);
                    }
                }
            }
            foreachIndex--;
        }

        Assertions.assertTrue(allQueueIds.isEmpty());
    }

    @Test
    @Order(3)
    public void autoFunctionCase() throws Exception {
        if (project == null) {
            this.initData();
        }
        //测试计划关联功能用例 -- 附带把功能用例对应的接口用例关联过来并执行
        //创建测试计划
        TestPlanCreateRequest request = new TestPlanCreateRequest();
        request.setProjectId(project.getId());
        request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        request.setName("testPlanForAutoFunction");
        request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        MvcResult mvcResult = this.requestPostWithOkAndReturn("/test-plan/add", request);
        String returnStr = mvcResult.getResponse().getContentAsString();
        ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
        String testPlanID = JSON.parseObject(JSON.toJSONString(holder.getData()), TestPlan.class).getId();

        TestPlanResourceIds testPlanResourceIds = this.insertFunctionalCase(testPlanID);
        testPlanResourceIds.getReportApiScenarios().forEach(item -> {
            item.setApiScenarioExecuteResult(ResultStatus.SUCCESS.name());
            testPlanReportApiScenarioMapper.updateByPrimaryKeySelective(item);
        });
        testPlanService.autoUpdateFunctionalCase(testPlanResourceIds.getTestPlanReportId());
        TestPlanFunctionalCase testPlanFunctionalCase = testPlanFunctionalCaseMapper.selectByPrimaryKey(testPlanResourceIds.getTestPlanFunctionalCaseId());
        Assertions.assertEquals(testPlanFunctionalCase.getLastExecResult(), ResultStatus.SUCCESS.name());
        for (int i = 0; i < testPlanResourceIds.getReportApiScenarios().size(); i++) {
            TestPlanReportApiScenario reportApiScenario = testPlanResourceIds.getReportApiScenarios().get(i);
            if (i == 0) {
                reportApiScenario.setApiScenarioExecuteResult(ResultStatus.ERROR.name());
                testPlanReportApiScenarioMapper.updateByPrimaryKeySelective(reportApiScenario);
            } else if (i == 1) {
                reportApiScenario.setApiScenarioExecuteResult(ResultStatus.FAKE_ERROR.name());
                testPlanReportApiScenarioMapper.updateByPrimaryKeySelective(reportApiScenario);
            }
        }
        testPlanService.autoUpdateFunctionalCase(testPlanResourceIds.getTestPlanReportId());
        testPlanFunctionalCase = testPlanFunctionalCaseMapper.selectByPrimaryKey(testPlanResourceIds.getTestPlanFunctionalCaseId());
        Assertions.assertEquals(testPlanFunctionalCase.getLastExecResult(), ResultStatus.ERROR.name());

        String lastExecResult = testPlanFunctionalCase.getLastExecResult();
        for (int i = 0; i < testPlanResourceIds.getReportApiScenarios().size(); i++) {
            if (i == 0) {
                TestPlanReportApiScenario reportApiScenario = testPlanResourceIds.getReportApiScenarios().get(i);
                reportApiScenario.setApiScenarioExecuteResult(ResultStatus.BLOCKED.name());
                testPlanReportApiScenarioMapper.updateByPrimaryKeySelective(reportApiScenario);
            }
        }
        testPlanService.autoUpdateFunctionalCase(testPlanResourceIds.getTestPlanReportId());
        testPlanFunctionalCase = testPlanFunctionalCaseMapper.selectByPrimaryKey(testPlanResourceIds.getTestPlanFunctionalCaseId());
        Assertions.assertEquals(testPlanFunctionalCase.getLastExecResult(), lastExecResult);

        for (int i = 0; i < testPlanResourceIds.getReportApiScenarios().size(); i++) {
            TestPlanReportApiScenario reportApiScenario = testPlanResourceIds.getReportApiScenarios().get(i);
            reportApiScenario.setApiScenarioExecuteResult(ResultStatus.FAKE_ERROR.name());
            testPlanReportApiScenarioMapper.updateByPrimaryKeySelective(reportApiScenario);
        }
        testPlanService.autoUpdateFunctionalCase(testPlanResourceIds.getTestPlanReportId());
        testPlanFunctionalCase = testPlanFunctionalCaseMapper.selectByPrimaryKey(testPlanResourceIds.getTestPlanFunctionalCaseId());
        Assertions.assertEquals(testPlanFunctionalCase.getLastExecResult(), ResultStatus.SUCCESS.name());
    }

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    private TestPlanResourceIds insertFunctionalCase(String testPlanId) {
        TestPlanResourceIds returnResourceIds = new TestPlanResourceIds();
        returnResourceIds.setTestPlanId(testPlanId);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(testPlanId);
        testPlanConfig.setAutomaticStatusUpdate(true);
        testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);

        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setId(IDGenerator.nextStr());
        testPlanReport.setTestPlanId(testPlanId);
        testPlanReport.setName(testPlanId);
        testPlanReport.setCreateTime(System.currentTimeMillis());
        testPlanReport.setCreateUser("admin");
        testPlanReport.setStartTime(System.currentTimeMillis());
        testPlanReport.setEndTime(System.currentTimeMillis());
        testPlanReport.setExecStatus("COMPLETED");
        testPlanReport.setResultStatus("PREPARED");
        testPlanReport.setPassRate(100.00);
        testPlanReport.setTriggerMode("API");
        testPlanReport.setPassThreshold(100.00);
        testPlanReport.setProjectId(project.getId());
        testPlanReport.setDeleted(false);
        testPlanReport.setIntegrated(false);
        testPlanReport.setTestPlanName(testPlanId);
        testPlanReport.setDefaultLayout(true);
        testPlanReportMapper.insert(testPlanReport);

        FunctionalCase functionalCase = new FunctionalCase();
        functionalCase.setProjectId(project.getId());
        functionalCase.setName(String.valueOf(System.currentTimeMillis()));
        functionalCase.setId(IDGenerator.nextStr());
        functionalCase.setModuleId("root");
        functionalCase.setTemplateId("root");
        functionalCase.setReviewStatus("PREPARED");
        functionalCase.setCaseEditType("STEP");
        functionalCase.setPos(4096L);
        functionalCase.setVersionId("root");
        functionalCase.setRefId(functionalCase.getId());
        functionalCase.setLastExecuteResult("PREPARED");
        functionalCase.setDeleted(false);
        functionalCase.setPublicCase(false);
        functionalCase.setLatest(true);
        functionalCase.setAiCreate(false);
        functionalCase.setCreateUser("admin");
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCase.setNum(NumGenerator.nextNum(project.getId(), ApplicationNumScope.CASE_MANAGEMENT));
        functionalCaseMapper.insert(functionalCase);

        TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
        testPlanFunctionalCase.setId(IDGenerator.nextStr());
        testPlanFunctionalCase.setTestPlanId(testPlanId);
        testPlanFunctionalCase.setFunctionalCaseId(functionalCase.getId());
        testPlanFunctionalCase.setCreateTime(System.currentTimeMillis());
        testPlanFunctionalCase.setCreateUser("admin");
        testPlanFunctionalCase.setPos(4096L);
        testPlanFunctionalCase.setLastExecResult("SUCCESS");
        testPlanFunctionalCase.setTestPlanCollectionId("root");
        testPlanFunctionalCaseMapper.insert(testPlanFunctionalCase);

        for (int i = 0; i < 4; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId(IDGenerator.nextStr());
            apiScenario.setName(apiScenario.getId());
            apiScenario.setPriority("P1");
            apiScenario.setStatus("PREPARED");
            apiScenario.setStepTotal(1);
            apiScenario.setRequestPassRate("100");
            apiScenario.setNum(100000L);
            apiScenario.setDeleted(false);
            apiScenario.setPos(4096L * (i + 1));
            apiScenario.setVersionId("root");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setLatest(true);
            apiScenario.setProjectId(project.getId());
            apiScenario.setModuleId("root");
            apiScenario.setGrouped(false);
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            apiScenarioMapper.insertSelective(apiScenario);

            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(IDGenerator.nextStr());
            testPlanApiScenario.setTestPlanId(testPlanId);
            testPlanApiScenario.setApiScenarioId(apiScenario.getId());
            testPlanApiScenario.setPos(4096L * (i + 1));
            testPlanApiScenario.setTestPlanCollectionId("root");
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setCreateUser("admin");
            testPlanApiScenarioMapper.insertSelective(testPlanApiScenario);

            FunctionalCaseTest functionalCaseTest = new FunctionalCaseTest();
            functionalCaseTest.setId(IDGenerator.nextStr());
            functionalCaseTest.setCaseId(functionalCase.getId());
            functionalCaseTest.setSourceId(apiScenario.getId());
            functionalCaseTest.setSourceType("SCENARIO");
            functionalCaseTest.setProjectId(project.getId());
            functionalCaseTest.setVersionId(functionalCase.getVersionId());
            functionalCaseTest.setCreateTime(System.currentTimeMillis());
            functionalCaseTest.setUpdateTime(System.currentTimeMillis());
            functionalCaseTest.setCreateUser("admin");
            functionalCaseTest.setUpdateUser("admin");
            functionalCaseTestMapper.insertSelective(functionalCaseTest);

            TestPlanReportApiScenario testPlanReportApiScenario = new TestPlanReportApiScenario();
            testPlanReportApiScenario.setId(IDGenerator.nextStr());
            testPlanReportApiScenario.setTestPlanReportId(testPlanReport.getId());
            testPlanReportApiScenario.setTestPlanCollectionId("root");
            testPlanReportApiScenario.setGrouped(false);
            testPlanReportApiScenario.setTestPlanApiScenarioId(testPlanApiScenario.getId());
            testPlanReportApiScenario.setApiScenarioId(testPlanApiScenario.getApiScenarioId());
            testPlanReportApiScenario.setApiScenarioName(apiScenario.getName());
            testPlanReportApiScenario.setPos(testPlanApiScenario.getPos());
            testPlanReportApiScenario.setApiScenarioNum(apiScenario.getNum());
            testPlanReportApiScenarioMapper.insertSelective(testPlanReportApiScenario);

            returnResourceIds.getReportApiScenarios().add(testPlanReportApiScenario);
        }


        returnResourceIds.setTestPlanFunctionalCaseId(testPlanFunctionalCase.getId());
        returnResourceIds.setTestPlanReportId(testPlanReport.getId());
        return returnResourceIds;
    }
}

@Data
class TestPlanResourceIds {
    private String testPlanId;
    private String testPlanReportId;
    private String testPlanFunctionalCaseId;
    private List<TestPlanReportApiScenario> reportApiScenarios = new ArrayList<>();
}
