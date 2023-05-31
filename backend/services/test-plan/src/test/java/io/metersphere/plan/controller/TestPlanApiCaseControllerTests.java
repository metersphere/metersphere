package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanApiCaseDTO;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = {"/ddl/test_plan_api_case.sql"},
        config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
public class TestPlanApiCaseControllerTests {

    @Resource
    private MockMvc mockMvc;

    public static final String REQ_PREFIX = "/test-plan/api/case";

    public static final String PARAM_TEST_ID = "test-plan-api-case-id";


    @Test
    @Order(1)
    public void testAdd() throws Exception {
        TestPlanApiCaseDTO testPlanApiCaseDTO = new TestPlanApiCaseDTO();
        testPlanApiCaseDTO.setId(UUID.randomUUID().toString());
        testPlanApiCaseDTO.setTestPlanId(UUID.randomUUID().toString());
        testPlanApiCaseDTO.setApiCaseId(UUID.randomUUID().toString());
        testPlanApiCaseDTO.setPos(10001L);
        mockMvc.perform(MockMvcRequestBuilders.post(REQ_PREFIX + "/add")
                        .content(JSON.toJSONString(testPlanApiCaseDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    public void testGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/get/" + PARAM_TEST_ID))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(3)
    public void testUpdate() throws Exception {
        TestPlanApiCaseDTO testPlanApiCaseDTO = new TestPlanApiCaseDTO();
        testPlanApiCaseDTO.setId(PARAM_TEST_ID);
        testPlanApiCaseDTO.setPos(15001L);
        mockMvc.perform(MockMvcRequestBuilders.put(REQ_PREFIX + "/update")
                        .content(JSON.toJSONString(testPlanApiCaseDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
        mockMvc.perform(MockMvcRequestBuilders.get(REQ_PREFIX + "/get/" + PARAM_TEST_ID))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.data.pos").value(15001L))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(4)
    public void testDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(REQ_PREFIX + "/delete/" + PARAM_TEST_ID))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
