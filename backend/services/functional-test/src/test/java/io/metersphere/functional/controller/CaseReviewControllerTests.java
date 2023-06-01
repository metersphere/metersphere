package io.metersphere.functional.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CaseReviewControllerTests {

    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;


    @Test
    @Order(0)
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/signin")
                        .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
    }

    @Test
    @Order(1)
    public void testAddCaseReview() throws Exception {
        CaseReview caseReview = new CaseReview();
        caseReview.setId("case-review-id");
        caseReview.setName("1");
        caseReview.setStatus("Finished");
        caseReview.setCreateTime(System.currentTimeMillis());
        caseReview.setUpdateTime(System.currentTimeMillis());
        caseReview.setEndTime(System.currentTimeMillis());
        caseReview.setProjectId("1");
        caseReview.setCreateUser("admin");
        caseReview.setReviewPassRule("SINGLE");

        mockMvc.perform(MockMvcRequestBuilders.post("/case/review/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(caseReview))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    @Test
    @Order(2)
    public void testAddCaseReviewFalse() throws Exception {
        CaseReview caseReview = new CaseReview();
        caseReview.setName("test");

        mockMvc.perform(MockMvcRequestBuilders.post("/case/review/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(caseReview))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(3)
    public void testDelCaseReview() throws Exception {
        CaseReview caseReview = new CaseReview();
        caseReview.setId("case-review-id");
        mockMvc.perform(MockMvcRequestBuilders.post("/case/review/delete")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(caseReview))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(4)
    public void testGetCaseReview() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/case/review/get/case-review-id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());
    }
}
