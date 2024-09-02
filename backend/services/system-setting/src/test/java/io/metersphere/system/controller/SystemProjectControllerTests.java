package io.metersphere.system.controller;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.*;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.job.CleanProjectJob;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.*;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private final static String updateName = prefix + "/rename";
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
    private OrganizationCustomFieldService organizationCustomFieldService;
    @Resource
    private OrganizationTemplateService organizationTemplateService;
    @Resource
    private BaseStatusItemService baseStatusItemService;
    @Resource
    private BaseStatusDefinitionService baseStatusDefinitionService;
    @Resource
    private BaseStatusFlowService baseStatusFlowService;
    @Resource
    private BaseStatusFlowSettingService baseStatusFlowSettingService;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;
    @Resource
    private CleanProjectJob cleanProjectJob;
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtGetPosService extGetPosService;

    @Autowired
    public SystemProjectControllerTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
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
            initProject.setDeleted(true);
            initProject.setDeleteTime(System.currentTimeMillis());
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
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
        initData();
        addMultipleCustomField();
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "name", "description", true, List.of("admin"));
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
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(DEFAULT_ORGANIZATION_ID).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
        userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        Assertions.assertTrue(userRoleRelations.stream().map(UserRoleRelation::getUserId).toList().contains("admin"));
        Project currentProject = projectMapper.selectByPrimaryKey(projectId);
        Assertions.assertNull(currentProject.getModuleSetting());
        // 项目模板未启用时，校验是否创建了项目模板
        assertionsRefTemplateCreate(projectId);
        assertionsRefStatusFlowSetting(projectId);

        //userId为空的时候
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "userIdNotNull", "description", true, List.of("admin"));
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

        compareProjectDTO(projects.getFirst(), result);
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
        List<StatusItemDTO> statusItemDTOS = baseStatusFlowSettingService.getStatusFlowSetting(projectId, TemplateScene.BUG.name());
        OrganizationStatusFlowSettingControllerTest.assertDefaultStatusFlowSettingInit(statusItemDTOS);

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

        compareProjectDTO(projects.getFirst(), result);
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

        compareProjectDTO(projects.getFirst(), result);
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

    public void assertionsRefStatusFlowSetting(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);

        // 校验是否初始化了项目状态项
        List<StatusItem> orgStatusItems = baseStatusItemService.getByScopeId(project.getOrganizationId());
        List<StatusItem> projectStatusItems = baseStatusItemService.getByScopeId(projectId);
        Assertions.assertEquals(orgStatusItems.size(), projectStatusItems.size());
        orgStatusItems.forEach(orgStatusItem -> {
            StatusItem projectStatusItem = projectStatusItems.stream()
                    .filter(statusItem -> StringUtils.equals(statusItem.getRefId(), orgStatusItem.getId()))
                    .findFirst()
                    .get();

            StatusItem copyStatusItem = BeanUtils.copyBean(new StatusItem(), orgStatusItem);
            copyStatusItem.setId(projectStatusItem.getId());
            copyStatusItem.setScopeId(projectId);
            copyStatusItem.setScopeType(TemplateScopeType.PROJECT.name());
            copyStatusItem.setRefId(orgStatusItem.getId());
            copyStatusItem.setPos(projectStatusItem.getPos());
            Assertions.assertEquals(copyStatusItem, projectStatusItem);
        });

        Map<String, String> refMap = projectStatusItems.stream()
                .collect(Collectors.toMap(StatusItem::getId, StatusItem::getRefId));
        List<String> orgStatusItemIds = orgStatusItems.stream().map(StatusItem::getId).toList();
        List<String> projectStatusItemIds = projectStatusItems.stream().map(StatusItem::getId).toList();

        // 校验是否初始化了项目状态定义
        List<StatusDefinition> orgStatusDefinitions = baseStatusDefinitionService.getStatusDefinitions(orgStatusItemIds);
        List<StatusDefinition> projectStatusDefinitions = baseStatusDefinitionService.getStatusDefinitions(projectStatusItemIds);
        projectStatusDefinitions.forEach(item -> item.setStatusId(refMap.get(item.getStatusId())));
        orgStatusDefinitions.sort(Comparator.comparing(StatusDefinition::getStatusId));
        projectStatusDefinitions.sort(Comparator.comparing(StatusDefinition::getStatusId));
        Assertions.assertEquals(orgStatusDefinitions, projectStatusDefinitions);

        // 校验是否初始化了项目状态流转
        List<StatusFlow> orgStatusFlows = baseStatusFlowService.getStatusFlows(orgStatusItemIds);
        List<StatusFlow> projectStatusFlows = baseStatusFlowService.getStatusFlows(projectStatusItemIds);
        orgStatusFlows.forEach(item -> item.setId(null));
        projectStatusFlows.forEach(item -> {
            item.setId(null);
            item.setFromId(refMap.get(item.getFromId()));
            item.setToId(refMap.get(item.getToId()));
        });
        orgStatusFlows.sort(Comparator.comparing(a -> (a.getFromId() + a.getToId())));
        projectStatusFlows.sort(Comparator.comparing(a -> (a.getFromId() + a.getToId())));
        Assertions.assertEquals(orgStatusFlows, projectStatusFlows);
    }

    public void assertionsInitTemplate(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        // 校验是否初始化了项目字段
        List<CustomField> fields = baseCustomFieldService.getByScopeId(project.getId());
        Assertions.assertEquals(fields.size(), DefaultFunctionalCustomField.values().length + DefaultBugCustomField.values().length);
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

    /**
     * 添加多选的自定义字段
     * 校验初始化项目模板时，数组类型的默认值是否正常转化
     */
    private void addMultipleCustomField() {
        // 新建一个多选的自定义字段
        String scene = TemplateScene.TEST_PLAN.name();
        CustomField customField = new CustomField();
        customField.setScene(scene);
        customField.setName("test project default value");
        customField.setType(CustomFieldType.MULTIPLE_SELECT.name());
        customField.setScopeId(DEFAULT_ORGANIZATION_ID);
        customField.setEnableOptionKey(true);
        CustomFieldOptionRequest customFieldOptionRequest1 = new CustomFieldOptionRequest();
        customFieldOptionRequest1.setValue("1");
        customFieldOptionRequest1.setText("test1");
        customFieldOptionRequest1.setPos(1);
        CustomFieldOptionRequest customFieldOptionRequest2 = new CustomFieldOptionRequest();
        customFieldOptionRequest2.setValue("2");
        customFieldOptionRequest2.setText("test2");
        customFieldOptionRequest2.setPos(2);
        customField.setCreateUser("admin");
        List<CustomFieldOptionRequest> optionRequests = Arrays.asList(customFieldOptionRequest1, customFieldOptionRequest2);
        organizationCustomFieldService.add(customField, optionRequests);

        // 关联模板
        TemplateUpdateRequest request = new TemplateUpdateRequest();
        Template template = baseTemplateService.getTemplates(DEFAULT_ORGANIZATION_ID, scene).getFirst();
        TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
        templateCustomFieldRequest.setFieldId(customField.getId());
        templateCustomFieldRequest.setRequired(false);
        templateCustomFieldRequest.setDefaultValue(List.of("1", "2"));
        request.setId(template.getId());
        request.setCustomFields(List.of(templateCustomFieldRequest));
        organizationTemplateService.update(request);
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
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "nameError", "description", true, List.of("admin"));
        this.responsePost(addProject, project);
        //项目名称存在 500
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "nameError", "description", true, List.of("admin"));
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
        //成员为空
        project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "name", null, true, null);
        this.requestPost(addProject, project, BAD_REQUEST_MATCHER);

    }

    @Test
    @Order(3)
    public void testGetProject() throws Exception {
        AddProjectRequest project = this.generatorAdd(DEFAULT_ORGANIZATION_ID, "getName", "description", true, List.of("admin"));
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

        //查询不存在的组织id
        projectRequest.setOrganizationId("111111");
        mvcResult = this.responsePost(getProjectList, projectRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值为

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
        //当前页数大于500
        projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(501);
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
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "TestName", "Edit name", true, List.of("admin", "admin1"));
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

        // 修改模块设置
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId1", "TestName2", "Edit name", true, List.of("admin"));
        List<String> moduleIds = new ArrayList<>();
        moduleIds.add("apiTest");
        moduleIds.add("uiTest");
        moduleIds.add("loadTest");
        project.setModuleIds(moduleIds);
        mvcResult = this.responsePost(updateProject, project);
        result = parseObjectFromMvcResult(mvcResult, ProjectDTO.class);
        currentProject = projectMapper.selectByPrimaryKey(project.getId());
        compareProjectDTO(currentProject, result);
        //断言模块设置
        Project projectExtend = projectMapper.selectByPrimaryKey("projectId1");
        Assertions.assertEquals(projectExtend.getModuleSetting(), JSON.toJSONString(moduleIds));

        //设置资源池
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId5", "updatePools", "updatePools", true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId", "resourcePoolId1"));
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
        UpdateProjectRequest project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2", "TestName2", "description", true, List.of("admin"));
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
        //资源池不存在
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2", "pool-edit", "description", true, List.of("admin"));
        project.setResourcePoolIds(List.of("resourcePoolId3"));
        this.requestPost(updateProject, project, ERROR_REQUEST_MATCHER);
        //成员为空
        project = this.generatorUpdate(DEFAULT_ORGANIZATION_ID, "projectId2", "name", null, true, null);
        this.requestPost(updateProject, project, BAD_REQUEST_MATCHER);

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
    public void testAddProjectMember() throws Exception {
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        List<String> userIds = List.of("admin1", "admin2");
        projectAddMemberRequest.setUserIds(userIds);
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, status().isOk());
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD, addProjectMember, projectAddMemberRequest);
    }

    @Test
    @Order(14)
    public void testAddProjectMemberError() throws Exception {
        //项目Id为空
        ProjectAddMemberRequest projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId(null);
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id为空
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, BAD_REQUEST_MATCHER);
        //用户Id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId");
        projectAddMemberRequest.setUserIds(List.of("admin3"));
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_MEMBER.getValue()));
        this.requestPost(addProjectMember, projectAddMemberRequest, ERROR_REQUEST_MATCHER);
        //项目id不存在
        projectAddMemberRequest = new ProjectAddMemberRequest();
        projectAddMemberRequest.setProjectId("projectId111");
        projectAddMemberRequest.setUserIds(List.of("admin1"));
        projectAddMemberRequest.setUserRoleIds(List.of(InternalUserRole.PROJECT_MEMBER.getValue()));
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
        Assertions.assertTrue(((List<UserExtendDTO>) returnPager.getList()).size() <= memberRequest.getPageSize());
        memberRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        mvcResult = this.responsePost(getProjectMemberList, memberRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //第一个数据的createTime是最大的
        List<UserExtendDTO> userExtendDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), UserExtendDTO.class);
        long firstCreateTime = userExtendDTOS.getFirst().getCreateTime();
        for (UserExtendDTO userExtendDTO : userExtendDTOS) {
            Assertions.assertFalse(userExtendDTO.getCreateTime() > firstCreateTime);
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
        //当前页数大于501
        memberRequest = new ProjectMemberRequest();
        memberRequest.setCurrent(1);
        memberRequest.setPageSize(501);
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
    public void testRemoveProjectMember() throws Exception {
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
    public void testRemoveProjectMemberError() throws Exception {
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
        //用户id为最后一个管理员
        projectId = "projectId5";
        userId = "admin";
        this.responseGet(removeProjectMember + projectId + "/" + userId, ERROR_REQUEST_MATCHER);
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
        this.responseGet(enableProject + id, status().isOk());
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
        ProjectPoolRequest projectPoolRequest = new ProjectPoolRequest();
        this.requestPost(getPoolOptions, projectPoolRequest, BAD_REQUEST_MATCHER);
        projectPoolRequest.setModulesIds(List.of("apiTest", "uiTest", "loadTest"));
        projectPoolRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        this.responsePost(getPoolOptions, projectPoolRequest);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, getPoolOptions, projectPoolRequest);
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
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusItemService.getByScopeId(id)));
    }

    @Test
    @Order(24)
    public void testUpdateName() throws Exception {
        UpdateProjectNameRequest project = new UpdateProjectNameRequest();
        project.setId("projectId1");
        project.setName("updateName");
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        requestPost(updateName, project);
        Project currentProject = projectMapper.selectByPrimaryKey(project.getId());
        Assertions.assertEquals(currentProject.getName(), project.getName());
        checkLog(project.getId(), OperationLogType.UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE, updateName, project);
    }

    @Test
    @Order(25)
    public void testUpdateNameError() throws Exception {
        //项目名称存在 500
        UpdateProjectNameRequest project = new UpdateProjectNameRequest();
        project.setId("projectId2");
        project.setName("updateName");
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
    public void test() throws Exception {
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("test-apiTestCaseId");
        apiTestCase.setApiDefinitionId("apiDefinitionId");
        apiTestCase.setProjectId(DEFAULT_PROJECT_ID);
        apiTestCase.setName("test-apiTestCaseName");
        apiTestCase.setPriority("P0");
        apiTestCase.setStatus("Underway");
        apiTestCase.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID + "_" + "100001", ApplicationNumScope.API_TEST_CASE));
        apiTestCase.setPos(100L);
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setCreateUser("admin");
        apiTestCase.setUpdateUser("admin");
        apiTestCase.setVersionId("1.0");
        apiTestCase.setDeleted(false);
        apiTestCaseMapper.insertSelective(apiTestCase);
        ApiTestCase anotherCase = new ApiTestCase();
        anotherCase.setId("test-apiTestCaseId1");
        anotherCase.setApiDefinitionId("apiDefinitionId");
        anotherCase.setProjectId(DEFAULT_PROJECT_ID);
        anotherCase.setName("test-apiTestCaseName1");
        anotherCase.setPriority("P0");
        anotherCase.setStatus("Underway");
        anotherCase.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID + "_" + "100001", ApplicationNumScope.API_TEST_CASE));
        anotherCase.setPos(120L);
        anotherCase.setCreateTime(System.currentTimeMillis());
        anotherCase.setUpdateTime(System.currentTimeMillis());
        anotherCase.setCreateUser("admin");
        anotherCase.setUpdateUser("admin");
        anotherCase.setVersionId("1.0");
        anotherCase.setDeleted(false);
        apiTestCaseMapper.insertSelective(anotherCase);
        PosRequest request = new PosRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setTargetId(apiTestCase.getId());
        request.setMoveId(anotherCase.getId());
        request.setMoveMode("AFTER");

        ServiceUtils.updatePosField(request,
                ApiTestCase.class,
                apiTestCaseMapper::selectByPrimaryKey,
                extGetPosService::getPrePos,
                extGetPosService::getLastPos,
                apiTestCaseMapper::updateByPrimaryKeySelective);

        request.setMoveMode("BEFORE");
        ServiceUtils.updatePosField(request,
                ApiTestCase.class,
                apiTestCaseMapper::selectByPrimaryKey,
                extGetPosService::getPrePos,
                extGetPosService::getLastPos,
                apiTestCaseMapper::updateByPrimaryKeySelective);
        apiTestCaseMapper.deleteByPrimaryKey(apiTestCase.getId());
        apiTestCaseMapper.deleteByPrimaryKey(anotherCase.getId());
    }


    @Test
    @Order(17)
    public void getProjectListByOrgSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(prefix + "/list")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectList));
    }
}
