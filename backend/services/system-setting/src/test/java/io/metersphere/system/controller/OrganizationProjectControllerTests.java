package io.metersphere.system.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.*;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.OrganizationCustomFieldService;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.OrganizationTemplateService;
import io.metersphere.system.utils.Pager;
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
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private final static String getAdminList = prefix + "/user-admin-list/";
    private final static String getMemberList = prefix + "/user-member-list/";
    private final static String getPoolOptions = prefix + "/pool-options";
    private final static String updateName = prefix + "/rename";
    private final static String userList = prefix + "/user-list";
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
    private UserMapper userMapper;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private OrganizationCustomFieldService organizationCustomFieldService;
    @Resource
    private OrganizationTemplateService organizationTemplateService;
    @Resource
    protected TemplateMapper templateMapper;
    private final ProjectServiceInvoker serviceInvoker;

    @Autowired
    public OrganizationProjectControllerTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

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
        Assertions.assertSame(currentProject.getEnable(), result.getEnable());

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

    public void initData() {

        // 当前组织下，创建一个字段并关联到模板，测试创建项目时，是否初始化成功
        CustomField customField = new CustomField();
        customField.setName("test create project");
        customField.setScopeId(DEFAULT_ORGANIZATION_ID);
        customField.setScene(TemplateScene.API.name());
        customField.setType(CustomFieldType.DATE.name());
        customField.setCreateUser("amdin");
        customField = organizationCustomFieldService.add(customField, null);
        TemplateExample example = new TemplateExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_ORGANIZATION_ID)
                .andSceneEqualTo(TemplateScene.API.name());
        Template template = templateMapper.selectByExample(example).getFirst();
        TemplateUpdateRequest updateRequest = new TemplateUpdateRequest();
        TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
        templateCustomFieldRequest.setFieldId(customField.getId());
        templateCustomFieldRequest.setDefaultValue(StringUtils.EMPTY);
        templateCustomFieldRequest.setRequired(false);
        updateRequest.setCustomFields(List.of(templateCustomFieldRequest));
        updateRequest.setId(template.getId());
        organizationTemplateService.update(updateRequest);

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
            initProject.setName("项目1");
            initProject.setDescription("项目1");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId2") == null) {
            Project initProject = new Project();
            initProject.setId("projectId2");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目2");
            initProject.setDescription("项目2");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId3") == null) {
            Project initProject = new Project();
            initProject.setId("projectId3");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目3");
            initProject.setDescription("项目3");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId4") == null) {
            Project initProject = new Project();
            initProject.setId("projectId4");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目4");
            initProject.setDescription("项目4");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\",\"loadTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId5") == null) {
            Project initProject = new Project();
            initProject.setId("projectId5");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目5");
            initProject.setDescription("项目5");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }

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
        initData();
        AddProjectRequest project = this.generatorAdd("organizationId", "organization-name", "description", true, List.of("admin"));
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        List<Project> projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.getFirst(), result);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        projectId = result.getId();
        Project projectExtend = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertNull(projectExtend.getModuleSetting());
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        //userId为空的时候
        project = this.generatorAdd("organizationId", "organization-userIdIsNull", "description", true, List.of("admin"));
        mvcResult = this.responsePost(addProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.getFirst(), result);
        Assertions.assertNull(projectExtend.getModuleSetting());

        //设置了模块模版
        List<String> moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("loadTest");
        project.setModuleIds(moduleIds);
        project.setName("org-moduleSetting");
        mvcResult = this.responsePost(addProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.getFirst(), result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        projectExtend = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleIds));

        //设置资源池
        project.setResourcePoolIds(List.of("resourcePoolId"));
        project.setName("org-resourcePool");
        mvcResult = this.responsePost(addProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.getFirst(), result);
        //校验资源池
        ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
        projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectTestResourcePool> projectTestResourcePools = projectTestResourcePoolMapper.selectByExample(projectTestResourcePoolExample);
        Assertions.assertTrue(projectTestResourcePools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList().contains("resourcePoolId"));


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
        AddProjectRequest project = this.generatorAdd("organizationId", "organization-nameError", "description", true, List.of("admin"));
        this.responsePost(addProject, project);
        //项目名称存在 500
        project = this.generatorAdd("organizationId", "organization-nameError", "description", true, List.of("admin"));
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
        //资源池不存在
        project = this.generatorAdd("organizationId", "org-pool-error", null, true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId3"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);
        //成员为空
        project = this.generatorAdd("organizationId", "org-pool-error", null, true, null);
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);

    }

    @Test
    @Order(3)
    public void testGetProject() throws Exception {
        AddProjectRequest project = this.generatorAdd("organizationId", "organization-getName", "description", true, List.of("admin"));
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert result != null;
        projectId = result.getId();
        mvcResult = this.responseGet(getProject + projectId);
        Project getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), projectId));

        mvcResult = this.responseGet(getProject + "projectId");
        getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert getProjects != null;
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), "projectId"));

        mvcResult = this.responseGet(getProject + "projectId1");
        getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert getProjects != null;
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), "projectId1"));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getProject + projectId);
    }

    @Test
    @Order(4)
    public void testGetProjectError() throws Exception {
        //项目不存在
        MvcResult mvcResult = this.responseGet(getProject + "111111");
        ProjectDTO project = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Assertions.assertNull(project);
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
        assert returnPager != null;
        List<ProjectDTO> projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        long firstCreateTime = projectDTOS.getFirst().getCreateTime();
        for (ProjectDTO projectDTO : projectDTOS) {
            Assertions.assertFalse(projectDTO.getCreateTime() > firstCreateTime);
        }
        projectRequest.setFilter(new HashMap<>() {{
            put("createUser", List.of("test"));
        }});
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回的数据中的createUser是admin或者admin1
        assert returnPager != null;
        projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        //拿到所有的createUser
        List<String> createUsers = projectDTOS.stream().map(ProjectDTO::getCreateUser).toList();
        User user = userMapper.selectByPrimaryKey("test");
        Assertions.assertTrue(List.of(user.getName()).containsAll(createUsers));
        //排序
        projectRequest.setSort(new HashMap<>() {{
            put("organizationName", "asc");
        }});
        projectRequest.setFilter(new HashMap<>());
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的organizationName是最小的
        assert returnPager != null;
        projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        String firstOrganizationName = projectDTOS.getFirst().getOrganizationName();
        for (ProjectDTO projectDTO : projectDTOS) {
            Assertions.assertFalse(projectDTO.getOrganizationName().compareTo(firstOrganizationName) < 0);
        }

        projectRequest.setSort(new HashMap<>() {{
            put("organizationName", "desc");
        }});
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的organizationName是最大的
        assert returnPager != null;
        projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        firstOrganizationName = projectDTOS.getFirst().getOrganizationName();
        for (ProjectDTO projectDTO : projectDTOS) {
            Assertions.assertFalse(projectDTO.getOrganizationName().compareTo(firstOrganizationName) > 0);
        }
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
        //当前页数大于500
        projectRequest = new OrganizationProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(501);
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
        UpdateProjectRequest project = this.generatorUpdate("organizationId", projectId, "organization-TestName", "Edit name", true, List.of("admin", "admin1"));
        Project projectExtend = projectMapper.selectByPrimaryKey(projectId);
        List<String> moduleIds = new ArrayList<>();
        if (StringUtils.isNotBlank(projectExtend.getModuleSetting())) {
            moduleIds = JSON.parseArray(projectExtend.getModuleSetting(), String.class);
        }
        project.setModuleIds(moduleIds);
        MvcResult mvcResult = this.responsePost(updateProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        Assertions.assertFalse(currentProject.getAllResourcePool());
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin1"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("organizationId").andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin1"));
        //断言模块设置
        projectExtend = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(project.getModuleIds()));

        // 校验日志
        checkLog(projectId, OperationLogType.ADD);
        //用户id为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2", "organization-TestNameUserIdIsNull", "Edit name", true, List.of("admin1"));
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        //断言模块设置
        projectExtend = projectMapper.selectByPrimaryKey("projectId2");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(new ArrayList<>()));

        // 修改模块设置
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2", "org-Module", "Edit name", true, List.of("admin1"));
        moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("uiTest");
        project.setModuleIds(moduleIds);
        project.setAllResourcePool(true);
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        Assertions.assertTrue(currentProject.getAllResourcePool());
        compareProjectDTO(currentProject, result);
        //断言模块设置
        projectExtend = projectMapper.selectByPrimaryKey("projectId2");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleIds));

        //设置资源池
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId3", "org-updatePools", "org-updatePools", true, List.of("admin1"));
        project.setResourcePoolIds(List.of("resourcePoolId", "resourcePoolId1"));
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        //校验资源池
        ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
        projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo("projectId3");
        List<ProjectTestResourcePool> projectTestResourcePools = projectTestResourcePoolMapper.selectByExample(projectTestResourcePoolExample);
        Assertions.assertTrue(projectTestResourcePools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList().containsAll(project.getResourcePoolIds()));

        // @@校验权限
        project.setName("organization-TestName2");
        project.setId("projectId1");
        project.setOrganizationId(getDefault().getId());
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE, updateProject, project);
    }

    @Test
    @Order(8)
    public void testUpdateProjectError() throws Exception {
        //项目名称存在 500
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "org-Module", "description", true, List.of("admin"));
        this.requestPost(updateProject, project);
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
        //资源池不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId", "org-Module-pool", null, true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId3"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId", "org-Module-pool", null, true, null);
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);

    }

    @Test
    @Order(9)
    public void testDeleteProject() throws Exception {
        MvcResult mvcResult = this.responseGet(deleteProject + projectId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Project currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(currentProject.getDeleted(), true);
        Assertions.assertEquals(currentProject.getId(), projectId);
        Assertions.assertEquals(1, count);
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
        Assertions.assertEquals(currentProject.getId(), projectId);
        Assertions.assertEquals(1, count);
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
    public void testAddProjectMember() throws Exception {
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId(projectId);
        List<String> userIds = List.of("admin1", "admin2");
        projectAddMemberRequest.setUserIds(userIds);
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_ADMIN.getValue(), InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, status().isOk());
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(userIds));
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(userIds));
        userRoleRelations.forEach(item -> {
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
    public void testAddProjectMemberError() throws Exception {
        //项目Id为空
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId(null);
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_ADMIN.getValue(), InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id为空
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_ADMIN.getValue(), InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        projectAddMemberRequest.setUserIds(List.of("admin3"));
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_ADMIN.getValue(), InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, ERROR_REQUEST_MATCHER);
        //项目id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId111");
        projectAddMemberRequest.setUserIds(List.of("admin1"));
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_ADMIN.getValue(), InternalUserRole.PROJECT_MEMBER.getValue()));
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
        Assertions.assertTrue(((List<UserExtendDTO>) returnPager.getList()).size() <= memberRequest.getPageSize());
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //去重
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        //返回的数据量和数据库中的数据量相同
        Assertions.assertEquals(returnPager.getTotal(), userIds.size());
        memberRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(getProjectMemberList, memberRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的createTime是最大的
        assert returnPager != null;
        List<UserExtendDTO> userExtendDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserExtendDTO.class);
        long firstCreateTime = userExtendDTOS.getFirst().getCreateTime();
        for (UserExtendDTO userExtendDTO : userExtendDTOS) {
            Assertions.assertFalse(userExtendDTO.getCreateTime() > firstCreateTime);
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
        //当前页数大于500
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(501);
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
    public void testRemoveProjectMember() throws Exception {
        String userId = "admin1";
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        MvcResult mvcResult = this.responseGet(removeProjectMember + projectId + "/" + userId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Assertions.assertEquals(count, userRoleRelations.size());
        // 校验日志
        checkLog(userRoleRelations.getFirst().getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_DELETE, removeProjectMember + projectId + "/" + userId);
    }

    @Test
    @Order(18)
    public void testRemoveProjectMemberError() throws Exception {
        String userId = "admin1";
        MvcResult mvcResult = this.responseGet(removeProjectMember + projectId + "/" + userId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Assertions.assertEquals(0, count);

        //用户Id不存在
        projectId = "projectId1";
        userId = "admin34";
        this.responseGet(removeProjectMember + projectId + "/" + userId, ERROR_REQUEST_MATCHER);
        //项目id不存在
        projectId = "projectId111";
        userId = "admin1";
        this.responseGet(removeProjectMember + projectId + "/" + userId, ERROR_REQUEST_MATCHER);
        this.responseGet(removeProjectMember + "projectId3" + "/" + "admin1", ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(19)
    public void disableSuccess() throws Exception {
        String id = "projectId";
        this.responseGet(disableProject + id, status().isOk());
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
        this.responseGet(enableProject + id, status().isOk());
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

    @Test
    @Order(21)
    public void testGetAdminList() throws Exception {
        //组织下面有成员 返回不为空
        String organizationId = getDefault().getId();
        MvcResult mvcResult = responseGet(getAdminList + organizationId);
        List<UserDTO> userDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        assert userDTOS != null;
        Assertions.assertFalse(userDTOS.isEmpty());

        String keyword = "a";
        responseGet(getAdminList + organizationId + "?keyword=" + keyword);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getAdminList + organizationId);

        //组织不存在
        organizationId = "organizationId111";
        this.responseGet(getAdminList + organizationId, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(22)
    public void testGetMemberList() throws Exception {
        //组织下面有成员 返回不为空
        String organizationId = getDefault().getId();
        String projectId = "projectId4";
        MvcResult mvcResult = responseGet(getMemberList + organizationId + "/" + projectId);
        List<UserDTO> userDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        assert userDTOS != null;
        Assertions.assertFalse(userDTOS.isEmpty());

        String keyword = "a";
        responseGet(getMemberList + organizationId + "/" + projectId + "?keyword=" + keyword);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getMemberList + organizationId + "/" + projectId);
        //组织下面没有成员 返回为空
        organizationId = "default-organization-20";
        projectId = "projectId4";
        mvcResult = responseGet(getMemberList + organizationId + "/" + projectId);
        userDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNull(userDTOS);

        //组织不存在
        organizationId = "organizationId111";
        projectId = "projectId4";
        this.responseGet(getMemberList + organizationId + "/" + projectId, ERROR_REQUEST_MATCHER);
        //项目不存在
        organizationId = getDefault().getId();
        projectId = "projectId111";
        this.responseGet(getMemberList + organizationId + "/" + projectId, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(23)
    public void testGetOptions() throws Exception {
        ProjectPoolRequest projectPoolRequest = new ProjectPoolRequest();
        projectPoolRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.requestPost(getPoolOptions, projectPoolRequest, BAD_REQUEST_MATCHER);
        projectPoolRequest.setModulesIds(List.of("apiTest", "uiTest", "loadTest"));
        this.responsePost(getPoolOptions, projectPoolRequest);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ, getPoolOptions, projectPoolRequest);
    }

    @Test
    @Order(24)
    public void testUpdateName() throws Exception {
        UpdateProjectNameRequest project = new UpdateProjectNameRequest();
        project.setId("projectId1");
        project.setName("org-updateName");
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        requestPost(updateName, project);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        Assertions.assertEquals(currentProject.getName(), project.getName());
        checkLog(project.getId(), OperationLogType.UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE, updateName, project);
    }

    @Test
    @Order(25)
    public void testUpdateNameError() throws Exception {
        //项目名称存在 500
        UpdateProjectNameRequest project = new UpdateProjectNameRequest();
        project.setId("projectId2");
        project.setName("org-updateName");
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.requestPost(updateName, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project.setOrganizationId(null);
        this.requestPost(updateName, project, BAD_REQUEST_MATCHER);
        //项目Id为空
        project.setId(null);
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.requestPost(updateName, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project.setName(null);
        this.requestPost(updateName, project, BAD_REQUEST_MATCHER);
        //项目不存在
        project.setId("1111");
        project.setName("updateName");
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.requestPost(updateName, project, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(26)
    public void testUserList() throws Exception {
        //组织下面有成员 返回不为空
        ProjectUserRequest request = new ProjectUserRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setOrganizationId(getDefault().getId());
        request.setProjectId("projectId4");
        MvcResult mvcResult = responsePost(userList, request);
        List<UserDTO> userDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        assert userDTOS != null;
        Assertions.assertFalse(userDTOS.isEmpty());

        request.setKeyword("a");
        responsePost(userList, request);

    }
}
