package io.metersphere.project.controller;

import io.metersphere.project.dto.ProjectTemplateDTO;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.service.ProjectTemplateLogService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.OrganizationTemplateControllerTests;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.param.TemplateUpdateRequestDefinition;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.OrganizationParameter;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.BaseTemplateCustomFieldService;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;
import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectTemplateControllerTests extends BaseTest {
    private static final String BASE_PATH = "/project/template/";
    private static final String LIST = "list/{0}/{1}";
    private static final String SET_DEFAULT = "set-default/{0}/{1}";
    protected static final String ENABLE_CONFIG = "enable/config/{0}";
    protected static final String UPLOAD_TEMP_IMG = "upload/temp/img";
    protected static final String IMG_PREVIEW = "/img/preview/{0}/{1}/{2}";

    @Resource
    private TemplateMapper templateMapper;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private ProjectTemplateLogService projectTemplateLogService;
    @Resource
    private BasePluginTestService basePluginTestService;

    private static Template addTemplate;
    private static Template anotherTemplateField;
    private static Template defaultTemplate;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        // @@校验没有数据的情况
        MvcResult mvcResult = this.requestGetWithOkAndReturn(LIST, DEFAULT_PROJECT_ID, TemplateScene.FUNCTIONAL.name());
        List<Template> templates = getResultDataArray(mvcResult, Template.class);
        this.defaultTemplate = templates.getFirst();
        this.requestGetWithOkAndReturn(LIST, DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
    }

    @Test
    @Order(0)
    public void uploadTempFile() throws Exception {
        // 准备数据，上传文件管理文件
        MockMultipartFile file = new MockMultipartFile("file", IDGenerator.nextStr() + "_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        // @@请求成功
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, UPLOAD_TEMP_IMG, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_TEMPLATE_ADD, UPLOAD_TEMP_IMG, file);

        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, fileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, fileId, false)).andExpect(status().isOk());
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP_IMG, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        // 开启项目模板
        changeOrgTemplateEnable(false);
        String scene = TemplateScene.FUNCTIONAL.name();
        // @@请求成功
        TemplateUpdateRequest request = new TemplateUpdateRequest();
        TemplateCustomFieldRequest templateCustomFieldRequest = getTemplateCustomFieldRequest(scene);
        request.setScene(scene);
        request.setName("test");
        request.setRemark("AAA");
        request.setEnableThirdPart(true);
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setCustomFields(List.of(templateCustomFieldRequest));
        request.setSystemFields(OrganizationTemplateControllerTests.getTemplateSystemCustomFieldRequests());

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);

        Template resultData = getResultData(mvcResult, Template.class);
        Template template = templateMapper.selectByPrimaryKey(resultData.getId());
        this.addTemplate = template;
        // 校验请求成功数据
        request.setId(template.getId());
        TemplateUpdateRequest copyRequest = BeanUtils.copyBean(new TemplateUpdateRequest(), request);
        copyRequest.setCustomFields(null);
        copyRequest.setSystemFields(null);
        Assertions.assertEquals(copyRequest, BeanUtils.copyBean(new TemplateUpdateRequest(), template));
        Assertions.assertEquals(template.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(template.getInternal(), false);
        Assertions.assertEquals(template.getScopeType(), TemplateScopeType.PROJECT.name());
        OrganizationTemplateControllerTests.assertTemplateCustomFields(request, template);

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), TEMPLATE_EXIST);

        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@校验组织是否存在
        request.setScopeId("1111");
        request.setName("test1");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // 插入另一条数据，用户更新时重名校验
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setCustomFields(null);
        request.setSystemFields(null);
        String uploadFileId = doUploadTempFile(OrganizationTemplateControllerTests.getMockMultipartFile("api-add-file_upload.JPG"));
        request.setUploadImgFileIds(List.of(uploadFileId));
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anotherTemplateField = templateMapper.selectByPrimaryKey(getResultData(anotherMvcResult, Template.class).getId());
        assertUploadFile(uploadFileId, "api-add-file_upload.JPG");
        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, uploadFileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, uploadFileId, false)).andExpect(status().isOk());
        request.setUploadImgFileIds(null);

        // @@校验日志
        checkLog(this.addTemplate.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(TemplateUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_ADD, DEFAULT_ADD, request);
    }


    /**
     * 校验上传的文件
     *
     */
    public static void assertUploadFile(String fileId, String fileName) throws Exception {
        String projectTemplateImgDir = DefaultRepositoryDir.getProjectTemplateImgDir(DEFAULT_PROJECT_ID);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(projectTemplateImgDir + "/" + fileId);
        fileRequest.setFileName(fileName);
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
    }

    private TemplateCustomFieldRequest getTemplateCustomFieldRequest(String scene) {
        List<CustomField> customFields = baseCustomFieldService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, scene);
        CustomField customField = customFields.stream()
                .filter(item -> item.getName().equals("functional_priority"))
                .findFirst()
                .get();
        TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
        BeanUtils.copyBean(templateCustomFieldRequest, customField);
        templateCustomFieldRequest.setFieldId(customField.getId());
        templateCustomFieldRequest.setRequired(true);
        templateCustomFieldRequest.setApiFieldId("aaa");
        templateCustomFieldRequest.setDefaultValue("P0");
        return templateCustomFieldRequest;
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        // @@请求成功
        String scene = TemplateScene.FUNCTIONAL.name();
        TemplateUpdateRequest request = new TemplateUpdateRequest();
        request.setId(addTemplate.getId());
        request.setScene(scene);
        request.setName("test2");
        request.setRemark("AAA1");
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setScene(TemplateScene.UI.name());
        request.setEnableThirdPart(true);
        request.setCustomFields(new ArrayList<>(0));
        request.setSystemFields(OrganizationTemplateControllerTests.getTemplateSystemCustomFieldRequests());
        request.getSystemFields().getFirst().setDefaultValue("update");
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Template template = templateMapper.selectByPrimaryKey(request.getId());
        // 校验请求成功数据
        Assertions.assertEquals(template.getName(), request.getName());
        Assertions.assertEquals(template.getRemark(), request.getRemark());
        Assertions.assertEquals(template.getScopeId(), DEFAULT_PROJECT_ID);
        Assertions.assertEquals(template.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(template.getInternal(), false);
        Assertions.assertEquals(template.getScopeType(), TemplateScopeType.PROJECT.name());
        Assertions.assertEquals(template.getScene(), scene);
        Assertions.assertEquals(template.getEnableThirdPart(), request.getEnableThirdPart());
        OrganizationTemplateControllerTests.assertTemplateCustomFields(request, template);

        // 带字段的更新
        TemplateCustomFieldRequest templateCustomFieldRequest = getTemplateCustomFieldRequest(scene);
        request.setCustomFields(List.of(templateCustomFieldRequest));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        OrganizationTemplateControllerTests.assertTemplateCustomFields(request, template);

        TemplateExample example = new TemplateExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("functional_default");
        TemplateUpdateRequest internalRequest = BeanUtils.copyBean(new TemplateUpdateRequest(), templateMapper.selectByExample(example).getFirst());
        internalRequest.setName("aaaa");
        this.requestPostWithOk(DEFAULT_UPDATE, internalRequest);
        Assertions.assertEquals(templateMapper.selectByExample(example).getFirst().getInternal(), true);
        // 内置字段名称不能修改
        Assertions.assertEquals(templateMapper.selectByExample(example).getFirst().getName(), "functional_default");

        // 不更新字段
        request.setCustomFields(null);
        String uploadFileId = doUploadTempFile(OrganizationTemplateControllerTests.getMockMultipartFile("api-add-file_upload.JPG"));
        request.setUploadImgFileIds(List.of(uploadFileId));
        request.setScopeId(DEFAULT_PROJECT_ID);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Assertions.assertEquals(baseTemplateCustomFieldService.getByTemplateId(template.getId()).size(), 3);
        assertUploadFile(uploadFileId, "api-add-file_upload.JPG");
        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, uploadFileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_PROJECT_ID, uploadFileId, false)).andExpect(status().isOk());
        request.setUploadImgFileIds(null);

        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@重名校验异常
        request.setName(anotherTemplateField.getName());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), TEMPLATE_EXIST);

        // @校验 NOT_FOUND 异常
        request.setId("1111");
        request.setName("1111");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        updatedGroupParamValidateTest(TemplateUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    public void list() throws Exception {

        String scene = TemplateScene.FUNCTIONAL.name();
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(LIST, DEFAULT_PROJECT_ID, scene)
                .andReturn();
        // 校验数据是否正确
        List<ProjectTemplateDTO> resultList = getResultDataArray(mvcResult, ProjectTemplateDTO.class);
        List<Template> dbTemplates = baseTemplateService.getTemplates(DEFAULT_PROJECT_ID, scene);
        assertListTemplate(scene, resultList, dbTemplates);

        // 校验获取模板选项方法
        List<ProjectTemplateOptionDTO> projectTemplateOptions = projectTemplateService.getOption(DEFAULT_PROJECT_ID, scene);
        assertTemplateOption(resultList, projectTemplateOptions);

        scene = TemplateScene.BUG.name();
        // @@缺陷模板请求成功
        mvcResult = this.requestGetWithOk(LIST, DEFAULT_PROJECT_ID, scene)
                .andReturn();
        // 校验数据是否正确
        resultList = getResultDataArray(mvcResult, ProjectTemplateDTO.class);
        dbTemplates = baseTemplateService.getTemplates(DEFAULT_PROJECT_ID, scene);
        assertListTemplate(scene, resultList, dbTemplates);

        // 校验获取模板选项方法
        projectTemplateOptions = projectTemplateService.getOption(DEFAULT_PROJECT_ID, scene);
        assertTemplateOption(resultList, projectTemplateOptions);

        // 上传jira插件，添加jira集成
        basePluginTestService.addJiraPlugin();
        basePluginTestService.addServiceIntegration(DEFAULT_ORGANIZATION_ID);
        // @@校验配置服务集成后，是否有jira模板
        mvcResult = this.requestGetWithOk(LIST, DEFAULT_PROJECT_ID, scene)
                .andReturn();
        resultList = getResultDataArray(mvcResult, ProjectTemplateDTO.class);
        // 没有配置项目信息，校验是否有jira模板
        Assertions.assertTrue(resultList.size() == dbTemplates.size());
        Assertions.assertTrue(resultList.stream().filter(item -> StringUtils.equals(item.getId(), "jira")).count() == 0);

        // 校验获取模板选项方法
        projectTemplateOptions = projectTemplateService.getOption(DEFAULT_PROJECT_ID, scene);
        assertTemplateOption(resultList, projectTemplateOptions);

        // 配置项目信息
        basePluginTestService.enableProjectBugConfig(DEFAULT_PROJECT_ID);
        // @@校验配置项目信息后，是否有jira模板
        mvcResult = this.requestGetWithOk(LIST, DEFAULT_PROJECT_ID, scene)
                .andReturn();
        Template jiraTemplate = getResultDataArray(mvcResult, Template.class).stream()
                .filter(item -> StringUtils.equals(item.getId(), "jira"))
                .findFirst()
                .get();
        Assertions.assertNotNull(jiraTemplate);

        // @@校验组织是否存在
        assertErrorCode(this.requestGet(LIST, "1111", scene), NOT_FOUND);

        // @@校验使用场景不合法
        assertErrorCode(this.requestGet(LIST, DEFAULT_PROJECT_ID, "111"), TEMPLATE_SCENE_ILLEGAL);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, LIST, DEFAULT_PROJECT_ID, scene);
    }

    private static void assertTemplateOption(List<ProjectTemplateDTO> resultList, List<ProjectTemplateOptionDTO> projectTemplateOptions) {
        Map<String, ProjectTemplateDTO> templateMap = resultList.stream().collect(Collectors.toMap(Template::getId, Function.identity()));
        Assertions.assertEquals(projectTemplateOptions.size(), resultList.size());
        projectTemplateOptions.forEach(option -> {
            ProjectTemplateDTO template = templateMap.get(option.getId());
            Assertions.assertEquals(option.getId(), template.getId());
            Assertions.assertEquals(option.getName(), template.getName());
            Assertions.assertEquals(option.getEnableDefault(), template.getEnableDefault());
        });
    }

    private void assertListTemplate(String scene, List<ProjectTemplateDTO> resultList, List<Template> dbTemplates) {
        List<String> userIds = dbTemplates.stream().map(Template::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        Assertions.assertTrue(resultList.size() == dbTemplates.size());
        for (int i = 0; i < resultList.size(); i++) {
            Template resultItem = resultList.get(i);
            Template template = dbTemplates.get(i);
            template.setCreateUser(userNameMap.get(template.getCreateUser()));
            if (template.getInternal()) {
                // 校验内置用户名称是否翻译
                template.setName(baseTemplateService.translateInternalTemplate());
            }
            Assertions.assertEquals(template, resultItem);
            Assertions.assertEquals(resultItem.getScene(), scene);
        }
    }

    @Test
    @Order(4)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, addTemplate.getId());
        // 校验数据是否正确
        TemplateDTO templateDTO = getResultData(mvcResult, TemplateDTO.class);
        OrganizationTemplateControllerTests.assertGetTemplateDTO(templateDTO);

        TemplateExample example = new TemplateExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("functional_default");
        mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, templateMapper.selectByExample(example).getFirst().getId());
        templateDTO = getResultData(mvcResult, TemplateDTO.class);
        Assertions.assertEquals(templateDTO.getName(), Translator.get("template.default"));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, DEFAULT_GET, templateDTO.getId());
    }

    @Test
    @Order(5)
    public void setDefaultTemplate() throws Exception {
        changeOrgTemplateEnable(false);

        String projectId = DEFAULT_PROJECT_ID;
        String scene = TemplateScene.FUNCTIONAL.name();
        String defaultTemplateId = projectTemplateService.getDefaultTemplateId(projectId, scene);
        // 校验默认没有设置默认模板，使用内置模板
        Assertions.assertNull(defaultTemplateId);
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(projectId, scene);
        Assertions.assertEquals(defaultTemplateDTO.getInternal(), true);

        // @@请求成功
        this.requestGetWithOk(SET_DEFAULT, DEFAULT_PROJECT_ID, addTemplate.getId());
        Template template = templateMapper.selectByPrimaryKey(addTemplate.getId());
        assertSetDefaultTemplate(template);
        defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(projectId, scene);
        Assertions.assertEquals(defaultTemplateDTO.getId(), template.getId());

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.UPDATE, MessageFormat.format(BASE_PATH + SET_DEFAULT, DEFAULT_PROJECT_ID, addTemplate.getId()));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, SET_DEFAULT, DEFAULT_PROJECT_ID, addTemplate.getId());
    }

    @Test
    @Order(6)
    public void delete() throws Exception {
        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addTemplate.getId()), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@校验内置模板删除异常
        Template internalTemplate = projectTemplateService.getInternalTemplate(DEFAULT_PROJECT_ID, TemplateScene.FUNCTIONAL.name());
        assertErrorCode(this.requestGet(DEFAULT_DELETE, internalTemplate.getId()), INTERNAL_TEMPLATE_PERMISSION);

        // @@校验删除默认模板
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addTemplate.getId()), DEFAULT_TEMPLATE_PERMISSION);
        // 设置回来，保证正常删除
        this.requestGetWithOk(SET_DEFAULT, DEFAULT_PROJECT_ID, defaultTemplate.getId());

        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addTemplate.getId());
        Assertions.assertNull(templateMapper.selectByPrimaryKey(addTemplate.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseTemplateCustomFieldService.getByTemplateId(addTemplate.getId())));

        basePluginTestService.deleteJiraPlugin();

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_DELETE, DEFAULT_DELETE, addTemplate.getId());
    }

    @Test
    @Order(7)
    public void getProjectTemplateEnableConfig() throws Exception {
        changeOrgTemplateEnable(true);
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(ENABLE_CONFIG, DEFAULT_PROJECT_ID);
        Map resultData = getResultData(mvcResult, Map.class);
        Assertions.assertEquals(resultData.size(), 2);
        Assertions.assertFalse((Boolean) resultData.get(TemplateScene.FUNCTIONAL.name()));
        changeOrgTemplateEnable(false);
        mvcResult = this.requestGetWithOkAndReturn(ENABLE_CONFIG, DEFAULT_PROJECT_ID);
        Assertions.assertTrue((Boolean) getResultData(mvcResult, Map.class).get(TemplateScene.FUNCTIONAL.name()));
        changeOrgTemplateEnable(true);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(ENABLE_CONFIG,"1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, ENABLE_CONFIG, DEFAULT_PROJECT_ID);
    }

    private void assertSetDefaultTemplate(Template template) {
        String defaultTemplateId = projectTemplateService.getDefaultTemplateId(template.getScopeId(), template.getScene());
        Assertions.assertEquals(defaultTemplateId, template.getId());
    }

    private void changeOrgTemplateEnable(boolean enable) {
        if (enable) {
            organizationParameterMapper.deleteByPrimaryKey(DEFAULT_ORGANIZATION_ID, OrganizationParameterConstants.ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
        } else {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            organizationParameter.setParamKey(OrganizationParameterConstants.ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            if (organizationParameterMapper.selectByPrimaryKey(DEFAULT_ORGANIZATION_ID,
                    OrganizationParameterConstants.ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY) == null) {
                organizationParameterMapper.insert(organizationParameter);
            }
        }
    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_test_template.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getTemplateDTOById() throws Exception {
        projectTemplateService.getTemplateDTOById("test_template_id", DEFAULT_PROJECT_ID, TemplateScene.FUNCTIONAL.name());
        projectTemplateService.getTemplateDTOById("test_template_id_1", "test_project_id_1", TemplateScene.FUNCTIONAL.name());
    }


    @Test
    @Order(3)
    public void getCustomFields() throws Exception {
        projectTemplateService.getTableCustomField("DEFAULT_PROJECT_ID", TemplateScene.FUNCTIONAL.name());
        projectTemplateService.getTableCustomField(DEFAULT_PROJECT_ID, TemplateScene.FUNCTIONAL.name());
        projectTemplateService.getTableCustomField("test_project_id_2", TemplateScene.FUNCTIONAL.name());
    }

    @Test
    @Order(10)
    public void testLog() {
        Assertions.assertEquals(projectTemplateLogService.getOperationLogModule(TemplateScene.API.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE);
        Assertions.assertEquals(projectTemplateLogService.getOperationLogModule(TemplateScene.FUNCTIONAL.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE);
        Assertions.assertEquals(projectTemplateLogService.getOperationLogModule(TemplateScene.BUG.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE);
        Assertions.assertEquals(projectTemplateLogService.getOperationLogModule(TemplateScene.TEST_PLAN.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_TEMPLATE);
        Assertions.assertEquals(projectTemplateLogService.getOperationLogModule(TemplateScene.UI.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_TEMPLATE);
    }
}