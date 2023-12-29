package io.metersphere.functional.controller;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.constants.AssociateCaseType;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.functional.request.FunctionalTestCaseDisassociateRequest;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalTestCaseControllerTests extends BaseTest {


    private static final String URL_CASE_PAGE = "/functional/case/test/associate/api/page";

    private static final String URL_CASE_PAGE_MODULE_COUNT = "/functional/case/test/associate/api/module/count";

    private static final String URL_CASE_PAGE_ASSOCIATE = "/functional/case/test/associate/case";

    private static final String URL_CASE_PAGE_DISASSOCIATE = "/functional/case/test/disassociate/case";




    @Resource
    BaseAssociateApiProvider provider;

    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;

    @Test
    @Order(1)
    public void getPageSuccess() throws Exception {
        TestCasePageProviderRequest request = new TestCasePageProviderRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
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
        List<TestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<TestCaseProviderDTO> testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestCaseProviderDTO.class);
        Assertions.assertNotNull(testCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiTestCaseList));

    }

    @Test
    @Order(3)
    public void getModuleCountSuccess() throws Exception {
        ApiModuleProviderRequest request = new ApiModuleProviderRequest();
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setKeyword("测试查询模块用");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_MODULE_COUNT, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
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
        request.setSelectAll(false);
        request.setProjectId("project-associate-case-test");
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_ASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(5)
    public void disassociateCaseSuccess() throws Exception {
        addFunctionalCaseTest();
        FunctionalTestCaseDisassociateRequest request = new FunctionalTestCaseDisassociateRequest();
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
        request = new FunctionalTestCaseDisassociateRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(true);
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request = new FunctionalTestCaseDisassociateRequest();
        request.setSourceType(AssociateCaseType.API);
        request.setCaseId("gyq_associate_case_id_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_associate_api_case_id_1"));
        mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE_DISASSOCIATE, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    private void addFunctionalCaseTest() {
        FunctionalCaseTest functionalCaseTest = new FunctionalCaseTest();
        functionalCaseTest.setId("functionalCaseTestHasId");
        functionalCaseTest.setCaseId("gyq_associate_functional_case_id_1");
        functionalCaseTest.setSourceId("gyq_api_case_id_1");
        functionalCaseTest.setSourceType(AssociateCaseType.API);
        functionalCaseTest.setProjectId("gyq-organization-associate-case-test");
        functionalCaseTest.setCreateUser("admin");
        functionalCaseTest.setCreateTime(System.currentTimeMillis());
        functionalCaseTest.setUpdateUser("admin");
        functionalCaseTest.setUpdateTime(System.currentTimeMillis());
        functionalCaseTestMapper.insert(functionalCaseTest);
    }


}
