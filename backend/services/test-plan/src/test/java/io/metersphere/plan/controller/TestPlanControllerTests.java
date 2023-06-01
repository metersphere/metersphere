package io.metersphere.plan.controller;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanDTO;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        TestPlanDTO testPlan = new TestPlanDTO();
        testPlan.setId("test");
        testPlan.setName("test");
        testPlan.setProjectId("1");
        testPlan.setParentId("1");
        testPlan.setCreateUser("JianGuo");
        testPlan.setStage("Smock");
        testPlan.setStatus("PREPARE");
        testPlan.setCreateUser("JianGuo");

        List<String> followerList = new ArrayList<>();
        followerList.add("JianGuo");
        followerList.add("SongGuoyu");
        followerList.add("SongYingyu");
        followerList.add("SongFanti");
        testPlan.setFollowers(followerList);

        List<String> participantList = new ArrayList<>();
        participantList.add("JianGuo");
        participantList.add("SongGuoyu");
        testPlan.setPrincipals(participantList);

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
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
