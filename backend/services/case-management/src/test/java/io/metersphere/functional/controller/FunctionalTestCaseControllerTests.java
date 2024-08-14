package io.metersphere.functional.controller;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioModule;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiDefinitionModuleMapper;
import io.metersphere.api.mapper.ApiScenarioModuleMapper;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.constants.AssociateCaseType;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.dto.FunctionalCaseTestPlanDTO;
import io.metersphere.functional.dto.TestPlanCaseExecuteHistoryDTO;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.functional.request.AssociatePlanPageRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.plan.mapper.TestPlanCaseExecuteHistoryMapper;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.provider.BaseAssociateScenarioProvider;
import io.metersphere.request.*;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalTestCaseControllerTests extends BaseTest {


    private static final String URL_CASE_PAGE = "/functional/case/test/associate/case/page";

    private static final String URL_CASE_PAGE_MODULE_COUNT = "/functional/case/test/associate/case/module/count";

    private static final String URL_CASE_PAGE_ASSOCIATE = "/functional/case/test/associate/case";

    private static final String URL_CASE_PAGE_DISASSOCIATE = "/functional/case/test/disassociate/case";

    private static final String URL_CASE_MODULE_TREE = "/functional/case/test/associate/case/module/tree";

    private static final String URL_HAS_CASE_PAGE = "/functional/case/test/has/associate/case/page";

    private static final String URL_BUG_PAGE = "/functional/case/test/associate/bug/page";
    private static final String URL_ASSOCIATE_BUG = "/functional/case/test/associate/bug";
    private static final String URL_DISASSOCIATE_BUG = "/functional/case/test/disassociate/bug/";
    private static final String URL_ASSOCIATE_BUG_PAGE = "/functional/case/test/has/associate/bug/page";

    private static final String URL_ASSOCIATE_TEST_PLAN_PAGE = "/functional/case/test/has/associate/plan/page";

    private static final String URL_TEST_PLAN_EXECUTE_HISTORY = "/functional/case/test/plan/comment/";


    @Resource
    BaseAssociateApiProvider provider;

    @Resource
    BaseAssociateScenarioProvider scenarioProvider;

    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;

    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;

    @Resource
    BaseAssociateBugProvider baseAssociateBugProvider;

    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;


    @Test
    @Order(1)
    public void getPageSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProtocols(List.of("HTTP"));
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        TestCaseProviderDTO testCaseProviderDTO = new TestCaseProviderDTO();
        testCaseProviderDTO.setName("第一个");
        List<TestCaseProviderDTO> operations = new ArrayList<>();
        operations.add(testCaseProviderDTO);
        Mockito.when(provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request)).thenReturn(operations);
        List<TestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<TestCaseProviderDTO> testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestCaseProviderDTO.class);
        Assertions.assertNotNull(testCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiTestCaseList));

        request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        testCaseProviderDTO = new TestCaseProviderDTO();
        testCaseProviderDTO.setName("第一个场景");
        operations = new ArrayList<>();
        operations.add(testCaseProviderDTO);
        Mockito.when(scenarioProvider.getScenarioCaseList("functional_case_test", "case_id", "source_id", request)).thenReturn(operations);
        List<TestCaseProviderDTO> apiScenarioList = scenarioProvider.getScenarioCaseList("functional_case_test", "case_id", "source_id", request);
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestCaseProviderDTO.class);
        Assertions.assertNotNull(testCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiScenarioList));
    }

    @Test
    @Order(2)
    public void getPageSuccessT() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProtocols(List.of("HTTP"));
        List<TestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<TestCaseProviderDTO> testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestCaseProviderDTO.class);
        Assertions.assertNotNull(testCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiTestCaseList));

        request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setSourceId("gyq_associate_scenario_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        List<TestCaseProviderDTO> apiScenarioList = scenarioProvider.getScenarioCaseList("functional_case_test", "case_id", "source_id", request);
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestCaseProviderDTO.class);
        Assertions.assertNotNull(testCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiScenarioList));

    }

    @Test
    @Order(3)
    public void getModuleCountSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProtocols(List.of("HTTP"));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_MODULE_COUNT, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        TestCasePageProviderRequest scenarioRequest = new TestCasePageProviderRequest();
        scenarioRequest.setSourceType(AssociateCaseType.SCENARIO);
        scenarioRequest.setSourceId("gyq_associate_scenario_id_1");
        scenarioRequest.setProjectId("project_gyq_associate_test");
        scenarioRequest.setCurrent(1);
        request.setPageSize(10);
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_MODULE_COUNT, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(4)
    public void associateCaseSuccess() throws Exception {
        AssociateOtherCaseRequest request = new AssociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setProjectId("project-associate-case-test");
        request.setExcludeIds(List.of("gyq_associate_api_case_id_2"));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_ASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        List<ApiTestCase> operations = new ArrayList<>();
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("gyq_associate_case_id_1");
        apiTestCase.setVersionId("11");
        operations.add(apiTestCase);
        Mockito.when(provider.getSelectApiTestCases(request, false)).thenReturn(operations);
        Assertions.assertNotNull(resultHolder);
        request.setSelectAll(false);
        request.setProjectId("project-associate-case-test");
        request.setSelectIds(List.of("gyq_associate_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_ASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request = new AssociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setSourceId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setProjectId("project-associate-case-test");
        request.setExcludeIds(List.of("gyq_associate_api_case_id_2"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_ASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        List<ApiScenario> operationScenarios = new ArrayList<>();
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId("gyq_associate_scenario_id_1");
        apiScenario.setVersionId("11");
        operationScenarios.add(apiScenario);
        Mockito.when(scenarioProvider.getSelectScenarioCases(request, false)).thenReturn(operationScenarios);
        Assertions.assertNotNull(resultHolder);
        request.setSelectAll(false);
        request.setProjectId("project-associate-case-test");
        request.setSelectIds(List.of("gyq_associate_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_ASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(5)
    public void disassociateCaseSuccess() throws Exception {
        addFunctionalCase();
        addFunctionalCaseTest();
        DisassociateOtherCaseRequest request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_functional_case_id_1");
        request.setSelectAll(true);
        request.setExcludeIds(List.of("gyq_associate_api_case_id_2"));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        FunctionalCaseTest functionalCaseTest = functionalCaseTestMapper.selectByPrimaryKey("functionalCaseTestHasId");
        Assertions.assertNull(functionalCaseTest);
        request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        addFunctionalCaseScenarioTest();
        request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setCaseId("gyq_associate_functional_case_id_1");
        request.setSelectAll(true);
        request.setExcludeIds(List.of("gyq_associate_api_scenario_id_2"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
        functionalCaseTest = functionalCaseTestMapper.selectByPrimaryKey("functionalCaseTestHasScenarioId");
        Assertions.assertNull(functionalCaseTest);
        request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setSelectIds(List.of("gyq_associate_api_scenario_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request = new DisassociateOtherCaseRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_associate_api_scenario_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(6)
    public void getTreeSuccess() throws Exception {
        ApiDefinitionModule apiDefinitionModule = new ApiDefinitionModule();
        apiDefinitionModule.setId("case_module");
        apiDefinitionModule.setPos(100L);
        apiDefinitionModule.setName("api_case_module");
        apiDefinitionModule.setParentId("NONE");
        apiDefinitionModule.setProjectId("project-associate-case-test");
        apiDefinitionModule.setCreateUser("admin");
        apiDefinitionModule.setCreateTime(System.currentTimeMillis());
        apiDefinitionModule.setUpdateUser("admin");
        apiDefinitionModule.setUpdateTime(System.currentTimeMillis());
        apiDefinitionModuleMapper.insert(apiDefinitionModule);
        List<BaseTreeNode> moduleTreeNode = this.getModuleTreeNode();
        Assertions.assertNotNull(moduleTreeNode);
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("scenario_module");
        apiScenarioModule.setPos(100L);
        apiScenarioModule.setName("api_scenario_module");
        apiScenarioModule.setParentId("NONE");
        apiScenarioModule.setProjectId("project-associate-case-test");
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModuleMapper.insert(apiScenarioModule);
        List<BaseTreeNode> moduleScenarioTreeNode = this.getModuleScenarioTreeNode();
        Assertions.assertNotNull(moduleScenarioTreeNode);
    }

    @Test
    @Order(7)
    public void getAssociateOtherCaseListSuccess() throws Exception {
        addFunctionalCaseTest();
        FunctionalCaseTestRequest request = new FunctionalCaseTestRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_functional_case_id_1");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_HAS_CASE_PAGE, request);
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-admin
        List<FunctionalCaseTestDTO> functionalCaseTestDTOS = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), FunctionalCaseTestDTO.class);
        Assertions.assertNotNull(functionalCaseTestDTOS);
        addFunctionalCaseScenarioTest();
        request = new FunctionalCaseTestRequest();
        request.setSourceType(AssociateCaseType.SCENARIO);
        request.setSourceId("gyq_associate_functional_case_id_1");
        request.setCurrent(1);
        request.setPageSize(10);
        mvcResult = this.requestPostWithOkAndReturn(URL_HAS_CASE_PAGE, request);
        sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-admin
        functionalCaseTestDTOS = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), FunctionalCaseTestDTO.class);
        Assertions.assertNotNull(functionalCaseTestDTOS);
    }


    private List<BaseTreeNode> getModuleTreeNode() throws Exception {
        MvcResult result = this.requestPostWithOkAndReturn(URL_CASE_MODULE_TREE, new AssociateCaseModuleRequest() {{
            this.setProtocol("HTTP");
            this.setProjectId("project-associate-case-test");
            this.setSourceType(AssociateCaseType.API);
        }});
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }

    private List<BaseTreeNode> getModuleScenarioTreeNode() throws Exception {
        MvcResult result = this.requestPostWithOkAndReturn(URL_CASE_MODULE_TREE, new AssociateCaseModuleRequest() {{
            this.setProjectId("project-associate-case-test");
            this.setSourceType(AssociateCaseType.SCENARIO);
        }});
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }

    private void addFunctionalCaseTest() {
        FunctionalCaseTest functionalCaseTest = new FunctionalCaseTest();
        functionalCaseTest.setId("functionalCaseTestHasId");
        functionalCaseTest.setCaseId("gyq_associate_functional_case_id_1");
        functionalCaseTest.setVersionId("1.0");
        functionalCaseTest.setSourceId("gyq_api_case_id_1");
        functionalCaseTest.setSourceType(AssociateCaseType.API);
        functionalCaseTest.setProjectId("gyq-organization-associate-case-test");
        functionalCaseTest.setCreateUser("admin");
        functionalCaseTest.setCreateTime(System.currentTimeMillis());
        functionalCaseTest.setUpdateUser("admin");
        functionalCaseTest.setUpdateTime(System.currentTimeMillis());
        functionalCaseTestMapper.insert(functionalCaseTest);
    }

    private void addFunctionalCaseScenarioTest() {
        FunctionalCaseTest functionalCaseTest = new FunctionalCaseTest();
        functionalCaseTest.setId("functionalCaseTestHasScenarioId");
        functionalCaseTest.setCaseId("gyq_associate_functional_case_id_1");
        functionalCaseTest.setVersionId("1.0");
        functionalCaseTest.setSourceId("gyq_api_scenario_id_1");
        functionalCaseTest.setSourceType(AssociateCaseType.SCENARIO);
        functionalCaseTest.setProjectId("gyq-organization-associate-case-test");
        functionalCaseTest.setCreateUser("admin");
        functionalCaseTest.setCreateTime(System.currentTimeMillis());
        functionalCaseTest.setUpdateUser("admin");
        functionalCaseTest.setUpdateTime(System.currentTimeMillis());
        functionalCaseTestMapper.insert(functionalCaseTest);
    }

    private void addFunctionalCase() {
        FunctionalCase functionalCase = new FunctionalCase();
        functionalCase.setName("测试关联");
        functionalCase.setNum(100001l);
        functionalCase.setModuleId("module");
        functionalCase.setProjectId("gyq-organization-associate-case-test");
        functionalCase.setDeleted(false);
        functionalCase.setTemplateId("default_template");
        functionalCase.setId("gyq_associate_functional_case_id_1");
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setCaseEditType("Text");
        functionalCase.setPos(500L);
        functionalCase.setVersionId("12335");
        functionalCase.setRefId(functionalCase.getId());
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setPublicCase(false);
        functionalCase.setLatest(true);
        functionalCase.setCreateUser("gyq");
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateUser("gyq");
        functionalCase.setUpdateTime(System.currentTimeMillis());
        List<String> tags = new ArrayList<>();
        tags.add("111");
        tags.add("222");
        functionalCase.setTags(tags);
        functionalCaseMapper.insertSelective(functionalCase);
    }


    @Test
    @Order(8)
    public void getAssociateBugList() throws Exception {
        BugPageProviderRequest request = new BugPageProviderRequest();
        request.setSourceId("wx_associate_case_id_1");
        request.setProjectId("project_wx_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        BugProviderDTO bugProviderDTO = new BugProviderDTO();
        bugProviderDTO.setName("第二个");
        List<BugProviderDTO> operations = new ArrayList<>();
        operations.add(bugProviderDTO);
        Mockito.when(baseAssociateBugProvider.getBugList("functional_case_test", "case_id", "bug_id", request)).thenReturn(operations);
        this.requestPostWithOkAndReturn(URL_BUG_PAGE, request);

        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});

        List<BugProviderDTO> bugList = baseAssociateBugProvider.getBugList("functional_case_test", "case_id", "bug_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_BUG_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<BugProviderDTO> bugProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BugProviderDTO.class);
        Assertions.assertNotNull(bugProviderDTOS);
        System.out.println(JSON.toJSONString(bugList));

    }


    @Test
    @Order(9)
    public void testAssociateBugs() throws Exception {
        AssociateBugRequest request = new AssociateBugRequest();
        request.setCaseId("test_1");
        request.setProjectId("project_wx_associate_test");
        List<String> ids = new ArrayList<>();
        ids.add("bug_id_1");
        Mockito.when(baseAssociateBugProvider.getSelectBugs(request, false)).thenReturn(ids);
        this.requestPostWithOkAndReturn(URL_ASSOCIATE_BUG, request);
    }

    @Test
    @Order(10)
    @Sql(scripts = {"/dml/init_bug_relation_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testDisassociateBug() throws Exception {
        //增加日志覆盖率
        this.requestGetWithOkAndReturn(URL_DISASSOCIATE_BUG + "TEST");
        this.requestGetWithOkAndReturn(URL_DISASSOCIATE_BUG + "1234");
    }

    @Test
    @Order(11)
    public void testAssociateBugPage() throws Exception {
        AssociateBugPageRequest request = new AssociateBugPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setCaseId("wx_2");
        request.setProjectId("project_wx_associate_test");
        List<BugProviderDTO> list = new ArrayList<>();
        BugProviderDTO bugProviderDTO = new BugProviderDTO();
        bugProviderDTO.setId("123");
        bugProviderDTO.setName("测试返回数据");
        bugProviderDTO.setHandleUser("wx");
        bugProviderDTO.setStatus("进行中");
        bugProviderDTO.setHandleUserName("wx");
        list.add(bugProviderDTO);
        Mockito.when(baseAssociateBugProvider.hasAssociateBugPage(request)).thenReturn(list);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_ASSOCIATE_BUG_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<BugProviderDTO> bugProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BugProviderDTO.class);
        Assertions.assertNotNull(bugProviderDTOS);

        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        this.requestPostWithOkAndReturn(URL_ASSOCIATE_BUG_PAGE, request);
    }

    @Test
    @Order(12)
    @Sql(scripts = {"/dml/init_test_plan_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testAssociatePlanPage() throws Exception {
        AssociatePlanPageRequest request = new AssociatePlanPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setCaseId("gyq_associate_function_case");
        request.setKeyword("name");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_ASSOCIATE_TEST_PLAN_PAGE, request);
        Pager<List<FunctionalCaseTestPlanDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        Assertions.assertNotNull(tableData);
        List<FunctionalCaseTestPlanDTO> list = tableData.getList();
        Assertions.assertEquals(2, list.size());
    }

    @Test
    @Order(13)
    public void testPlanExecuteHistoryList() throws Exception {
        TestPlanCaseExecuteHistory testPlanCaseExecuteHistory = new TestPlanCaseExecuteHistory();
        testPlanCaseExecuteHistory.setTestPlanCaseId("associate_case_plan_gyq_one");
        testPlanCaseExecuteHistory.setTestPlanId("test_plan_associate_case_gyq_one");
        testPlanCaseExecuteHistory.setCaseId("gyq_associate_function_case");
        testPlanCaseExecuteHistory.setCreateUser("admin");
        testPlanCaseExecuteHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        testPlanCaseExecuteHistory.setId("test");
        String content = "你好评论";
        testPlanCaseExecuteHistory.setContent(content.getBytes());
        testPlanCaseExecuteHistory.setCreateTime(System.currentTimeMillis());
        testPlanCaseExecuteHistoryMapper.insertSelective(testPlanCaseExecuteHistory);
        List<TestPlanCaseExecuteHistoryDTO> gyqReviewCaseTest = getPlanExecuteHistoryList("gyq_associate_function_case");
        Assertions.assertTrue(StringUtils.isNotBlank(gyqReviewCaseTest.getFirst().getContentText()));
        testPlanCaseExecuteHistory = new TestPlanCaseExecuteHistory();
        testPlanCaseExecuteHistory.setTestPlanCaseId("test_plan_associate_case_gyq_two");
        testPlanCaseExecuteHistory.setTestPlanId("associate_case_plan_gyq_two");
        testPlanCaseExecuteHistory.setCaseId("gyq_associate_function_case");
        testPlanCaseExecuteHistory.setCreateUser("admin");
        testPlanCaseExecuteHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        testPlanCaseExecuteHistory.setId("testNoContent");
        testPlanCaseExecuteHistory.setCreateTime(System.currentTimeMillis());
        FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setNum(1);
        functionalCaseStepDTO.setDesc("步骤一");
        functionalCaseStepDTO.setResult("你好评论");
        List<FunctionalCaseStepDTO> list = new ArrayList<>();
        list.add(functionalCaseStepDTO);
        testPlanCaseExecuteHistory.setSteps(JSON.toJSONString(list).getBytes());
        testPlanCaseExecuteHistory.setCreateTime(System.currentTimeMillis());
        testPlanCaseExecuteHistoryMapper.insertSelective(testPlanCaseExecuteHistory);
        gyqReviewCaseTest = getPlanExecuteHistoryList("gyq_associate_function_case");
        Assertions.assertTrue(gyqReviewCaseTest.size() > 1);
    }

    public List<TestPlanCaseExecuteHistoryDTO> getPlanExecuteHistoryList(String caseId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(URL_TEST_PLAN_EXECUTE_HISTORY + caseId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestPlanCaseExecuteHistoryDTO.class);

    }
}
