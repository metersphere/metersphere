package io.metersphere.plan.controller;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.plan.service.TestPlanExecuteService;
import io.metersphere.plan.service.TestPlanTestService;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String URL_POST_TEST_PLAN_SINGLE_EXECUTE = "/test-plan-execute/single";
    private static final String URL_POST_TEST_PLAN_BATCH_EXECUTE = "/test-plan-execute/batch";

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
    @Autowired
    private TestPlanReportMapper testPlanReportMapper;

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
            Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
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
        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
        testPlanReportMapper.deleteByExample(example);

        //单独串行一个计划
        this.executeOne(noGroupPlan.getId(), ApiBatchRunMode.SERIAL.name());
        example = new TestPlanReportExample();
        example.createCriteria().andTestPlanIdEqualTo(noGroupPlan.getId());
        Assertions.assertTrue(testPlanReportMapper.countByExample(example) > 0);
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

        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_EXECUTE, batchExecuteRequest);

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

        // 待补充
//        Assertions.assertTrue(!collectionQueueIdList.isEmpty());
//        Assertions.assertTrue(!allQueueIds.isEmpty());

        this.checkRedisKeyEmpty(allQueueIds, collectionQueueIdList);

    }

    private void executeOne(String id, String runMode) throws Exception {
        TestPlanExecuteRequest executeRequest = new TestPlanExecuteRequest();
        executeRequest.setExecuteId(id);
        executeRequest.setRunMode(runMode);

        this.requestPostWithOk(URL_POST_TEST_PLAN_SINGLE_EXECUTE, executeRequest);

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
        this.checkRedisKeyEmpty(allQueueIds, collectionQueueIdList);

    }

    private void checkRedisKeyEmpty(List<String> allQueueIds, List<String> collectionQueueIdList) throws Exception {
        //本条测试用例中，最多传入的是5个批量执行。（4个测试计划组和1个测试计划， 有一个测试计划组下面没有测试计划），
        // 串行的话模拟其中8个测试集的回调。 既while中最多循环8次进行回调，每次回调只传入1个测试集，防止并行/串行的干扰
        int foreachIndex = 8;
        while (foreachIndex > 0 && !allQueueIds.isEmpty()) {

            String collectionFinishQueueIds = collectionQueueIdList.getFirst();
            //模拟执行完成之后的回调
            testPlanExecuteService.collectionExecuteQueueFinish(collectionFinishQueueIds);

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
}
