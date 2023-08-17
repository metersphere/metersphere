package io.metersphere.system.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectExtend;
import io.metersphere.project.mapper.ProjectExtendMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.ModuleSettingDTO;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.ProjectExtendDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationProjectRequest;
import io.metersphere.system.request.ProjectAddMemberRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationProjectControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private final static String prefix = "/organization/project";
    private final static String addProject = prefix + "/add";
    private final static String updateProject = prefix + "/update";
    private final static String deleteProject = prefix + "/delete/";
    private final static String revokeProject = prefix + "/revoke/";
    private final static String getProject = prefix + "/get/";
    private final static String getProjectList = prefix + "/page";
    private final static String getProjectMemberList = prefix + "/member-list";
    private final static String addProjectMember = prefix + "/add-members";
    private final static String removeProjectMember = prefix + "/remove-member/";
    private final static String disableProject = prefix + "/disable/";
    private final static String enableProject = prefix + "/enable/";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    private static String projectId;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private ProjectExtendMapper projectExtendMapper;

    private OrganizationDTO getDefault() {
        return organizationService.getDefault();
    }

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
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

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
    private void responseGet(String url, ResultMatcher resultMatcher) throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
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
        Assertions.assertTrue(currentProject.getEnable() == result.getEnable());

    }

    public AddProjectRequest generatorAdd(String organizationId, String name, String description, boolean enable, List<String> userIds) {
        AddProjectRequest addProjectDTO = new AddProjectRequest();
        addProjectDTO.setOrganizationId(organizationId);
        addProjectDTO.setName(name);
        addProjectDTO.setDescription(description);
        addProjectDTO.setEnable(enable);
        addProjectDTO.setUserIds(userIds);
        return addProjectDTO;
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
    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_org_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    /**
     * 测试添加项目成功的情况
     */
    public void testAddProjectSuccess() throws Exception {
        AddProjectRequest project = this.generatorAdd("organizationId","organization-name", "description", true, List.of("admin"));
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectExtendDTO result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        List<Project> projects = projectMapper.selectByExample(projectExample);
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        this.compareProjectDTO(projects.get(0), result);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
         userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        projectId = result.getId();
        ProjectExtend projectExtend = projectExtendMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(new ModuleSettingDTO()));
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        //userId为空的时候
        project = this.generatorAdd("organizationId","organization-userIdIsNull", "description", true, new ArrayList<>());
        mvcResult = this.responsePost(addProject, project);
        result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        this.compareProjectDTO(projects.get(0), result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        projectExtend = projectExtendMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(new ModuleSettingDTO()));

        //设置了模块模版
        ModuleSettingDTO moduleSettingDTO = new ModuleSettingDTO();
        moduleSettingDTO.setApiTest(true);
        moduleSettingDTO.setLoadTest(true);
        project.setModuleSetting(moduleSettingDTO);
        project.setName("org-moduleSetting");
        mvcResult = this.responsePost(addProject, project);
        result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        this.compareProjectDTO(projects.get(0), result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin")), true);
        projectExtend = projectExtendMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleSettingDTO));

        project.setName("organization-testAddProjectSuccess1");
        project.setOrganizationId(getDefault().getId());
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_ADD, addProject, project);
    }

    @Test
    @Order(2)
    /**
     * 测试添加项目失败的用例
     */
    public void testAddProjectError() throws Exception {
        AddProjectRequest project = this.generatorAdd("organizationId","organization-nameError", "description", true, List.of("admin"));
        this.responsePost(addProject, project);
        //项目名称存在 500
        project = this.generatorAdd("organizationId","organization-nameError", "description", true, List.of("admin"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorAdd(null, null, null, true, List.of("admin"));
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorAdd("organizationId", null, null, true, List.of("admin"));
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);
        //项目成员在系统中不存在
        project = this.generatorAdd("organizationId", "name", null, true, List.of("admin", "admin1", "admin3"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);

    }
    @Test
    @Order(3)
    public void testGetProject() throws Exception {
        AddProjectRequest project = this.generatorAdd("organizationId","organization-getName", "description", true, List.of("admin"));
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectExtendDTO result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        projectId = result.getId();
        mvcResult = this.responseGet(getProject + projectId);
        ProjectExtendDTO getProjects = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), projectId));


        mvcResult = this.responseGet(getProject + "projectId1");
        getProjects = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), "projectId1"));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getProject + projectId);
    }
    @Test
    @Order(4)
    public void testGetProjectError() throws Exception {
        //项目不存在
        MvcResult mvcResult = this.responseGet(getProject + "111111");
        ProjectExtend project = this.parseObjectFromMvcResult(mvcResult, ProjectExtend.class);
        Assertions.assertEquals(project, new ProjectExtend());
    }

    @Test
    @Order(5)
    public void testGetProjectList() throws Exception {
        OrganizationProjectRequest projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(10);
        projectRequest.setOrganizationId(getDefault().getId());
        MvcResult mvcResult = this.responsePost(getProjectList, projectRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), projectRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ProjectDTO>) returnPager.getList()).size() <= projectRequest.getPageSize());
        projectRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的createTime是最大的
        List<ProjectDTO> projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        long firstCreateTime = projectDTOS.get(0).getCreateTime();
        for (ProjectDTO projectDTO : projectDTOS) {
            Assertions.assertFalse(projectDTO.getCreateTime() > firstCreateTime);
        }
        projectRequest.setFilter(new HashMap<>() {{
            put("createUser", List.of("test"));
        }});
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回的数据中的createUser是admin或者admin1
        projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        //拿到所有的createUser
        List<String> createUsers = projectDTOS.stream().map(ProjectDTO::getCreateUser).collect(Collectors.toList());
        Assertions.assertTrue(List.of("test").containsAll(createUsers));
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getProjectList, projectRequest);
    }

    @Test
    @Order(6)
    public void testPageError() throws Exception {
        //当前页码不大于0
        OrganizationProjectRequest projectRequest = new OrganizationProjectRequest();
        projectRequest.setPageSize(5);
        projectRequest.setOrganizationId("organizationId");
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setOrganizationId("organizationId");
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //当前页数大于100
        projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(101);
        projectRequest.setOrganizationId("organizationId");
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(5);
        projectRequest.setOrganizationId("organizationId");
        projectRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //组织id为空
        projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(5);
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
    }

    @Test
    @Order(7)
    public void testUpdateProject() throws Exception {
        UpdateProjectRequest project = this.generatorUpdate("organizationId", projectId,"organization-TestName", "Edit name", true, List.of("admin1"));
        ProjectExtend projectExtend = projectExtendMapper.selectByPrimaryKey(projectId);
        ModuleSettingDTO moduleSettingDTO = JSON.parseObject(projectExtend.getModuleSetting(), ModuleSettingDTO.class);
        moduleSettingDTO.setApiTest(true);
        moduleSettingDTO.setTestPlan(true);
        moduleSettingDTO.setUiTest(true);
        project.setModuleSetting(moduleSettingDTO);
        MvcResult mvcResult = this.responsePost(updateProject, project);
        ProjectExtendDTO result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        this.compareProjectDTO(currentProject, result);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of("admin1")), true);
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(List.of( "admin1")), true);
        //断言模块设置
        projectExtend = projectExtendMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleSettingDTO));

        // 校验日志
        checkLog(projectId, OperationLogType.ADD);
        //用户id为空
        project = this.generatorUpdate("organizationId", "projectId2", "organization-TestNameUserIdIsNull", "Edit name", true, new ArrayList<>());
        mvcResult = this.responsePost(updateProject, project);
        result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        this.compareProjectDTO(currentProject, result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId2").andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //断言userRoleRelations是空的
        Assertions.assertTrue(userRoleRelations.isEmpty());
        //断言模块设置
        projectExtend = projectExtendMapper.selectByPrimaryKey("projectId2");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(new ModuleSettingDTO()));

        // 修改模块设置
        project = this.generatorUpdate("organizationId", "projectId2", "org-Module", "Edit name", true, new ArrayList<>());
        projectExtend = projectExtendMapper.selectByPrimaryKey("projectId2");
        moduleSettingDTO = JSON.parseObject(projectExtend.getModuleSetting(), ModuleSettingDTO.class);
        moduleSettingDTO.setApiTest(true);
        moduleSettingDTO.setTestPlan(true);
        moduleSettingDTO.setUiTest(true);
        moduleSettingDTO.setWorkstation(true);
        project.setModuleSetting(moduleSettingDTO);
        mvcResult = this.responsePost(updateProject, project);
        result = this.parseObjectFromMvcResult(mvcResult, ProjectExtendDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        this.compareProjectDTO(currentProject, result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId2").andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //断言userRoleRelations是空的
        Assertions.assertTrue(userRoleRelations.isEmpty());
        //断言模块设置
        projectExtend = projectExtendMapper.selectByPrimaryKey("projectId2");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleSettingDTO));
        // @@校验权限
        project.setName("organization-TestName2");
        project.setId("projectId1");
        project.setOrganizationId(getDefault().getId());
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE, updateProject, project);
        // 校验日志
        checkLog(projectId, OperationLogType.UPDATE);
    }

    @Test
    @Order(8)
    public void testUpdateProjectError() throws Exception {
        //项目名称存在 500
        UpdateProjectRequest project = this.generatorUpdate(getDefault().getId(), "projectId1","organization-TestName2", "description", true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorUpdate(null, "projectId",null, null, true , List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目Id为空
        project = this.generatorUpdate("organizationId", null,null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorUpdate("organizationId", "projectId",null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目不存在
        project = this.generatorUpdate("organizationId", "1111","123", null, true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(9)
    public void testDeleteProject() throws Exception {
        MvcResult mvcResult = this.responseGet(deleteProject + projectId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Project currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(currentProject.getDeleted(), true);
        Assertions.assertTrue(currentProject.getId().equals(projectId));
        Assertions.assertTrue(count == 1);
        // 校验日志
        checkLog(projectId, OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_DELETE, deleteProject + projectId);
    }

    @Test
    @Order(10)
    public void testDeleteProjectError() throws Exception {
        String id = "1111";
        this.responseGet(deleteProject + id, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(11)
    public void revokeSuccess() throws Exception {
        MvcResult mvcResult = this.responseGet(revokeProject + projectId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Project currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(currentProject.getDeleted(), false);
        Assertions.assertTrue(currentProject.getId().equals(projectId));
        Assertions.assertTrue(count == 1);
        // 校验日志
        checkLog(projectId, OperationLogType.RECOVER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_RECOVER, revokeProject + projectId);
    }

    @Test
    @Order(12)
    public void testRevokeProjectError() throws Exception {
        String id = "1111";
        this.responseGet(revokeProject + id, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(13)
    public void testAddProjectMember() throws Exception{
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId(projectId);
        List<String> userIds = List.of("admin1", "admin2");
        projectAddMemberRequest.setUserIds(userIds);
        this.requestPost(addProjectMember, projectAddMemberRequest, status().isOk());
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertEquals(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(userIds), true);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList()).containsAll(userIds));
        userRoleRelations.forEach(item ->{
            try {
                checkLog(item.getId(), OperationLogType.ADD);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_ADD, addProjectMember, projectAddMemberRequest);
    }

    @Test
    @Order(14)
    public void testAddProjectMemberError() throws Exception{
        //项目Id为空
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId(null);
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id为空
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        projectAddMemberRequest.setUserIds(List.of("admin3"));
        this.requestPost(addProjectMember, projectAddMemberRequest, ERROR_REQUEST_MATCHER);
        //项目id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId111");
        projectAddMemberRequest.setUserIds(List.of("admin1"));
        this.requestPost(addProjectMember, projectAddMemberRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(15)
    public void testGetMemberSuccess() throws Exception {
        ProjectMemberRequest memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(5);
        memberRequest.setProjectId(projectId);
        MvcResult mvcResult = this.responsePost(getProjectMemberList, memberRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), memberRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<UserExtend>) returnPager.getList()).size() <= memberRequest.getPageSize());
        memberRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(getProjectMemberList, memberRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的createTime是最大的
        List<UserExtend> userExtends = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserExtend.class);
        long firstCreateTime = userExtends.get(0).getCreateTime();
        for (UserExtend userExtend : userExtends) {
            Assertions.assertFalse(userExtend.getCreateTime() > firstCreateTime);
        }
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getProjectMemberList, memberRequest);
    }

    @Test
    @Order(16)
    public void testGetMemberError() throws Exception {
        //当前页码不大于0
        ProjectMemberRequest memberRequest = new ProjectMemberRequest();
        memberRequest.setPageSize(5);
        this.requestPost(getProjectMemberList, memberRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        this.requestPost(getProjectMemberList, memberRequest, BAD_REQUEST_MATCHER);
        //当前页数大于100
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(101);
        this.requestPost(getProjectMemberList, memberRequest, BAD_REQUEST_MATCHER);
        //项目Id为空
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(5);
        this.requestPost(getProjectMemberList, memberRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(5);
        memberRequest.setProjectId("projectId1");
        memberRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(getProjectMemberList, memberRequest, BAD_REQUEST_MATCHER);
    }

    @Test
    @Order(17)
    public void testRemoveProjectMember() throws Exception{
        String userId = "admin1";
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        MvcResult mvcResult = this.responseGet(removeProjectMember + projectId + "/" + userId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Assertions.assertTrue(count == userRoleRelations.size());
        // 校验日志
        checkLog(userRoleRelations.get(0).getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_DELETE, removeProjectMember + projectId + "/" + userId);
    }

    @Test
    @Order(18)
    public void testRemoveProjectMemberError() throws Exception{
        String userId = "admin1";
        MvcResult mvcResult = this.responseGet(removeProjectMember + projectId + "/" + userId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Assertions.assertTrue(count == 0);

        //用户Id不存在
        projectId = "projectId1";
        userId = "admin34";
        this.responseGet(removeProjectMember + projectId + "/" + userId, ERROR_REQUEST_MATCHER);
        //项目id不存在
        projectId = "projectId111";
        userId = "admin1";
        this.responseGet(removeProjectMember + projectId + "/" + userId, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(19)
    public void disableSuccess() throws Exception {
        String id = "projectId";
        this.responseGet(disableProject + id,status().isOk());
        Project currentProject = projectMapper.selectByPrimaryKey(id);
        Assertions.assertEquals(currentProject.getEnable(), false);
        checkLog(id, OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE, disableProject + id);
    }

    @Test
    @Order(20)
    public void disableError() throws Exception {
        String id = "1111";
        this.responseGet(disableProject + id, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(19)
    public void enableSuccess() throws Exception {
        String id = "projectId";
        this.responseGet(enableProject + id,status().isOk());
        Project currentProject = projectMapper.selectByPrimaryKey(id);
        Assertions.assertEquals(currentProject.getEnable(), true);
        checkLog(id, OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE, enableProject + id);
    }

    @Test
    @Order(20)
    public void enableError() throws Exception {
        String id = "1111";
        this.responseGet(enableProject + id, ERROR_REQUEST_MATCHER);
    }

}
