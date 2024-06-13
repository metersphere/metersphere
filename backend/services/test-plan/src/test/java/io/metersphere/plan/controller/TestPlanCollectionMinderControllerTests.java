package io.metersphere.plan.controller;

import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderEditDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.dto.request.TestPlanCollectionMinderEditRequest;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanCollectionMinderControllerTests extends BaseTest {

    private static final String PLAN_MIND = "/test-plan/mind/data/";

    private static final String EDIT_MIND = "/test-plan/mind/data/edit";

    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_mind.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void tesPagePlanReportSuccess() throws Exception {

        MvcResult mvcResult = this.requestGetWithOkAndReturn(PLAN_MIND+"gyq_plan_1");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<TestPlanCollectionMinderTreeDTO> testPlanCollectionMinderTreeDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestPlanCollectionMinderTreeDTO.class);
        // 返回值不为空
        Assertions.assertNotNull(testPlanCollectionMinderTreeDTOS);
    }

    @Test
    @Order(2)
    void editMind() throws Exception {
        TestPlanCollectionMinderEditRequest request = new TestPlanCollectionMinderEditRequest();
        request.setPlanId("gyq_plan_1");
        List<TestPlanCollectionMinderEditDTO> editList = new ArrayList<>();
        TestPlanCollectionMinderEditDTO testPlanCollectionMinderEditDTO = new TestPlanCollectionMinderEditDTO();
        testPlanCollectionMinderEditDTO.setId("gyq_wxxx_4");
        testPlanCollectionMinderEditDTO.setName("更新名称");
        testPlanCollectionMinderEditDTO.setNum(500L);
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
        List<TestPlanCollectionAssociateDTO>associateDTOS = new ArrayList<>();
        TestPlanCollectionAssociateDTO testPlanCollectionAssociateDTO = new TestPlanCollectionAssociateDTO();
        testPlanCollectionAssociateDTO.setAssociateType("API_CASE");
        testPlanCollectionAssociateDTO.setIds(List.of("gyq_plan_api-case-associate-1"));
        associateDTOS.add(testPlanCollectionAssociateDTO);
        testPlanCollectionMinderEditDTO.setAssociateDTOS(associateDTOS);
        editList.add(testPlanCollectionMinderEditDTO);
        request.setEditList(editList);
        this.requestPostWithOkAndReturn(EDIT_MIND, request);

        TestPlanCollection testPlanCollection = testPlanCollectionMapper.selectByPrimaryKey("gyq_wxxx_4");
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(testPlanCollection.getName(),"更新名称"));
        editList = new ArrayList<>();
        testPlanCollectionMinderEditDTO = new TestPlanCollectionMinderEditDTO();
        testPlanCollectionMinderEditDTO.setId(null);
        testPlanCollectionMinderEditDTO.setName("新建名称");
        testPlanCollectionMinderEditDTO.setNum(500L);
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
        testPlanCollectionAssociateDTO.setIds(List.of("gyq_plan_api-case-associate-1"));
        associateDTOS.add(testPlanCollectionAssociateDTO);
        testPlanCollectionMinderEditDTO.setAssociateDTOS(associateDTOS);
        editList.add(testPlanCollectionMinderEditDTO);
        request.setEditList(editList);
        this.requestPostWithOkAndReturn(EDIT_MIND, request);
        TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andNameEqualTo("新建名称");
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(testPlanCollections));
        request.setEditList(new ArrayList<>());
        request.setDeletedIds(List.of(testPlanCollections.get(0).getId()));
        this.requestPostWithOkAndReturn(EDIT_MIND, request);
        testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andNameEqualTo("新建名称");
        testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(testPlanCollections));
    }


}
