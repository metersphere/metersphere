package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.job.TestPlanScheduleJob;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.ExtScheduleMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestPlanTestService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    public TestPlan selectTestPlanByName(String name) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andNameEqualTo(name);
        List<TestPlan> testPlanList = testPlanMapper.selectByExample(testPlanExample);
        if (CollectionUtils.isEmpty(testPlanList)) {
            return null;
        } else {
            return testPlanList.getFirst();
        }
    }
    public void checkTestPlanByAddTest() {
        /*
        抽查：
            testPlan_13没有设置计划开始时间、没有设置重复添加用例和自动更新状态、阈值为100、描述为空；
            testPlan_53检查是否设置了计划开始结束时间；
            testPlan_123是否设置了重复添加用例和自动更新状态；
            testPlan_173的阈值是否不等于100、描述不会为空
         */
        TestPlan testPlan = selectTestPlanByName("testPlan_13");
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_53");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNotNull(testPlan.getPlannedStartTime());
        Assertions.assertNotNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_123");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), true);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), true);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_173");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertNotEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNotNull(testPlan.getDescription());
    }

    public void updateTestPlanTypeToGroup(String[] testPlanIds) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdIn(List.of(testPlanIds));
        TestPlan testPlan = new TestPlan();
        testPlan.setType(TestPlanConstants.TEST_PLAN_TYPE_GROUP);
        testPlanMapper.updateByExampleSelective(testPlan, testPlanExample);
    }

    public List<TestPlan> selectByProjectIdAndNames(String projectId, String[] strings) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId).andNameIn(List.of(strings));
        return testPlanMapper.selectByExample(testPlanExample);
    }

    public boolean checkDataCount(String projectId, int finalCount) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId);
        return testPlanMapper.countByExample(testPlanExample) == finalCount;
    }

    public long countByModuleId(String id) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andModuleIdEqualTo(id);
        return testPlanMapper.countByExample(testPlanExample);
    }

    public List<FunctionalCase> createFunctionCase(int caseNums, String projectId) {
        List<FunctionalCase> returnList = new ArrayList<>();
        for (int i = 0; i < caseNums; i++) {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setId(IDGenerator.nextStr());
            functionalCase.setProjectId(projectId);
            functionalCase.setNum(NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT));
            functionalCase.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            functionalCase.setName("function_" + projectId + "_" + i);
            functionalCase.setReviewStatus("UN_REVIEWED");
            functionalCase.setPos((long) (i * 64));
            functionalCase.setRefId(functionalCase.getId());
            functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
            functionalCase.setLatest(true);
            functionalCase.setCreateUser("admin");
            functionalCase.setCreateTime(System.currentTimeMillis());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            functionalCase.setVersionId("v6.6.6");
            functionalCase.setTemplateId("none");
            functionalCase.setCaseEditType("step");
            functionalCase.setDeleted(false);
            functionalCase.setPublicCase(false);
            returnList.add(functionalCase);
        }
        functionalCaseMapper.batchInsert(returnList);
        return returnList;
    }

    public List<ApiScenario> createApiScenario(int caseNums, String projectId) {
        List<ApiScenario> returnList = new ArrayList<>();
        for (int i = 0; i < caseNums; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId(IDGenerator.nextStr());
            apiScenario.setProjectId(projectId);
            apiScenario.setNum(NumGenerator.nextNum(projectId, ApplicationNumScope.API_SCENARIO));
            apiScenario.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            apiScenario.setName("api_scenario_" + projectId + "_" + i);
            apiScenario.setPriority("P0");
            apiScenario.setVersionId("v6.6.6");
            apiScenario.setPos((long) (i * 64));
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setStatus("Prepared");
            apiScenario.setLatest(true);
            apiScenario.setCreateUser("admin");
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setUpdateUser("admin");
            apiScenario.setDeleted(false);
            apiScenario.setStepTotal(1);
            apiScenario.setRequestPassRate("100");
            apiScenario.setLastReportStatus("UN_EXECUTED");
            returnList.add(apiScenario);
        }
        apiScenarioMapper.batchInsert(returnList);
        return returnList;
    }

    public long countResource(String id, String resourceFunctionalCase) {
        if (StringUtils.equals(TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, resourceFunctionalCase)) {
            TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(id);
            return testPlanFunctionalCaseMapper.countByExample(example);
        } else if (StringUtils.equals(TestPlanResourceConstants.RESOURCE_API_CASE, resourceFunctionalCase)) {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(id);
            return testPlanApiCaseMapper.countByExample(example);
        } else if (StringUtils.equals(TestPlanResourceConstants.RESOURCE_API_SCENARIO, resourceFunctionalCase)) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andTestPlanIdEqualTo(id);
            return testPlanApiScenarioMapper.countByExample(example);
        }
        return 0;
    }

    public List<TestPlanFunctionalCase> selectTestPlanFunctionalCaseByTestPlanId(String testPlanId) {
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        example.setOrderByClause(" pos asc ");
        return testPlanFunctionalCaseMapper.selectByExample(example);
    }

    public List<TestPlanApiCase> selectTestPlanApiCaseByTestPlanId(String testPlanId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        example.setOrderByClause(" pos asc ");
        return testPlanApiCaseMapper.selectByExample(example);
    }

    public List<TestPlanApiScenario> selectTestPlanApiScenarioByTestPlanId(String testPlanId) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        example.setOrderByClause(" pos asc ");
        return testPlanApiScenarioMapper.selectByExample(example);
    }

    public TestPlanConfig selectTestPlanConfigById(String id) {
        return testPlanConfigMapper.selectByPrimaryKey(id);
    }

    public void checkTestPlanUpdateResult(TestPlan testPlan, TestPlanConfig testPlanConfig, TestPlanUpdateRequest updateRequest) {
        TestPlan newTestPlan = testPlanMapper.selectByPrimaryKey(updateRequest.getId());

        if (StringUtils.isNotBlank(updateRequest.getName())) {
            Assertions.assertEquals(newTestPlan.getName(), updateRequest.getName());
        } else {
            Assertions.assertEquals(newTestPlan.getName(), testPlan.getName());
        }
        if (StringUtils.isNotBlank(updateRequest.getModuleId())) {
            Assertions.assertEquals(newTestPlan.getModuleId(), updateRequest.getModuleId());
        } else {
            Assertions.assertEquals(newTestPlan.getModuleId(), testPlan.getModuleId());
        }
        if (CollectionUtils.isNotEmpty(updateRequest.getTags())) {
            Assertions.assertEquals(JSON.toJSONString(newTestPlan.getTags()), JSON.toJSONString(updateRequest.getTags()));
        } else {
            Assertions.assertEquals(JSON.toJSONString(newTestPlan.getTags()), JSON.toJSONString(testPlan.getTags()));
        }

        if (updateRequest.getPlannedStartTime() != null) {
            Assertions.assertEquals(newTestPlan.getPlannedStartTime(), updateRequest.getPlannedStartTime());
        } else {
            Assertions.assertEquals(newTestPlan.getPlannedStartTime(), testPlan.getPlannedStartTime());
        }

        if (updateRequest.getPlannedEndTime() != null) {
            Assertions.assertEquals(newTestPlan.getPlannedEndTime(), updateRequest.getPlannedEndTime());
        } else {
            Assertions.assertEquals(newTestPlan.getPlannedEndTime(), testPlan.getPlannedEndTime());
        }

        if (StringUtils.isNotBlank(updateRequest.getDescription())) {
            Assertions.assertEquals(newTestPlan.getDescription(), updateRequest.getDescription());
        } else {
            Assertions.assertEquals(newTestPlan.getDescription(), testPlan.getDescription());
        }

        if (testPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = testPlanConfigMapper.selectByPrimaryKey(updateRequest.getId());
            if (updateRequest.getAutomaticStatusUpdate() != null) {
                Assertions.assertEquals(newTestPlanConfig.getAutomaticStatusUpdate(), updateRequest.getAutomaticStatusUpdate());
            } else {
                Assertions.assertEquals(newTestPlanConfig.getAutomaticStatusUpdate(), testPlanConfig.getAutomaticStatusUpdate());
            }

            if (updateRequest.getRepeatCase() != null) {
                Assertions.assertEquals(newTestPlanConfig.getRepeatCase(), updateRequest.getRepeatCase());
            } else {
                Assertions.assertEquals(newTestPlanConfig.getRepeatCase(), testPlanConfig.getRepeatCase());
            }

            if (updateRequest.getPassThreshold() != null) {
                Assertions.assertEquals(newTestPlanConfig.getPassThreshold(), updateRequest.getPassThreshold());
            } else {
                Assertions.assertEquals(newTestPlanConfig.getPassThreshold(), testPlanConfig.getPassThreshold());
            }
        }

        if (updateRequest.getGroupId() != null) {
            Assertions.assertEquals(newTestPlan.getGroupId(), updateRequest.getGroupId());
        } else {
            Assertions.assertEquals(newTestPlan.getGroupId(), testPlan.getGroupId());
        }

    }

    public TestPlanUpdateRequest generateUpdateRequest(String testPlanId) {
        TestPlanUpdateRequest updateRequest = new TestPlanUpdateRequest();
        updateRequest.setId(testPlanId);
        return updateRequest;
    }

    public List<ApiTestCase> createApiCases(int caseNums, String projectId) {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId(IDGenerator.nextStr());
        apiDefinition.setName("建国测试用接口");
        apiDefinition.setNum(NumGenerator.nextNum(projectId, ApplicationNumScope.API_DEFINITION));
        apiDefinition.setProtocol("HTTP");
        apiDefinition.setMethod("POST");
        apiDefinition.setPath("/api/def");
        apiDefinition.setStatus("Prepared");
        apiDefinition.setProjectId(projectId);
        apiDefinition.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        apiDefinition.setPos(0L);
        apiDefinition.setLatest(true);
        apiDefinition.setCreateUser("admin");
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateUser("admin");
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setRefId(apiDefinition.getId());
        apiDefinition.setVersionId("v6.6.6");
        apiDefinition.setDeleted(false);
        apiDefinitionMapper.insert(apiDefinition);

        List<ApiTestCase> returnList = new ArrayList<>();
        for (int i = 0; i < caseNums; i++) {
            ApiTestCase apiTestCase = new ApiTestCase();
            apiTestCase.setId(IDGenerator.nextStr());
            apiTestCase.setApiDefinitionId(apiDefinition.getId());
            apiTestCase.setProjectId(projectId);
            apiTestCase.setNum(NumGenerator.nextNum(projectId + "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
            apiTestCase.setName(apiDefinition.getName() + "_test-case_" + i);
            apiTestCase.setPriority("P0");
            apiTestCase.setPos((long) (i * 64));
            apiTestCase.setStatus("Prepared");
            apiTestCase.setVersionId("v6.6.6");
            apiTestCase.setDeleted(false);
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setCreateUser("admin");
            apiTestCase.setUpdateUser("admin");
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setLastReportStatus("UN_EXECUTED");
            returnList.add(apiTestCase);
        }
        apiTestCaseMapper.batchInsert(returnList);
        return returnList;
    }

    public void removeProjectModule(Project project, String[] projectModule, String removeModule) {
        ArrayList<String> moduleList = new ArrayList<>(List.of(projectModule));
        moduleList.remove(removeModule);

        Project updateProject = new Project();
        updateProject.setId(project.getId());
        updateProject.setModuleSetting(JSON.toJSONString(moduleList));
        projectMapper.updateByPrimaryKeySelective(updateProject);
    }

    public void resetProjectModule(Project project, String[] projectModule) {
        ArrayList<String> moduleList = new ArrayList<>(List.of(projectModule));
        Project updateProject = new Project();
        updateProject.setId(project.getId());
        updateProject.setModuleSetting(JSON.toJSONString(moduleList));
        projectMapper.updateByPrimaryKeySelective(updateProject);
    }

    public void setResourcePos(String id, String resourceType, long pos) {
        if (StringUtils.equals(resourceType, TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE)) {
            TestPlanFunctionalCase updateCase = new TestPlanFunctionalCase();
            updateCase.setId(id);
            updateCase.setPos(pos);
            testPlanFunctionalCaseMapper.updateByPrimaryKeySelective(updateCase);
        } else if (StringUtils.equals(resourceType, TestPlanResourceConstants.RESOURCE_API_CASE)) {
            TestPlanApiCase updateCase = new TestPlanApiCase();
            updateCase.setId(id);
            updateCase.setPos(pos);
            testPlanApiCaseMapper.updateByPrimaryKeySelective(updateCase);
        } else if (StringUtils.equals(resourceType, TestPlanResourceConstants.RESOURCE_API_SCENARIO)) {
            TestPlanApiScenario updateCase = new TestPlanApiScenario();
            updateCase.setId(id);
            updateCase.setPos(pos);
            testPlanApiScenarioMapper.updateByPrimaryKeySelective(updateCase);
        }
    }

    public void checkDataEmpty(List<String> testPlanIdList, String projectId) {
        SubListUtils.dealForSubList(testPlanIdList, SubListUtils.DEFAULT_BATCH_SIZE, (subList) -> {
            TestPlanExample testPlanExample = new TestPlanExample();
            testPlanExample.createCriteria().andIdIn(subList);
            Assertions.assertEquals(testPlanMapper.countByExample(testPlanExample), 0);

            TestPlanModuleExample testPlanModuleExample = new TestPlanModuleExample();
            testPlanModuleExample.createCriteria().andProjectIdEqualTo(projectId);
            Assertions.assertEquals(testPlanMapper.countByExample(testPlanExample), 0);

            TestPlanConfigExample testPlanConfigExample = new TestPlanConfigExample();
            testPlanConfigExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanConfigMapper.countByExample(testPlanConfigExample), 0);

            TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
            testPlanFunctionalCaseExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanFunctionalCaseMapper.countByExample(testPlanFunctionalCaseExample), 0);

            TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
            testPlanApiCaseExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanApiCaseMapper.countByExample(testPlanApiCaseExample), 0);

            TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanApiScenarioMapper.countByExample(testPlanApiScenarioExample), 0);

            TestPlanFollowerExample testPlanFollowerExample = new TestPlanFollowerExample();
            testPlanFollowerExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanFollowerMapper.countByExample(testPlanFollowerExample), 0);

            TestPlanReportExample testPlanReportExample = new TestPlanReportExample();
            testPlanReportExample.createCriteria().andTestPlanIdIn(subList);
            Assertions.assertEquals(testPlanReportMapper.countByExample(testPlanReportExample), 0);


        });
    }

    public List<TestPlanResponse> getTestPlanResponse(String returnData) {
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<Object> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        List<TestPlanResponse> returnList = JSON.parseArray(JSON.toJSONString(result.getList()), TestPlanResponse.class);
        return returnList;
    }

    public void checkTestPlanPage(String returnData, long current, long pageSize, long allData) {
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<Object> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(result.getCurrent(), current);
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= pageSize);
        if (allData > 0) {
            Assertions.assertEquals(result.getTotal(), allData);
        }
    }

    @Resource
    private ExtScheduleMapper extScheduleMapper;

    @Resource
    private Scheduler scheduler;

    /*
    校验定时任务是否成功开启：
        1.schedule表中存在数据，且开启状态符合isEnable
        2.开启状态下：    qrtz_triggers 和 qrtz_cron_triggers 表存在对应的数据
        3.关闭状态下：    qrtz_triggers 和 qrtz_cron_triggers 表不存在对应的数据
     */
    public void checkSchedule(String scheduleId, String resourceId, boolean isEnable) throws Exception {
        Assertions.assertEquals(extScheduleMapper.countByIdAndEnable(scheduleId, isEnable), 1L);
        Assertions.assertEquals(scheduler.checkExists(TestPlanScheduleJob.getJobKey(resourceId)), isEnable);
    }

    public void checkSchedule(String resourceId, boolean isEnable) throws Exception {
        Assertions.assertEquals(extScheduleMapper.countByResourceId(resourceId), 1L);
        Assertions.assertEquals(scheduler.checkExists(TestPlanScheduleJob.getJobKey(resourceId)), isEnable);
    }
    
    public void checkScheduleIsRemove(String resourceId) throws SchedulerException {
        Assertions.assertEquals(extScheduleMapper.countByResourceId(resourceId), 0L);
        Assertions.assertEquals(scheduler.checkExists(TestPlanScheduleJob.getJobKey(resourceId)), false);
    }
}
