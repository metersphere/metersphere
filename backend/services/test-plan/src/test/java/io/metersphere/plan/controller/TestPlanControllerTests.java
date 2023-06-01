package io.metersphere.plan.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanDTO;
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

    private static String sessionId;
    private static String csrfToken;


    private TestPlanDTO getSimpleTestPlan() {
        TestPlanDTO testPlan = new TestPlanDTO();
        testPlan.setId("test");
        testPlan.setName("test");
        testPlan.setProjectId("1");
        testPlan.setParentId("1");
        testPlan.setCreateUser("JianGuo");
        testPlan.setStage("Smock");
        testPlan.setStatus("PREPARE");
        testPlan.setCreateUser("JianGuo");
        return testPlan;
    }


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
    public void testAdd1() throws Exception {
        TestPlanDTO testPlan = this.getSimpleTestPlan();

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
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    @Test
    @Order(2)
    public void testAdd2() throws Exception {
        TestPlanDTO testPlan = this.getSimpleTestPlan();

        List<String> followerList = new ArrayList<>();
        followerList.add("JianGuo");
        followerList.add("SongGuoyu");
        followerList.add("SongYingyu");
        followerList.add("SongFanti");
        testPlan.setFollowers(followerList);

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    @Test
    @Order(2)
    public void testAdd3() throws Exception {
        TestPlanDTO testPlan = this.getSimpleTestPlan();

        List<String> participantList = new ArrayList<>();
        participantList.add("JianGuo");
        participantList.add("SongGuoyu");
        testPlan.setPrincipals(participantList);

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    @Test
    @Order(4)
    public void testAdd4() throws Exception {
        TestPlanDTO testPlan = this.getSimpleTestPlan();

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    @Test
    @Order(5)
    public void testAddUserFalse() throws Exception {
        TestPlan testPlan = new TestPlan();
        testPlan.setName("test");

        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
