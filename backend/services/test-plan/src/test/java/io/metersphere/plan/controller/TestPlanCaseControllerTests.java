package io.metersphere.plan.controller;

import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.domain.BugRelationCaseExample;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.system.dto.ModuleSelectDTO;
import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.mapper.TestPlanCaseExecuteHistoryMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanCaseControllerTests extends BaseTest {

    public static final String FUNCTIONAL_CASE_LIST_URL = "/test-plan/functional/case/page";
    public static final String FUNCTIONAL_CASE_TREE_URL = "/test-plan/functional/case/tree";
    public static final String FUNCTIONAL_CASE_TREE_COUNT_URL = "/test-plan/functional/case/module/count";
    public static final String FUNCTIONAL_CASE_DISASSOCIATE_URL = "/test-plan/functional/case/disassociate";
    public static final String FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL = "/test-plan/functional/case/batch/disassociate";


    public static final String FUNCTIONAL_CASE_RUN_URL = "/test-plan/functional/case/run";
    public static final String FUNCTIONAL_CASE_BATCH_RUN_URL = "/test-plan/functional/case/batch/run";
    public static final String FUNCTIONAL_CASE_BATCH_UPDATE_EXECUTOR_URL = "/test-plan/functional/case/batch/update/executor";
    public static final String FUNCTIONAL_CASE_DETAIL = "/test-plan/functional/case/detail/";


    public static final String FUNCTIONAL_CASE_EXEC_HISTORY_URL = "/test-plan/functional/case/exec/history";
    public static final String USER_URL = "/test-plan/functional/case/user-option/";
    public static final String FUNCTIONAL_CASE_BATCH_MOVE_URL = "/test-plan/functional/case/batch/move";
    public static final String FUNCTIONAL_CASE_BATCH_ADD_BUG_URL = "/test-plan/functional/case/batch/add-bug";
    public static final String FUNCTIONAL_CASE_BATCH_ASSOCIATE_BUG_URL = "/test-plan/functional/case/batch/associate-bug";

    public static final String FUNCTIONAL_CASE_MINDER_BATCH_ADD_BUG = "/test-plan/functional/case/minder/batch/add-bug";
    public static final String FUNCTIONAL_CASE_MINDER_BATCH_ASSOCIATE_BUG = "/test-plan/functional/case/minder/batch/associate-bug";
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    BaseAssociateBugProvider baseAssociateBugProvider;
    @Resource
    BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    FunctionalCaseBlobMapper functionalCaseBlobMapper;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_case_relate_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetFunctionalCaseList() throws Exception {
        TestPlanCaseRequest request = new TestPlanCaseRequest();
        request.setProjectId("123");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("plan_1");
        this.requestPost(FUNCTIONAL_CASE_LIST_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(2)
    public void testGetFunctionalCaseTree() throws Exception {
        TestPlanTreeRequest request = new TestPlanTreeRequest();
        request.setTestPlanId("plan_1");
        request.setTreeType("MODULE");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_URL, request);
        request.setTestPlanId("plan_2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTestPlanId("plan_2");
        request.setTreeType("COLLECTION");
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_URL, request);
        String returnData1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder1 = JSON.parseObject(returnData1, ResultHolder.class);
        Assertions.assertNotNull(resultHolder1);
    }

    @Test
    @Order(3)
    public void testGetFunctionalCaseTreeCount() throws Exception {
        TestPlanCaseModuleRequest request = new TestPlanCaseModuleRequest();
        request.setProjectId("123");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("plan_1");
        request.setTreeType("MODULE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_COUNT_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTreeType("COLLECTION");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_COUNT_URL, request);
    }


    @Test
    @Order(4)
    void disassociate() throws Exception {
        TestPlanDisassociationRequest request = new TestPlanDisassociationRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setId("gyq_disassociate_case_3");
        this.requestPostWithOk(FUNCTIONAL_CASE_DISASSOCIATE_URL, request);
    }

    @Test
    @Order(5)
    public void disassociateBatch() throws Exception {
        BasePlanCaseBatchRequest request = new BasePlanCaseBatchRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setSelectAll(true);
        request.setExcludeIds(List.of("gyq_disassociate_case_2"));
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL, request);
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andTestPlanIdEqualTo("gyq_disassociate_plan_1");
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        Assertions.assertEquals(1, testPlanFunctionalCases.size());
        request = new BasePlanCaseBatchRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_disassociate_case_2"));
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL, request);
        testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        Assertions.assertEquals(0, testPlanFunctionalCases.size());
    }

    @Test
    @Order(5)
    public void getAssociateBugList() throws Exception {
        BugPageProviderRequest request = new BugPageProviderRequest();
        request.setSourceId("test_plan_case_id");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        BugProviderDTO bugProviderDTO = new BugProviderDTO();
        bugProviderDTO.setName("第二个");
        List<BugProviderDTO> operations = new ArrayList<>();
        operations.add(bugProviderDTO);
        Mockito.when(baseAssociateBugProvider.getBugList("bug_relation_case", "test_plan_case_id", "bug_id", request)).thenReturn(operations);
        this.requestPostWithOkAndReturn("/test-plan/functional/case/associate/bug/page", request);
    }

    @Test
    @Order(9)
    public void testAssociateBugs() throws Exception {
        TestPlanCaseAssociateBugRequest request = new TestPlanCaseAssociateBugRequest();
        request.setCaseId("fc_1");
        request.setTestPlanCaseId("relate_case_1");
        request.setTestPlanId("plan_1");
        request.setProjectId(DEFAULT_PROJECT_ID);
        List<String> ids = new ArrayList<>();
        ids.add("bug_1");
        Mockito.when(baseAssociateBugProvider.getSelectBugs(request, false)).thenReturn(ids);
        this.requestPostWithOkAndReturn("/test-plan/functional/case/associate/bug", request);
        AssociateBugPageRequest associateBugPageRequest = new AssociateBugPageRequest();
        associateBugPageRequest.setProjectId(DEFAULT_PROJECT_ID);
        associateBugPageRequest.setCurrent(1);
        associateBugPageRequest.setPageSize(10);
        associateBugPageRequest.setTestPlanCaseId("relate_case_1");
        this.requestPostWithOkAndReturn("/test-plan/functional/case/has/associate/bug/page", associateBugPageRequest);
        ids.add("bug_5");
        this.requestPostWithOkAndReturn("/test-plan/functional/case/associate/bug", request);


    }

    @Test
    @Order(10)
    public void testDisassociateBug() throws Exception {
        BugRelationCaseExample bugRelationCaseExample = new BugRelationCaseExample();
        bugRelationCaseExample.createCriteria().andTestPlanCaseIdEqualTo("relate_case_1").andTestPlanIdEqualTo("plan_1");
        List<BugRelationCase> bugRelationCases = bugRelationCaseMapper.selectByExample(bugRelationCaseExample);
        this.requestGetWithOk("/test-plan/functional/case/disassociate/bug/" + bugRelationCases.getFirst().getId());
    }


    @Test
    @Order(11)
    public void testFunctionalCaseRun() throws Exception {
        TestPlanCaseRunRequest request = new TestPlanCaseRunRequest();
        request.setProjectId("1234");
        request.setId("relate_case_3");
        request.setTestPlanId("plan_2");
        request.setCaseId("fc_1");
        request.setLastExecResult("SUCCESS");
        request.setStepsExecResult("123");
        request.setContent("12334");
        request.setNotifier("123");
        this.requestPostWithOk(FUNCTIONAL_CASE_RUN_URL, request);
        request.setLastExecResult("ERROR");
        this.requestPostWithOk(FUNCTIONAL_CASE_RUN_URL, request);

    }


    @Test
    @Order(12)
    public void testFunctionalCaseBatchRun() throws Exception {
        TestPlanCaseBatchRunRequest request = new TestPlanCaseBatchRunRequest();
        request.setProjectId("1234");
        request.setTestPlanId("plan_2");
        request.setLastExecResult("SUCCESS");
        request.setContent("12334");
        request.setNotifier("123");
        request.setSelectAll(true);
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_RUN_URL, request);
        request.setSelectAll(false);
        request.setSelectIds(List.of("relate_case_3"));
        request.setLastExecResult("ERROR");
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_RUN_URL, request);

    }


    @Test
    @Order(13)
    public void testBatchUpdateExecutor() throws Exception {
        TestPlanCaseUpdateRequest request = new TestPlanCaseUpdateRequest();
        request.setUserId("test_user");
        request.setTestPlanId("plan_4");
        request.setSelectAll(true);
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_UPDATE_EXECUTOR_URL, request);
        request.setTestPlanId("plan_2");
        request.setSelectAll(false);
        request.setSelectIds(List.of("relate_case_3"));
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_UPDATE_EXECUTOR_URL, request);

    }


    @Test
    @Order(14)
    public void testExecHistory() throws Exception {
        TestPlanCaseExecHistoryRequest request = new TestPlanCaseExecHistoryRequest();
        request.setId("relate_case_1");
        request.setTestPlanId("plan_1");
        request.setCaseId("fc_1");
        this.requestPostWithOk(FUNCTIONAL_CASE_EXEC_HISTORY_URL, request);
        TestPlanCaseExecuteHistory testPlanCaseExecuteHistory = new TestPlanCaseExecuteHistory();
        testPlanCaseExecuteHistory.setId("123445");
        FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setId(UUID.randomUUID().toString());
        functionalCaseStepDTO.setNum(1);
        functionalCaseStepDTO.setDesc("步骤描述");
        functionalCaseStepDTO.setResult("结果");
        List<FunctionalCaseStepDTO> list = new ArrayList<>();
        list.add(functionalCaseStepDTO);
        testPlanCaseExecuteHistory.setSteps(JSON.toJSONString(list).getBytes());
        testPlanCaseExecuteHistoryMapper.updateByPrimaryKeySelective(testPlanCaseExecuteHistory);
        this.requestPostWithOk(FUNCTIONAL_CASE_EXEC_HISTORY_URL, request);
    }

    @Test
    @Order(16)
    public void testGetDetail() throws Exception {
        this.requestGet(FUNCTIONAL_CASE_DETAIL + "relate_case_1").andExpect(status().is5xxServerError());
        MvcResult mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL + "gyq_disassociate_case_4");
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCaseDetailDTO.class);
        Assertions.assertTrue(CollectionUtils.isEmpty(JSON.parseArray(functionalCaseDetailDTO.getSteps(), FunctionalCaseDetailDTO.class)));
        FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setId("id_step");
        functionalCaseStepDTO.setNum(1);
        functionalCaseStepDTO.setDesc("步骤一");
        functionalCaseStepDTO.setResult("步骤一结果");
        functionalCaseStepDTO.setActualResult("步骤一实际结果");
        functionalCaseStepDTO.setExecuteResult("SUCCESS");
        String jsonString = JSON.toJSONString(functionalCaseStepDTO);
        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId("gyq_disassociate_fc_4");
        functionalCaseBlob.setSteps(jsonString.getBytes());
        String textDescription = "textDescription";
        String expectedResult = "expectedResult";
        String prerequisite = "prerequisite";
        String description = "description";
        functionalCaseBlob.setPrerequisite(prerequisite.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(textDescription.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(expectedResult.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(description.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.updateByPrimaryKeyWithBLOBs(functionalCaseBlob);
        mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL + "gyq_disassociate_case_4");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        TestPlanCaseExecuteHistory testPlanCaseExecuteHistory = new TestPlanCaseExecuteHistory();
        testPlanCaseExecuteHistory.setCaseId("gyq_disassociate_fc_4");
        testPlanCaseExecuteHistory.setTestPlanCaseId("gyq_disassociate_case_4");
        testPlanCaseExecuteHistory.setTestPlanId("gyq_disassociate_plan_2");
        testPlanCaseExecuteHistory.setDeleted(false);
        testPlanCaseExecuteHistory.setId("history_id");
        testPlanCaseExecuteHistory.setCreateTime(System.currentTimeMillis());
        testPlanCaseExecuteHistory.setCreateUser("admin");
        testPlanCaseExecuteHistory.setStatus("SUCCESS");
        testPlanCaseExecuteHistoryMapper.insertSelective(testPlanCaseExecuteHistory);
        mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL + "gyq_disassociate_case_4");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        TestPlanCaseExecuteHistory testPlanCaseExecuteHistory1 = testPlanCaseExecuteHistoryMapper.selectByPrimaryKey("history_id");
        testPlanCaseExecuteHistory1.setSteps(new byte[0]);
        testPlanCaseExecuteHistoryMapper.updateByPrimaryKeySelective(testPlanCaseExecuteHistory1);
        mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL + "gyq_disassociate_case_4");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        testPlanCaseExecuteHistory1.setSteps(jsonString.getBytes());
        testPlanCaseExecuteHistoryMapper.updateByPrimaryKeySelective(testPlanCaseExecuteHistory1);
        mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL + "gyq_disassociate_case_4");
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        functionalCaseDetailDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCaseDetailDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(JSON.parseArray(functionalCaseDetailDTO.getSteps(), FunctionalCaseDetailDTO.class)));
    }


    @Test
    @Order(17)
    public void testUserList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(USER_URL + DEFAULT_PROJECT_ID);
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<User> list = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), User.class);
        Assertions.assertFalse(list.isEmpty());
    }


    @Test
    @Order(17)
    public void testFunctionalAssociate() throws Exception {
        Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates = new HashMap<>();
        List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests = new ArrayList<>();
        BaseCollectionAssociateRequest baseCollectionAssociateRequest = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest.setCollectionId("123");
        baseCollectionAssociateRequest.setModules(buildModulesAll());
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        collectionAssociates.put(AssociateCaseType.FUNCTIONAL, baseCollectionAssociateRequests);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        userDTO.setLastOrganizationId("wxx_1234");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);
        testPlanFunctionalCaseService.associateCollection("plan_1", collectionAssociates, user);

        baseCollectionAssociateRequest.setModules(buildModules());
        testPlanFunctionalCaseService.associateCollection("plan_1", collectionAssociates, user);

        baseCollectionAssociateRequest.setCollectionId("wxxx1231_1");
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        collectionAssociates.put(AssociateCaseType.FUNCTIONAL, baseCollectionAssociateRequests);
        Assertions.assertThrows(Exception.class, () -> testPlanFunctionalCaseService.associateCollection("plan_1", collectionAssociates, user));
    }

    private TestPlanCollectionAssociateDTO buildModules() {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(false);
        associateDTO.setAssociateType(AssociateCaseType.FUNCTIONAL);
        associateDTO.setProjectId("123");
        associateDTO.setModuleMaps(buildModuleMap());
        associateDTO.setSyncCase(true);
        associateDTO.setApiCaseCollectionId("223");
        associateDTO.setApiScenarioCollectionId("323");
        return associateDTO;
    }

    private Map<String, ModuleSelectDTO> buildModuleMap() {
        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(false);
        moduleSelectDTO.setSelectIds(List.of("fc_1"));
        moduleMap.put("100001", moduleSelectDTO);
        return moduleMap;
    }

    private TestPlanCollectionAssociateDTO buildModulesAll() {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(true);
        associateDTO.setAssociateType(AssociateCaseType.FUNCTIONAL);
        associateDTO.setProjectId("123");

        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(true);
        moduleSelectDTO.setSelectIds(new ArrayList<>());
        moduleMap.put("100001", moduleSelectDTO);
        associateDTO.setModuleMaps(moduleMap);
        return associateDTO;
    }

    @Test
    @Order(18)
    public void testFunctionalBatchMove() throws Exception {
        BaseBatchMoveRequest request = new BaseBatchMoveRequest();
        request.setTestPlanId("plan_1");
        request.setTargetCollectionId("wxxx_1");
        request.setSelectAll(true);
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_MOVE_URL, request);
    }

    @Test
    @Order(19)
    public void testBatchAddBug() throws Exception {
        TestPlanCaseBatchAddBugRequest request = buildRequest(false);
        List<MockMultipartFile> files = new ArrayList<>();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_BATCH_ADD_BUG_URL, paramMap);

        request.setSelectAll(true);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_BATCH_ADD_BUG_URL, paramMap);
    }

    private TestPlanCaseBatchAddBugRequest buildRequest(boolean isUpdate) {
        TestPlanCaseBatchAddBugRequest request = new TestPlanCaseBatchAddBugRequest();
        request.setTestPlanId("plan_1");
        request.setProjectId("123");
        request.setTitle("default-bug-title");
        request.setDescription("default-bug-description");
        request.setTemplateId("default-bug-template");
        request.setLinkFileIds(List.of("default-bug-file-id-1"));
        if (isUpdate) {
            request.setId("default-bug-id");
            request.setUnLinkRefIds(List.of("default-file-association-id"));
            request.setDeleteLocalFileIds(List.of("default-bug-file-id"));
            request.setLinkFileIds(List.of("default-bug-file-id-2"));
        }
        BugCustomFieldDTO fieldDTO1 = new BugCustomFieldDTO();
        fieldDTO1.setId("custom-field");
        fieldDTO1.setName("oasis");
        BugCustomFieldDTO fieldDTO2 = new BugCustomFieldDTO();
        fieldDTO2.setId("test_field");
        fieldDTO2.setName(JSON.toJSONString(List.of("test")));
        BugCustomFieldDTO handleUserField = new BugCustomFieldDTO();
        handleUserField.setId("handleUser");
        handleUserField.setName("处理人");
        handleUserField.setValue("admin");
        BugCustomFieldDTO statusField = new BugCustomFieldDTO();
        statusField.setId("status");
        statusField.setName("状态");
        statusField.setValue("1");
        request.setCustomFields(List.of(fieldDTO1, fieldDTO2, handleUserField, statusField));
        return request;
    }


    @Test
    @Order(19)
    public void testBatchAssociateBug() throws Exception {
        TestPlanCaseBatchAssociateBugRequest request = new TestPlanCaseBatchAssociateBugRequest();
        request.setBugIds(Arrays.asList("123456"));
        request.setTestPlanId("plan_1");
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_ASSOCIATE_BUG_URL, request);
        request.setSelectAll(true);
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_ASSOCIATE_BUG_URL, request);
    }

    @Test
    @Order(20)
    public void testMinderBatchAddBug() throws Exception {
        TestPlanCaseBatchAddBugRequest request = buildRequest(false);
        request.setSelectAll(true);
        List<MockMultipartFile> files = new ArrayList<>();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_MINDER_BATCH_ADD_BUG, paramMap);

        request.setSelectAll(false);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_MINDER_BATCH_ADD_BUG, paramMap);

        request.setMinderProjectIds(List.of("123"));
        request.setMinderModuleIds(List.of("t_1"));
        request.setMinderCaseIds(List.of("fc_1"));
        request.setMinderCollectionIds(List.of("123"));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_MINDER_BATCH_ADD_BUG, paramMap);
    }

    @Test
    @Order(21)
    public void testMinderBatchAssociateBug() throws Exception {
        TestPlanCaseBatchAssociateBugRequest request = new TestPlanCaseBatchAssociateBugRequest();
        request.setBugIds(Arrays.asList("123456"));
        request.setTestPlanId("plan_1");
        this.requestPostWithOk(FUNCTIONAL_CASE_MINDER_BATCH_ASSOCIATE_BUG, request);
        request.setSelectAll(true);
        this.requestPostWithOk(FUNCTIONAL_CASE_MINDER_BATCH_ASSOCIATE_BUG, request);
    }
}
