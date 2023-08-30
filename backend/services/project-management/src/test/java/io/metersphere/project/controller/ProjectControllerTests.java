package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.ProjectExtendDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;
    private static final String prefix = "/project";
    private static final String getOptions = prefix + "/list/options/";
    @Resource
    private ProjectMapper projectMapper;

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetProject() throws Exception {
        MvcResult mvcResult = responseGet(prefix + "/get/projectId");
        ProjectExtendDTO getProjects = parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        Assertions.assertNotNull(getProjects);
        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, prefix + "/get/projectId");

    }

    @Test
    @Order(2)
    public void testGetProjectError() throws Exception {
        //项目不存在
        MvcResult mvcResult = this.responseGet(prefix + "/get/111111");
        ProjectExtendDTO project = parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        Assertions.assertNull(project);
    }

    @Test
    @Order(3)
    public void testGetOptionsSuccess() throws Exception {

        MvcResult mvcResult = this.responseGet(getOptions + "default_organization");
        List<Project> list = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(list);
        ProjectExample example = new ProjectExample();
        example.createCriteria().andOrganizationIdEqualTo("default_organization").andEnableEqualTo(true);
        Assertions.assertEquals(projectMapper.countByExample(example), list.size());

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "admin1", "admin1@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + "default_organization")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        list = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(list);

        //被删除的用户 查出来的是空的
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "delete", "deleted@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + "default_organization")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        list = parseObjectFromMvcResult(mvcResult, List.class);
        //断言list是空的
        Assertions.assertEquals(0, list.size());

        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, getOptions + "default_organization");
    }

    @Test
    @Order(4)
    public void testGetOptionsError() throws Exception {
        //组织不存在
        requestGet(getOptions + "111111", status().is5xxServerError());
    }

    @Test
    @Order(5)
    public void testSwitchProject() throws Exception {
        //切换项目
        ProjectSwitchRequest request = new ProjectSwitchRequest();
        request.setProjectId("projectId");
        request.setUserId("admin");
        MvcResult mvcResult = requestPostWithOkAndReturn(prefix + "/switch", request);
        UserDTO userDTO = parseObjectFromMvcResult(mvcResult, UserDTO.class);
        Assertions.assertNotNull(userDTO);
        Assertions.assertEquals("projectId", userDTO.getLastProjectId());
        request.setProjectId("default_project");
        //权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, prefix + "/switch", request);
    }

    @Test
    @Order(6)
    public void testSwitchProjectError() throws Exception {
        //项目id为空
        ProjectSwitchRequest request = new ProjectSwitchRequest();
        request.setProjectId("");
        request.setUserId("admin");
        requestPost(prefix + "/switch", request, status().isBadRequest());
        //用户id为空
        request.setProjectId("projectId");
        request.setUserId("");
        requestPost(prefix + "/switch", request, status().isBadRequest());
        //项目不存在
        request = new ProjectSwitchRequest();
        request.setProjectId("111111");
        request.setUserId("admin");
        requestPost(prefix + "/switch", request, status().is5xxServerError());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "delete", "deleted@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        request.setProjectId("projectId");

        mockMvc.perform(MockMvcRequestBuilders.post(prefix + "/switch", request)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

    }

}
