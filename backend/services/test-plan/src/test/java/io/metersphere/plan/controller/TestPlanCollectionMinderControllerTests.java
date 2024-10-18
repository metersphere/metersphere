package io.metersphere.plan.controller;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderEditDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.dto.request.TestPlanCollectionMinderEditRequest;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.ModuleSelectDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanCollectionMinderControllerTests extends BaseTest {

    private static final String PLAN_MIND = "/test-plan/mind/data/";

    private static final String EDIT_MIND = "/test-plan/mind/data/edit";

    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_mind.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void tesPagePlanReportSuccess() throws Exception {
        Project initProject = new Project();
        initProject.setId("GYQALLPOOL");
        initProject.setNum(null);
        initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        initProject.setName("测试项目版本");
        initProject.setDescription("测试项目版本");
        initProject.setCreateUser("admin");
        initProject.setUpdateUser("admin");
        initProject.setCreateTime(System.currentTimeMillis());
        initProject.setUpdateTime(System.currentTimeMillis());
        initProject.setEnable(true);
        initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
        initProject.setAllResourcePool(true);
        projectMapper.insertSelective(initProject);

        TestPlan testPlan = new TestPlan();
        testPlan.setId("gyq_plan_1");
        testPlan.setProjectId("GYQALLPOOL");
        testPlanMapper.updateByPrimaryKeySelective(testPlan);

        MvcResult mvcResult = this.requestGetWithOkAndReturn(PLAN_MIND + "gyq_plan_1");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<TestPlanCollectionMinderTreeDTO> testPlanCollectionMinderTreeDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestPlanCollectionMinderTreeDTO.class);
        // 返回值不为空
        Assertions.assertNotNull(testPlanCollectionMinderTreeDTOS);

        testPlan.setId("gyq_plan_1");
        testPlan.setProjectId("gyq_plan_project");
        testPlanMapper.updateByPrimaryKeySelective(testPlan);

        mvcResult = this.requestGetWithOkAndReturn(PLAN_MIND + "gyq_plan_1");
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        testPlanCollectionMinderTreeDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestPlanCollectionMinderTreeDTO.class);
        // 返回值不为空
        Assertions.assertNotNull(testPlanCollectionMinderTreeDTOS);
    }

    @Test
    @Order(2)
    void editMind() throws Exception {
        TestPlanCollectionMinderEditRequest request = new TestPlanCollectionMinderEditRequest();
        request.setPlanId("gyq_plan_1");
        List<TestPlanCollectionMinderEditDTO> parentList = new ArrayList<>();
        TestPlanCollectionMinderEditDTO funcParent = new TestPlanCollectionMinderEditDTO();
        funcParent.setId("gyq_wxxx_2");
        funcParent.setText("功能用例");
        funcParent.setNum(2L);
        funcParent.setExecuteMethod("PARALLEL");
        funcParent.setType("FUNCTIONAL");
        funcParent.setExtended(false);
        funcParent.setGrouped(false);
        funcParent.setLevel(1);
        funcParent.setEnvironmentId("gyq_123");
        funcParent.setTestResourcePoolId("gyq_123_pool");
        funcParent.setRetryOnFail(true);
        funcParent.setRetryType("SCENARIO");
        funcParent.setRetryTimes(5);
        funcParent.setRetryInterval(1000);
        funcParent.setStopOnFail(true);
        parentList.add(funcParent);
        TestPlanCollectionMinderEditDTO apiParent = new TestPlanCollectionMinderEditDTO();
        apiParent.setId("gyq_wxxx_1");
        apiParent.setText("接口用例");
        apiParent.setNum(1L);
        apiParent.setExecuteMethod("PARALLEL");
        apiParent.setType("API");
        apiParent.setExtended(false);
        apiParent.setGrouped(false);
        apiParent.setLevel(1);
        apiParent.setEnvironmentId("NONE");
        apiParent.setTestResourcePoolId("gyq_123_pool");
        apiParent.setRetryOnFail(true);
        apiParent.setRetryType("SCENARIO");
        apiParent.setRetryTimes(5);
        apiParent.setRetryInterval(1000);
        apiParent.setStopOnFail(true);
        parentList.add(apiParent);

        TestPlanCollectionMinderEditDTO scenarioParent = new TestPlanCollectionMinderEditDTO();
        scenarioParent.setId("gyq_wxxx_3");
        scenarioParent.setText("场景用例");
        scenarioParent.setNum(3L);
        scenarioParent.setExecuteMethod("PARALLEL");
        scenarioParent.setType("SCENARIO");
        scenarioParent.setExtended(false);
        scenarioParent.setGrouped(false);
        scenarioParent.setLevel(1);
        scenarioParent.setEnvironmentId("NONE");
        scenarioParent.setTestResourcePoolId("gyq_123_pool");
        scenarioParent.setRetryOnFail(true);
        scenarioParent.setRetryType("SCENARIO");
        scenarioParent.setRetryTimes(6);
        scenarioParent.setRetryInterval(1000);
        scenarioParent.setStopOnFail(true);
        parentList.add(scenarioParent);

        List<TestPlanCollectionMinderEditDTO> editList = new ArrayList<>();
        TestPlanCollectionMinderEditDTO deleteDTO = new TestPlanCollectionMinderEditDTO();
        TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO = new TestPlanCollectionMinderEditDTO();
        testPlanCollectionMinderEditDTO.setId("gyq_wxxx_4");
        testPlanCollectionMinderEditDTO.setText("更新名称");
        testPlanCollectionMinderEditDTO.setNum(500L);
        testPlanCollectionMinderEditDTO.setExecuteMethod("PARALLEL");
        testPlanCollectionMinderEditDTO.setType("API");
        testPlanCollectionMinderEditDTO.setExtended(true);
        testPlanCollectionMinderEditDTO.setGrouped(false);
        testPlanCollectionMinderEditDTO.setLevel(2);
        testPlanCollectionMinderEditDTO.setEnvironmentId("gyq_123");
        testPlanCollectionMinderEditDTO.setTestResourcePoolId("gyq_123_pool");
        testPlanCollectionMinderEditDTO.setRetryOnFail(true);
        testPlanCollectionMinderEditDTO.setRetryType("SCENARIO");
        testPlanCollectionMinderEditDTO.setRetryTimes(5);
        testPlanCollectionMinderEditDTO.setRetryInterval(1000);
        testPlanCollectionMinderEditDTO.setStopOnFail(true);
        List<TestPlanCollectionAssociateDTO> associateDTOS = new ArrayList<>();
        TestPlanCollectionAssociateDTO testPlanCollectionAssociateDTO = new TestPlanCollectionAssociateDTO();
        testPlanCollectionAssociateDTO.setAssociateType("API_CASE");
        testPlanCollectionAssociateDTO.setSelectAllModule(true);
        testPlanCollectionAssociateDTO.setProjectId("gyq_plan_project");
        testPlanCollectionAssociateDTO.setModuleMaps(getModuleMaps());
        associateDTOS.add(testPlanCollectionAssociateDTO);
        testPlanCollectionMinderEditDTO.setAssociateDTOS(associateDTOS);
        deleteDTO = testPlanCollectionMinderEditDTO;
        editList.add(testPlanCollectionMinderEditDTO);
        editList.addAll(parentList);
        request.setEditList(editList);
        this.requestPostWithOkAndReturn(EDIT_MIND, request);
        TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo("gyq_plan_1");
        List<TestPlanCollection> testPlanCollections1 = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertEquals(4, testPlanCollections1.size());
        TestPlanCollection testPlanCollection = testPlanCollectionMapper.selectByPrimaryKey("gyq_wxxx_4");
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(testPlanCollection.getName(), "更新名称"));

        Assertions.assertTrue(StringUtils.equalsIgnoreCase(testPlanCollection.getEnvironmentId(), "NONE"));
        editList = new ArrayList<>();
        editList.addAll(parentList);
        testPlanCollectionMinderEditDTO = new TestPlanCollectionMinderEditDTO();
        testPlanCollectionMinderEditDTO.setId(UUID.randomUUID().toString());
        testPlanCollectionMinderEditDTO.setTempCollectionNode(true);
        testPlanCollectionMinderEditDTO.setText("新建名称");
        testPlanCollectionMinderEditDTO.setNum(500L);
        testPlanCollectionMinderEditDTO.setLevel(2);
        testPlanCollectionMinderEditDTO.setExecuteMethod("PARALLEL");
        testPlanCollectionMinderEditDTO.setType("API");
        testPlanCollectionMinderEditDTO.setExtended(false);
        testPlanCollectionMinderEditDTO.setGrouped(false);
        testPlanCollectionMinderEditDTO.setEnvironmentId("gyq_123");
        testPlanCollectionMinderEditDTO.setTestResourcePoolId("gyq_123_pool");
        testPlanCollectionMinderEditDTO.setRetryOnFail(true);
        testPlanCollectionMinderEditDTO.setRetryType("SCENARIO");
        testPlanCollectionMinderEditDTO.setRetryTimes(5);
        testPlanCollectionMinderEditDTO.setRetryInterval(1000);
        testPlanCollectionMinderEditDTO.setStopOnFail(true);
        associateDTOS = new ArrayList<>();
        testPlanCollectionAssociateDTO = new TestPlanCollectionAssociateDTO();
        testPlanCollectionAssociateDTO.setAssociateType("API_CASE");
        testPlanCollectionAssociateDTO.setSelectAllModule(true);
        testPlanCollectionAssociateDTO.setProjectId("gyq_plan_project");
        testPlanCollectionAssociateDTO.setModuleMaps(getModuleMaps());
        associateDTOS.add(testPlanCollectionAssociateDTO);
        testPlanCollectionMinderEditDTO.setAssociateDTOS(associateDTOS);
        editList.add(testPlanCollectionMinderEditDTO);
        request.setEditList(editList);
        this.requestPostWithOkAndReturn(EDIT_MIND, request);
        testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andNameEqualTo("新建名称");
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(testPlanCollections));
        editList = new ArrayList<>(parentList);
        editList.add(deleteDTO);
        editList.add(deleteDTO);
        request.setEditList(editList);
        this.requestPost(EDIT_MIND, request).andExpect(status().is5xxServerError());
        ;
        testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andNameEqualTo("新建名称");
        testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertFalse(CollectionUtils.isEmpty(testPlanCollections));
        testPlanCollectionMinderEditDTO = new TestPlanCollectionMinderEditDTO();
        testPlanCollectionMinderEditDTO.setId(testPlanCollections.getFirst().getId());
        testPlanCollectionMinderEditDTO.setText("hahaha");
        testPlanCollectionMinderEditDTO.setNum(500L);
        testPlanCollectionMinderEditDTO.setExecuteMethod("PARALLEL");
        testPlanCollectionMinderEditDTO.setType("API");
        testPlanCollectionMinderEditDTO.setLevel(2);
        testPlanCollectionMinderEditDTO.setExtended(false);
        testPlanCollectionMinderEditDTO.setGrouped(false);
        testPlanCollectionMinderEditDTO.setEnvironmentId("gyq_123");
        testPlanCollectionMinderEditDTO.setTestResourcePoolId("gyq_123_pool");
        testPlanCollectionMinderEditDTO.setRetryOnFail(true);
        testPlanCollectionMinderEditDTO.setRetryType("SCENARIO");
        testPlanCollectionMinderEditDTO.setRetryTimes(5);
        testPlanCollectionMinderEditDTO.setRetryInterval(1000);
        testPlanCollectionMinderEditDTO.setStopOnFail(true);
        editList = new ArrayList<>(parentList);
        editList.add(testPlanCollectionMinderEditDTO);
        request.setEditList(editList);
        this.requestPostWithOkAndReturn(EDIT_MIND, request);
        testPlanCollectionExample.createCriteria().andNameEqualTo("新建名称");
        testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(testPlanCollections));
    }

    private Map<String, ModuleSelectDTO> getModuleMaps() {
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(true);
        moduleSelectDTO.setSelectIds(List.of("gyq_plan_api-case-associate-1"));
        return Map.of("testmodule", moduleSelectDTO);
    }


}
