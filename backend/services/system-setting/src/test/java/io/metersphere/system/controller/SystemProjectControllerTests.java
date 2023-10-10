package io.metersphere.system.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.job.CleanProjectJob;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.ProjectAddMemberRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.BaseTemplateService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemProjectControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private final static String prefix = "/system/project";
    private final static String addProject = prefix + "/add";
    private final static String updateProject = prefix + "/update";
    private final static String deleteProject = prefix + "/delete/";
    private final static String revokeProject = prefix + "/revoke/";
    private final static String getProject = prefix + "/get/";
    private final static String getProjectList = prefix + "/page";
    private final static String getProjectMemberList = prefix + "/member-list";
    private final static String addProjectMember = prefix + "/add-member";
    private final static String removeProjectMember = prefix + "/remove-member/";
    private final static String disableProject = prefix + "/disable/";
    private final static String enableProject = prefix + "/enable/";
    private final static String userList = prefix + "/user-list";
    private final static String getPoolOptions = prefix + "/pool-options";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    private static String projectId;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;
    @Resource
    private CleanProjectJob cleanProjectJob;

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
    @Test
    @Order(1)
    /**
     * 测试添加项目成功的情况
     */
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAddProjectSuccess() throws Exception {
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID,"name", "description", true, List.of("admin"));
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        List<Project> projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.get(0), result);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(DEFAULT_ORGANIZATION_ID).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
         userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        Project currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertNull(currentProject.getModuleSetting());
        // 项目模板未启用时，校验是否创建了项目模板
        assertionsRefTemplateCreate(projectId);

        //userId为空的时候
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID,"userIdIsNull", "description", true, new ArrayList<>());
        // 开启项目模板
        changeOrgTemplateEnable(false);
        mvcResult = this.responsePost(addProject, project);
        changeOrgTemplateEnable(true);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.get(0), result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(DEFAULT_ORGANIZATION_ID).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertNull(currentProject.getModuleSetting());
        // 项目模板开启时，校验是否初始化了项目模板
        assertionsInitTemplate(projectId);

        //设置了模块模版
        List<String> moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("uiTest");
        project.setModuleIds(moduleIds);
        project.setName("moduleSetting");
        mvcResult = this.responsePost(addProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.get(0), result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(DEFAULT_ORGANIZATION_ID).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertEquals(currentProject.getModuleSetting(), JSON.toJSONString(moduleIds));

        //设置资源池
        project.setResourcePoolIds(List.of("resourcePoolId"));
        project.setName("resourcePool");
        mvcResult = this.responsePost(addProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(project.getOrganizationId()).andNameEqualTo(project.getName());
        projects = projectMapper.selectByExample(projectExample);
        assert result != null;
        projectId = result.getId();
        // 校验日志
        checkLog(projectId, OperationLogType.ADD);

        compareProjectDTO(projects.get(0), result);
        //校验资源池
        ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
        projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectTestResourcePool> projectTestResourcePools = projectTestResourcePoolMapper.selectByExample(projectTestResourcePoolExample);
        Assertions.assertTrue(projectTestResourcePools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList().contains("resourcePoolId"));

        project.setName("testAddProjectSuccess1");
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_ADD, addProject, project);
    }

    public void assertionsRefTemplateCreate(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);

        // 校验是否初始化了项目字段
        List<CustomField> orgFields = baseCustomFieldService.getByScopeId(project.getOrganizationId());
        List<CustomField> projectFields = baseCustomFieldService.getByScopeId(projectId);
        Assertions.assertEquals(orgFields.size(), projectFields.size());
        orgFields.forEach(orgField -> {
            CustomField projectField = projectFields.stream()
                    .filter(field -> StringUtils.equals(field.getRefId(), orgField.getId()))
                    .findFirst()
                    .get();

            CustomField copyField = BeanUtils.copyBean(new CustomField(), orgField);
            copyField.setId(projectField.getId());
            copyField.setScopeId(projectId);
            copyField.setScopeType(TemplateScopeType.PROJECT.name());
            copyField.setCreateTime(projectField.getCreateTime());
            copyField.setUpdateTime(projectField.getUpdateTime());
            copyField.setRefId(orgField.getId());
            Assertions.assertEquals(copyField, projectField);
        });

        // 校验是否初始化了项目模板
        List<Template> orgTemplates = baseTemplateService.getByScopeId(project.getOrganizationId());
        List<Template> projectTemplates = baseTemplateService.getByScopeId(projectId);
        Assertions.assertEquals(orgTemplates.size(), projectTemplates.size());
        orgTemplates.forEach(orgTemplate -> {
            Template projectTemplate = projectTemplates.stream()
                    .filter(template -> StringUtils.equals(template.getRefId(), orgTemplate.getId()))
                    .findFirst()
                    .get();

            Template copyTemplate = BeanUtils.copyBean(new Template(), orgTemplate);
            copyTemplate.setId(projectTemplate.getId());
            copyTemplate.setScopeId(projectId);
            copyTemplate.setScopeType(TemplateScopeType.PROJECT.name());
            copyTemplate.setUpdateTime(projectTemplate.getUpdateTime());
            copyTemplate.setCreateTime(projectTemplate.getCreateTime());
            copyTemplate.setRefId(orgTemplate.getId());
            Assertions.assertEquals(copyTemplate, projectTemplate);
        });
    }

    public void assertionsInitTemplate(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        // 校验是否初始化了项目字段
        List<CustomField> fields = baseCustomFieldService.getByScopeId(project.getId());
        Assertions.assertEquals(fields.size(), DefaultFunctionalCustomField.values().length);
        for (DefaultFunctionalCustomField value : DefaultFunctionalCustomField.values()) {
            CustomField customField = fields.stream()
                    .filter(field -> StringUtils.equals(field.getName(), value.getName()))
                    .findFirst().get();
            Assertions.assertEquals(customField.getType(), value.getType().name());
        }
        // 校验是否初始化了项目模板
        List<Template> templates = baseTemplateService.getByScopeId(project.getId());
        Assertions.assertEquals(templates.size(), TemplateScene.values().length);
        for (TemplateScene scene : TemplateScene.values()) {
            Template template = templates.stream()
                    .filter(field -> StringUtils.equals(field.getScene(), scene.name()))
                    .findFirst().get();
            Assertions.assertNotNull(template);
        }
    }

    private void changeOrgTemplateEnable(boolean enable) {
        changeOrgTemplateEnable(enable, OrganizationParameterConstants.ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
        changeOrgTemplateEnable(enable, OrganizationParameterConstants.ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
        changeOrgTemplateEnable(enable, OrganizationParameterConstants.ORGANIZATION_API_TEMPLATE_ENABLE_KEY);
        changeOrgTemplateEnable(enable, OrganizationParameterConstants.ORGANIZATION_UI_TEMPLATE_ENABLE_KEY);
        changeOrgTemplateEnable(enable, OrganizationParameterConstants.ORGANIZATION_TEST_PLAN_TEMPLATE_ENABLE_KEY);
    }

    private void changeOrgTemplateEnable(boolean enable, String organizationTemplateEnableKey) {
        if (enable) {
            organizationParameterMapper.deleteByPrimaryKey(DEFAULT_ORGANIZATION_ID, organizationTemplateEnableKey);
        } else {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            organizationParameter.setParamKey(organizationTemplateEnableKey);
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            if (organizationParameterMapper.selectByPrimaryKey(DEFAULT_ORGANIZATION_ID, organizationTemplateEnableKey) == null) {
                organizationParameterMapper.insert(organizationParameter);
            }
        }
    }

    @Test
    @Order(2)
    /**
     * 测试添加项目失败的用例
     */
    public void testAddProjectError() throws Exception {
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID,"nameError", "description", true, List.of("admin"));
        this.responsePost(addProject, project);
        //项目名称存在 500
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID,"nameError", "description", true, List.of("admin"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorAdd(null, null, null, true, List.of("admin"));
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, null, null, true, List.of("admin"));
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);
        //项目成员在系统中不存在
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "name", null, true, List.of("admin", "admin1", "admin3"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);
        //资源池不存在
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "pool", null, true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId3"));
        this.requestPost(addProject, project, ERROR_REQUEST_MATCHER);

    }
    @Test
    @Order(3)
    public void testGetProject() throws Exception {
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID,"getName", "description", true, List.of("admin"));
        List<String> moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("uiTest");
        project.setModuleIds(moduleIds);
        MvcResult mvcResult = this.responsePost(addProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert result != null;
        projectId = result.getId();
        mvcResult = this.responseGet(getProject + projectId);
        Project getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert getProjects != null;
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), projectId));
        mvcResult = this.responseGet(getProject + "projectId");
        getProjects = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        assert getProjects != null;
        Assertions.assertTrue(StringUtils.equals(getProjects.getId(), "projectId"));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, getProject + projectId);
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
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(10);
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
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的organizationName是最小的
        assert returnPager != null;
        projectDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ProjectDTO.class);
        String firstOrganizationName = projectDTOS.get(0).getOrganizationName();
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
        firstOrganizationName = projectDTOS.get(0).getOrganizationName();
        for (ProjectDTO projectDTO : projectDTOS) {
            Assertions.assertFalse(projectDTO.getOrganizationName().compareTo(firstOrganizationName) > 0);
        }


        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, getProjectList, projectRequest);
    }

    @Test
    @Order(6)
    public void testPageError() throws Exception {
        //当前页码不大于0
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setPageSize(5);
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //当前页数大于100
        projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(101);
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(5);
        projectRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(getProjectList, projectRequest, BAD_REQUEST_MATCHER);
    }

    @Test
    @Order(7)
    public void testUpdateProject() throws Exception {
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1","TestName", "Edit name", true, List.of("admin", "admin1"));
        MvcResult mvcResult = this.responsePost(updateProject, project);
        ProjectDTO result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId1").andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(List.of("admin", "admin1")));
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(DEFAULT_ORGANIZATION_ID).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(List.of("admin", "admin1")));

        //用户id为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "TestNameUserIdIsNull", "Edit name", true, new ArrayList<>());
        Project projectExtend = projectMapper.selectByPrimaryKey("projectId1");
        projectExtend.setModuleSetting(null);
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId1").andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //断言userRoleRelations是空的
        Assertions.assertTrue(userRoleRelations.isEmpty());

        // 修改模块设置
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "Module", "Edit name", true, new ArrayList<>());
        List<String> moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("uiTest");
        moduleIds.add("loadTest");
        project.setModuleIds(moduleIds);
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId1").andRoleIdEqualTo(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //断言userRoleRelations是空的
        Assertions.assertTrue(userRoleRelations.isEmpty());
        //断言模块设置
        projectExtend = projectMapper.selectByPrimaryKey("projectId1");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleIds));

        //设置资源池
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId5", "updatePools", "updatePools", true, new ArrayList<>());
        project.setResourcePoolIds(List.of("resourcePoolId","resourcePoolId1"));
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        //校验资源池
        ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
        projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo("projectId5");
        List<ProjectTestResourcePool> projectTestResourcePools = projectTestResourcePoolMapper.selectByExample(projectTestResourcePoolExample);
        Assertions.assertTrue(projectTestResourcePools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList().containsAll(project.getResourcePoolIds()));


        // @@校验权限
        project.setName("TestName2");
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE, updateProject, project);
    }

    @Test
    @Order(8)
    public void testUpdateProjectError() throws Exception {
        //项目名称存在 500
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2","TestName2", "description", true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //参数组织Id为空
        project = this.generatorUpdate(null, "projectId",null, null, true , List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目Id为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, null,null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目名称为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId",null, null, true, List.of("admin"));
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);
        //项目不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "1111","123", null, true, List.of("admin"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //资源池不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2","pool-edit", "description", true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId3"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(9)
    public void testDeleteProject() throws Exception {
        String id = "projectId";
        MvcResult mvcResult = this.responseGet(deleteProject + id);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Project currentProject = projectMapper.selectByPrimaryKey(id);
        Assertions.assertEquals(currentProject.getDeleted(), true);
        Assertions.assertEquals(currentProject.getId(), id);
        Assertions.assertEquals(1, count);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE, deleteProject + id);
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
        String id = "projectId";
        MvcResult mvcResult = this.responseGet(revokeProject + id);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Project currentProject = projectMapper.selectByPrimaryKey(id);
        Assertions.assertEquals(currentProject.getDeleted(), false);
        Assertions.assertEquals(currentProject.getId(), id);
        Assertions.assertEquals(1, count);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_RECOVER, revokeProject + id);
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
        projectAddMemberRequest.setProjectId("projectId");
        List<String> userIds = List.of("admin1", "admin2");
        projectAddMemberRequest.setUserIds(userIds);
        this.requestPost(addProjectMember, projectAddMemberRequest, status().isOk());
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo("projectId").andRoleIdEqualTo(InternalUserRole.PROJECT_MEMBER.getValue());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(userIds));
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().containsAll(userIds));
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD, addProjectMember, projectAddMemberRequest);
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
        memberRequest.setProjectId("projectId");
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
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, getProjectMemberList, memberRequest);

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
        //项目Id不存在
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(5);
        memberRequest.setProjectId("projectId111");
        MvcResult mvcResult = this.responsePost(getProjectMemberList, memberRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        Assertions.assertEquals(0, returnPager.getTotal());
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
        String projectId = "projectId1";
        String userId = "admin1";
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(userId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        MvcResult mvcResult = this.responseGet(removeProjectMember + projectId + "/" + userId);
        int count = parseObjectFromMvcResult(mvcResult, Integer.class);
        Assertions.assertEquals(count, userRoleRelations.size());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE, removeProjectMember + projectId + "/" + userId);
    }

    @Test
    @Order(18)
    public void testRemoveProjectMemberError() throws Exception{
        String projectId = "projectId1";
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
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE, disableProject + id);
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
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE, enableProject + id);
    }

    @Test
    @Order(20)
    public void enableError() throws Exception {
        String id = "1111";
        this.responseGet(enableProject + id, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(21)
    public void testUserList() throws Exception {
        String keyword = "a";
        this.requestGetWithOkAndReturn(userList + "?keyword=" + keyword);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, userList + "?keyword=" + keyword);
    }

    @Test
    @Order(22)
    public void testGetOptions() throws Exception {
        this.requestGetWithOkAndReturn(getPoolOptions);
        this.requestGetWithOkAndReturn(getPoolOptions + "?organizationId=" + DEFAULT_ORGANIZATION_ID);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, getPoolOptions + "?organizationId=" + DEFAULT_ORGANIZATION_ID);
    }

    @Test
    @Order(23)
    public void testDeleteForce() {
        String id = "projectId";
        // 设置成过期，强制删除
        Project project = new Project();
        project.setId(id);
        project.setDeleteTime(1L);
        project.setDeleted(true);
        projectMapper.updateByPrimaryKeySelective(project);

        // 直接调用删除项目资源的定时任务，校验资源是否删除
        cleanProjectJob.cleanupProject();
        Assertions.assertTrue(CollectionUtils.isEmpty(baseCustomFieldService.getByScopeId(id)));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseTemplateService.getByScopeId(id)));
    }
}
