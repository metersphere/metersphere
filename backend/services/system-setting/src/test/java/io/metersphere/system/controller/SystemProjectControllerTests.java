package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemProjectControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private final static String prefix = "/system/project";

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(1)
    public void testAddProjectSuccess() throws Exception {
        String url = prefix + "/add";
        final Project project = new Project();
        project.setId("projectId");
        project.setOrganizationId("organizationId");
        project.setName("name");
        project.setDescription("description");
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        project.setCreateUser("admin");
        project.setUpdateUser("admin");
        project.setEnable(true);
        project.setDeleted(false);

        this.requestPost(url, project, status().isOk());
    }

    @Test
    @Order(2)
    public void testAddProjectError() throws Exception {
        final Project project = new Project();
        project.setId(null);
        project.setNum(null);
        project.setOrganizationId(null);
        project.setName(null);
        project.setDescription(null);
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        project.setCreateUser(null);
        project.setUpdateUser(null);
        project.setEnable(true);
        project.setDeleted(false);

        this.requestPost(prefix + "/add", project, status().isBadRequest());
    }

    @Test
    @Order(3)
    public void testGetProject() throws Exception {
        String projectId = "projectId";
        MvcResult mvcResult = this.responseGet(prefix + "/get/" + projectId);
        String returnData = mvcResult.getResponse().getContentAsString();
        Project project = JSON.parseObject(returnData, Project.class);
        //返回请求正常
        Assertions.assertNotNull(project);
    }

    @Test
    @Order(4)
    @Sql(scripts = {"/sql/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetProjectList() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(5);

        MvcResult mvcResult = this.responsePost(prefix + "/page", projectRequest);

        MockHttpServletResponse mockResponse = mvcResult.getResponse();

        String returnData = mockResponse.getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);

        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        Pager<ProjectDTO> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);

        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), projectRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ProjectDTO>) returnPager.getList()).size() <= projectRequest.getPageSize());
    }

    @Test
    @Order(5)
    public void testUpdateProject() throws Exception {
        String url = prefix + "/update";
        final Project project = new Project();
        project.setId("projectId");
        project.setOrganizationId("organizationId");
        project.setName("TestName");
        project.setDescription("description");
        project.setUpdateTime(System.currentTimeMillis());
        project.setUpdateUser("admin");
        project.setEnable(true);
        project.setDeleted(false);

        this.requestPost(url, project, status().isOk());
    }

    @Test
    @Order(6)
    public void testDeleteProject() throws Exception {
        Project project = new Project();
        project.setId("projectId");
        this.requestPost(prefix + "/delete", project, status().isOk());
    }

    @Test
    @Order(7)
    public void revoke() throws Exception {
        Project project = new Project();
        project.setId("projectId");
        this.requestPost(prefix + "/revoke", project, status().isOk());
    }

    @Test
    @Order(8)
    @Sql(scripts = {"/sql/init_user_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetMember() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(5);
        projectRequest.setProjectId("projectId1");
        MvcResult mvcResult = this.responsePost(prefix + "/member-list", projectRequest);
        MockHttpServletResponse mockResponse = mvcResult.getResponse();

        String returnData = mockResponse.getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);

        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        Pager<User> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);

        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), projectRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<User>) returnPager.getList()).size() <= projectRequest.getPageSize());
    }

    @Test
    @Order(9)
    public void testGetProjectListByOrg() throws Exception{
        String organizationId = "organizationId";
        MvcResult mvcResult = this.responseGet(prefix + "/list/" + organizationId);
        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(10)
    public void testGetProjectListByOrgError() throws Exception{
        String organizationId = "organizationId";
        MvcResult mvcResult = this.responseGet(prefix + "/list/" + organizationId);
        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(11)
    public void testRemoveProjectMember() throws Exception{
        String projectId = "projectId";
        String userId = "admin1";
        mockMvc.perform(MockMvcRequestBuilders.get(prefix + "/remove-member/" + projectId + "/" + userId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print());
    }


}
