package io.metersphere.functional.controller;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiTestCasePageProviderRequest;
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


    private static final String URL_CASE_PAGE = "/functional/case/test/associate/page";

    @Resource
    BaseAssociateApiProvider provider;

    @Test
    @Order(1)
    public void getPageSuccess() throws Exception {
        ApiTestCasePageProviderRequest request = new ApiTestCasePageProviderRequest();
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        ApiTestCaseProviderDTO apiTestCaseProviderDTO = new ApiTestCaseProviderDTO();
        apiTestCaseProviderDTO.setName("第一个");
        List<ApiTestCaseProviderDTO> operations = new ArrayList<>();
        operations.add(apiTestCaseProviderDTO);
        Mockito.when(provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request)).thenReturn(operations);
        List<ApiTestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<ApiTestCaseProviderDTO> apiTestCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ApiTestCaseProviderDTO.class);
        Assertions.assertNotNull(apiTestCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiTestCaseList));

    }

    @Test
    @Order(2)
    public void getPageSuccessT() throws Exception {
        ApiTestCasePageProviderRequest request = new ApiTestCasePageProviderRequest();
        request.setSourceId("gyq_associate_case_id_1");
        request.setProjectId("project_gyq_associate_test");
        request.setCurrent(1);
        request.setPageSize(10);
        List<ApiTestCaseProviderDTO> apiTestCaseList = provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<ApiTestCaseProviderDTO> apiTestCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ApiTestCaseProviderDTO.class);
        Assertions.assertNotNull(apiTestCaseProviderDTOS);
        System.out.println(JSON.toJSONString(apiTestCaseList));

    }
}
