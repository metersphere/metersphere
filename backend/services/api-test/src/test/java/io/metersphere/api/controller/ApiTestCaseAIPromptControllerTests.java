package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiCaseAIPromptDTO;
import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseAIPromptControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/case/ai/prompt";

    public static final String EDIT_CONFIG = "/save";
    public static final String GET = "/get";


    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }


    @Test
    @Order(0)
    public void testEdit() throws Exception {
        ApiCaseAIPromptDTO apiCaseAIPromptDTO = new ApiCaseAIPromptDTO();
        apiCaseAIPromptDTO.setAbnormal(true);
        apiCaseAIPromptDTO.setNormal(true);
        apiCaseAIPromptDTO.setCaseName(true);
        apiCaseAIPromptDTO.setRequestParams(true);
        apiCaseAIPromptDTO.setPostScript(true);
        apiCaseAIPromptDTO.setPreScript(true);
        apiCaseAIPromptDTO.setAssertion(true);
        this.requestPost(EDIT_CONFIG, apiCaseAIPromptDTO).andExpect(status().isOk());
    }

    @Test
    @Order(1)
    public void testGet() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET);
        ApiCaseAIPromptDTO apiCaseAIPromptDTO = getResultData(mvcResult, ApiCaseAIPromptDTO.class);
        Assert.notNull(apiCaseAIPromptDTO, "获取AI提示词失败");
        System.out.println(apiCaseAIPromptDTO);


    }
}
