package io.metersphere.plan.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanControllerTests {
    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;

    static List<TestPlanDTO> SAVED_TEST_PLAN_DTO_LIST = new ArrayList<>();

    static final String STATIC_UUID = UUID.randomUUID().toString();

    private TestPlanDTO getSimpleTestPlan() {
        TestPlanDTO testPlan = new TestPlanDTO();
        testPlan.setName("test");
        testPlan.setProjectId("test-project-id");
        testPlan.setParentId("root");
        testPlan.setCreateUser("JianGuo");
        testPlan.setStage("Smock");
        testPlan.setStatus("PREPARE");
        testPlan.setCreateUser("JianGuo");
        return testPlan;
    }

    private void isPreDataOk() throws Exception {
        if (SAVED_TEST_PLAN_DTO_LIST.isEmpty()) {
            this.testAddSuccess();
        }
    }

    private void addTestPlanToSavedList(MockHttpServletResponse mockResponse) throws Exception {

        String returnData = mockResponse.getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);

        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        TestPlanDTO testPlanDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanDTO.class);

        //返回值不为空
        Assertions.assertNotNull(testPlanDTO);

        SAVED_TEST_PLAN_DTO_LIST.add(testPlanDTO);
    }

    @BeforeEach
    public void login() throws Exception {
        if (StringUtils.isAnyBlank(sessionId, csrfToken)) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
            csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        }
    }

    @Test
    @Order(1)
    public void testAddSuccess() throws Exception {
        //测试有责任人、关注人
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

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());

        //测试自动赋予了UUID
        testPlan = this.getSimpleTestPlan();
        testPlan.setId(STATIC_UUID);
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());

        //测试创建子计划
        testPlan = this.getSimpleTestPlan();
        followerList = new ArrayList<>();
        followerList.add("JianGuo");
        followerList.add("SongGuoyu");
        followerList.add("SongYingyu");
        followerList.add("SongFanti");
        testPlan.setFollowers(followerList);

        participantList = new ArrayList<>();
        participantList.add("JianGuo");
        participantList.add("SongGuoyu");
        testPlan.setPrincipals(participantList);
        testPlan.setParentId(SAVED_TEST_PLAN_DTO_LIST.get(0).getId());
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());

        //测试只有关注人
        testPlan = this.getSimpleTestPlan();
        followerList = new ArrayList<>();
        followerList.add("JianGuo");
        followerList.add("SongGuoyu");
        followerList.add("SongYingyu");
        followerList.add("SongFanti");
        testPlan.setFollowers(followerList);
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());

        //测试只有责任人
        testPlan = this.getSimpleTestPlan();
        participantList = new ArrayList<>();
        participantList.add("JianGuo");
        participantList.add("SongGuoyu");
        testPlan.setPrincipals(participantList);
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());

        //测试没有责任人没有关注人
        testPlan = this.getSimpleTestPlan();
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        this.addTestPlanToSavedList(mvcResult.getResponse());
    }

    @Test
    @Order(2)
    public void testDeleteSuccess() throws Exception {
        this.isPreDataOk();

        String testPlanId = SAVED_TEST_PLAN_DTO_LIST.get(SAVED_TEST_PLAN_DTO_LIST.size() - 1).getId();
        mockMvc.perform(MockMvcRequestBuilders.get("/test-plan/delete/" + testPlanId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(3)
    public void testDeleteBatchSuccess() throws Exception {
        this.isPreDataOk();

        List<String> testPlanIdList = new ArrayList<>();
        testPlanIdList.add(SAVED_TEST_PLAN_DTO_LIST.get(0).getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/delete/batch")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlanIdList))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testBatchDelete_Error_System() throws Exception {
        //没有必填项
        List<String> testPlanIdList = new ArrayList<>();
        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/delete/batch")
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .content(JSON.toJSONString(testPlanIdList))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is5xxServerError());
    }

    //添加测试计划反例校验:参数不合法
    @Test
    public void testAdd_Error_Param() throws Exception {
        //没有必填项
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

    //添加测试计划反例校验：系统错误（例如ID重复、parentId不合法）
    @Test
    public void testAdd_Error_System() throws Exception {
        this.isPreDataOk();
        //测试重复存储UUID不成功
        TestPlanDTO testPlan = this.getSimpleTestPlan();
        testPlan.setId(STATIC_UUID);
        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        //测试parentId和id相同
        testPlan = this.getSimpleTestPlan();
        testPlan.setId(UUID.randomUUID().toString());
        testPlan.setParentId(testPlan.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/test-plan/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testPlan))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
