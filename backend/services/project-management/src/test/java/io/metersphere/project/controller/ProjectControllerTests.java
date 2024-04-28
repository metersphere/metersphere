package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.dto.ProjectRequest;
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
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
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
    private static final String getOptionsWidthModule = prefix + "/list/options/";

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
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;


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

    public ProjectRequest generatorUpdate(String organizationId,
                                          String projectId,
                                          String name,
                                          String description,
                                          boolean enable) {
        ProjectRequest updateProjectDTO = new ProjectRequest();
        updateProjectDTO.setOrganizationId(organizationId);
        updateProjectDTO.setId(projectId);
        updateProjectDTO.setName(name);
        updateProjectDTO.setDescription(description);
        updateProjectDTO.setEnable(enable);
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
        requestGet(getOptionsWidthModule + DEFAULT_ORGANIZATION_ID +"/apiTest", status().is5xxServerError());
        requestGet(getOptionsWidthModule + DEFAULT_ORGANIZATION_ID +"/''", status().is5xxServerError());
        MvcResult mvcResultModule = this.responseGet(getOptionsWidthModule + DEFAULT_ORGANIZATION_ID +"/API");
        List<Project> listModule = parseObjectFromMvcResult(mvcResultModule, List.class);
        Assertions.assertNotNull(listModule);
        ProjectExample exampleWidthModule = new ProjectExample();
        exampleWidthModule.createCriteria().andOrganizationIdEqualTo(DEFAULT_ORGANIZATION_ID).andEnableEqualTo(true);
        List<Project> projects = projectMapper.selectByExample(exampleWidthModule);
        int a = 0;
        for (Project project : projects) {
            if (StringUtils.isNotBlank(project.getModuleSetting()) && project.getModuleSetting().contains("apiTest")) {
                a = a+1;
            }
        }
        Assertions.assertEquals(a, listModule.size());

        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId("admin1");
        userRoleRelation.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        userRoleRelation.setSourceId(DEFAULT_ORGANIZATION_ID);
        userRoleRelation.setRoleId("1");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setCreateUser("admin");
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelationMapper.insertSelective(userRoleRelation);

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "admin1", "admin1@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        UserDTO userDTO = baseUserMapper.selectById("admin1");
        userDTO.setLastProjectId(null);
        userMapper.updateByPrimaryKey(userDTO);
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + DEFAULT_ORGANIZATION_ID)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        list = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(list);

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + DEFAULT_ORGANIZATION_ID+"/API")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        listModule = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(listModule);

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

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(getOptions + DEFAULT_ORGANIZATION_ID+"/API")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        listModule = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(listModule);
        //断言list是空的
        Assertions.assertEquals(0, listModule.size());
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
        ProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "project-TestName", "Edit name", true);
        MvcResult mvcResult = this.responsePost(updateProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        // 校验日志
        checkLog("projectId1", OperationLogType.UPDATE);

        // @@校验权限 这个是因为 插入的权限数据是空的  导致权限校验失败  主要是 user_role_relation表的数据

    }

    @Test
    @Order(8)
    public void testUpdateProjectError() throws Exception {
        //项目名称存在 500
        ProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "project-TestName", "description", true);
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorUpdate(null, "projectId", null, null, true);
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目Id为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, null, null, null, true);
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId", null, null, true);
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "1111", "123", null, true);
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(9)
    public void testGetPoolOptions() throws Exception {
        MvcResult mvcResult = this.responseGet(getPoolOptions + ApplicationScope.API_TEST + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + "test" + "/" + DEFAULT_PROJECT_ID);
        mvcResult = this.responseGet(getPoolOptions + ApplicationScope.API_TEST + "/" + "projectId");
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

    @Test
    @Order(12)
    public void testGetProjectMember() throws Exception {
        // 成员列表
        MvcResult mvcResult = this.responseGet(prefix + "/get-member/option/" + DEFAULT_PROJECT_ID);
        List list = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(list);

        this.requestGet(prefix + "/get-member/option/" + "123");

        requestGetPermissionTest(PermissionConstants.PROJECT_BASE_INFO_READ, prefix + "/get-member/option/" + DEFAULT_PROJECT_ID);
    }
}
