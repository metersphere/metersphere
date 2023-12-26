package io.metersphere.functional.controller;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiTestCasePageProviderRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalTestCaseControllerTests extends BaseTest {

    @MockBean
    BaseAssociateApiProvider provider;

    private static final String URL_CASE_PAGE = "/functional/case/test/associate/page";

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
        List<ApiTestCaseProviderDTO> operations = new ArrayList<>();
        Mockito.when(provider.getApiTestCaseList("functional_case_test","case_id", "source_id", request)).thenReturn(operations);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<ApiTestCaseProviderDTO> apiTestCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ApiTestCaseProviderDTO.class);
        String jsonString = JSON.toJSONString(apiTestCaseProviderDTOS);
        System.out.println(jsonString);
    }
}
