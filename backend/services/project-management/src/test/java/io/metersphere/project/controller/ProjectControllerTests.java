package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApplicationScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;
    private static final String prefix = "/project";
    private static final String getOptions = prefix + "/list/options/";
    private final static String updateProject = prefix + "/update";

    private static final String getPoolOptions = prefix + "/pool-options/";

    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    private ProjectMapper projectMapper;
    private final ProjectServiceInvoker serviceInvoker;

    @Resource
    private ProjectService projectService;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @Autowired
    public ProjectControllerTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

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

    private void responseGet(String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public UpdateProjectRequest generatorUpdate(String organizationId,
                                                String projectId,
                                                String name,
                                                String description,
                                                boolean enable,
                                                List<String> userIds) {
        UpdateProjectRequest updateProjectDTO = new UpdateProjectRequest();
        updateProjectDTO.setOrganizationId(organizationId);
        updateProjectDTO.setId(projectId);
        updateProjectDTO.setName(name);
        updateProjectDTO.setDescription(description);
        updateProjectDTO.setEnable(enable);
        updateProjectDTO.setUserIds(userIds);
        return updateProjectDTO;
    }

    public static void compareProjectDTO(Project currentProject, Project result) {
        Assertions.assertNotNull(currentProject);
        Assertions.assertNotNull(result);
        //判断ID是否一样
        Assertions.assertTrue(StringUtils.equals(currentProject.getId(), result.getId()));
        //判断名称是否一样
        Assertions.assertTrue(StringUtils.equals(currentProject.getName(), result.getName()));
        //判断组织是否一样
        Assertions.assertTrue(StringUtils.equals(currentProject.getOrganizationId(), result.getOrganizationId()));
        //判断描述是否一样
        Assertions.assertTrue(StringUtils.equals(currentProject.getDescription(), result.getDescription()));
        //判断是否启用
        Assertions.assertSame(currentProject.getEnable(), result.getEnable());

    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
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
        if (projectMapper.selectByPrimaryKey("projectId") == null) {
            Project initProject = new Project();
            initProject.setId("projectId");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目");
            initProject.setDescription("项目");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId1") == null) {
            Project initProject = new Project();
            initProject.setId("projectId1");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("接口调试的项目1");
            initProject.setDescription("接口调试的项目1");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        MvcResult mvcResult = responseGet(prefix + "/get/projectId");
        ProjectDTO getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Assertions.assertNotNull(getProjects);
        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, prefix + "/get/projectId");

    }

    @Test
    @Order(2)
    public void testGetProjectError() throws Exception {
        //项目不存在
        MvcResult mvcResult = this.responseGet(prefix + "/get/111111");
        ProjectDTO project = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Assertions.assertNull(project);
    }

    @Test
    @Order(3)
    public void testGetOptionsSuccess() throws Exception {

        MvcResult mvcResult = this.responseGet(getOptions + DEFAULT_ORGANIZATION_ID);
        List<Project> list = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(list);
        ProjectExample example = new ProjectExample();
        example.createCriteria().andOrganizationIdEqualTo(DEFAULT_ORGANIZATION_ID).andEnableEqualTo(true);
        Assertions.assertEquals(projectMapper.countByExample(example), list.size());

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "admin1", "admin1@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + DEFAULT_ORGANIZATION_ID)
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
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + DEFAULT_ORGANIZATION_ID)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        list = parseObjectFromMvcResult(mvcResult, List.class);
        //断言list是空的
        Assertions.assertEquals(0, list.size());

        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, getOptions + DEFAULT_ORGANIZATION_ID);
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
        request.setProjectId(DEFAULT_PROJECT_ID);
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

    @Test
    @Order(7)
    public void testUpdateProject() throws Exception {
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "project-TestName", "Edit name", true, List.of("admin1"));
        Project projectExtend = projectMapper.selectByPrimaryKey("projectId1");
        List<String> moduleIds = new ArrayList<>();
        if (StringUtils.isNotBlank(projectExtend.getModuleSetting())) {
            moduleIds = JSON.parseArray(projectExtend.getModuleSetting(), String.class);
        }
        project.setModuleIds(moduleIds);
        MvcResult mvcResult = this.responsePost(updateProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        //断言模块设置
        projectExtend = projectMapper.selectByPrimaryKey("projectId1");
        Assertions.assertEquals(projectExtend.getModuleSetting(), CollectionUtils.isEmpty(project.getModuleIds()) ? null : JSON.toJSONString(project.getModuleIds()));

        // 校验日志
        checkLog("projectId1", OperationLogType.UPDATE);

        // @@校验权限
        project.setName("project-TestName2");
        project.setId(DEFAULT_PROJECT_ID);
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ_UPDATE, updateProject, project);
        // 校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
    }

    @Test
    @Order(8)
    public void testUpdateProjectError() throws Exception {
        //项目名称存在 500
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "project-TestName", "description", true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorUpdate(null, "projectId", null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目Id为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, null, null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId", null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "1111", "123", null, true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(9)
    public void testGetPoolOptions() throws Exception {
        MvcResult mvcResult = this.responseGet(getPoolOptions + ApplicationScope.API_TEST + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.UI_TEST + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.LOAD_TEST + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + "test" + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.API_TEST + "/" + "projectId");
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.UI_TEST + "/" + "projectId");
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.LOAD_TEST + "/" + "projectId");
        //项目不存在
        this.responseGet(getPoolOptions + ApplicationScope.LOAD_TEST + "/" + "projectId20", status().is5xxServerError());
        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, getPoolOptions + "api_test" + "/" + DEFAULT_PROJECT_ID);

    }

    @Test
    @Order(10)
    public void testGetLatestVersion() throws Exception {
        ProjectVersion latestVersion = projectService.getLatestVersion(DEFAULT_PROJECT_ID);
        Assertions.assertNotNull(latestVersion);
        Assertions.assertTrue(latestVersion.getLatest());
    }

    @Test
    @Order(11)
    public void testHasPermission() throws Exception {
        //当前用户是系统管理员
        responseGet(prefix + "/has-permission/" + DEFAULT_PROJECT_ID, status().isOk());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "delete", "deleted@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");

        mockMvc.perform(MockMvcRequestBuilders.get(prefix + "/has-permission/" + "projectId")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId("delete");
        userRoleRelation.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        userRoleRelation.setSourceId("projectId");
        userRoleRelation.setRoleId("1");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setCreateUser("admin");
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelationMapper.insertSelective(userRoleRelation);
        mockMvc.perform(MockMvcRequestBuilders.get(prefix + "/has-permission/" + "projectId")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        ProjectExample example = new ProjectExample();
        example.createCriteria().andIdEqualTo("projectId");
        Project project = new Project();
        project.setEnable(false);
        projectMapper.updateByExampleSelective(project, example);
        mockMvc.perform(MockMvcRequestBuilders.get(prefix + "/has-permission/" + "projectId")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
