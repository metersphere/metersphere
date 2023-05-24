package io.metersphere.plan.controller;

import io.metersphere.plan.domain.TestPlan;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanControllerTests {
    @Resource
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void testAddUserTrue() throws Exception {
        TestPlan testPlan = new TestPlan();
        testPlan.setName("test");
        testPlan.setProjectId("1");
        testPlan.setParentId("1");
        testPlan.setCreateUser("JianGuo");
        testPlan.setStage("Smock");
        testPlan.setStatus("PREPARE");


        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(2)
    public void testAddUserFalse() throws Exception {
        TestPlan testPlan = new TestPlan();
        testPlan.setName("test");

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
