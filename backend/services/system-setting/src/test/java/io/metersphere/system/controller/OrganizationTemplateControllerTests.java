package io.metersphere.system.controller;

import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseCustomFieldTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.param.TemplateUpdateRequestDefinition;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateSystemCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.*;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationTemplateControllerTests extends BaseTest {
    private static final String BASE_PATH = "/organization/template/";
    private static final String LIST = "list/{0}/{1}";
    private static final String DISABLE_ORG_TEMPLATE = "disable/{0}/{1}";
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
    private BaseOrganizationParameterService organizationParameterService;
    @Resource
    private OrganizationTemplateService organizationTemplateService;
    @Resource
    private BaseCustomFieldTestService baseCustomFieldTestService;
    @Resource
    private OrganizationTemplateLogService organizationTemplateLogService;

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
        MvcResult mvcResult = this.requestGetWithOkAndReturn(LIST, DEFAULT_ORGANIZATION_ID, TemplateScene.FUNCTIONAL.name());
        List<Template> templates = getResultDataArray(mvcResult, Template.class);
        this.defaultTemplate = templates.getFirst();
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

        requestUploadPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, UPLOAD_TEMP_IMG, file);
        requestUploadPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_ADD, UPLOAD_TEMP_IMG, file);

        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, fileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, fileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, "no id", false));
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
        String scene = TemplateScene.FUNCTIONAL.name();
        // @@请求成功
        TemplateUpdateRequest request = new TemplateUpdateRequest();
        TemplateCustomFieldRequest templateCustomFieldRequest = getTemplateCustomFieldRequest(scene);
        request.setScene(scene);
        request.setName("test");
        request.setRemark("AAA");
        request.setEnableThirdPart(true);
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        request.setCustomFields(List.of(templateCustomFieldRequest));
        request.setSystemFields(getTemplateSystemCustomFieldRequests());

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
        Assertions.assertEquals(template.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        assertTemplateCustomFields(request, template);
        assertRefTemplate(template, request);

        // 不同scene，不校验重名
        request.setScene(TemplateScene.TEST_PLAN.name());
        this.requestPostWithOkAndReturn(DEFAULT_ADD, request);

        // @@重名校验异常
        request.setScene(scene);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), TEMPLATE_EXIST);

        // @校验是否开启组织模板
        request.setName("test1");
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@校验组织是否存在
        request.setScopeId("1111");
        request.setName("test1");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // 插入另一条数据，用户更新时重名校验
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        request.setCustomFields(null);
        request.setSystemFields(null);
        String uploadFileId = doUploadTempFile(getMockMultipartFile("api-add-file_upload.JPG"));
        request.setUploadImgFileIds(List.of(uploadFileId));
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anotherTemplateField = templateMapper.selectByPrimaryKey(getResultData(anotherMvcResult, Template.class).getId());
        assertUploadFile(uploadFileId, "api-add-file_upload.JPG");
        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, uploadFileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, uploadFileId, false)).andExpect(status().isOk());
        request.setUploadImgFileIds(null);

        // @@校验日志
        checkLog(this.addTemplate.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(TemplateUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_ADD, DEFAULT_ADD, request);
    }

    /**
     * 校验上传的文件
     *
     */
    public static void assertUploadFile(String fileId, String fileName) throws Exception {
        String orgTemplateImgDir = DefaultRepositoryDir.getOrgTemplateImgDir(DEFAULT_ORGANIZATION_ID);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(orgTemplateImgDir + "/" + fileId);
        fileRequest.setFileName(fileName);
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
    }

    public static MockMultipartFile getMockMultipartFile(String fileName) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
    }

    public static List<TemplateSystemCustomFieldRequest> getTemplateSystemCustomFieldRequests() {
        TemplateSystemCustomFieldRequest nameField = new TemplateSystemCustomFieldRequest();
        nameField.setFieldId("name");
        nameField.setDefaultValue("name default");
        TemplateSystemCustomFieldRequest descriptionField = new TemplateSystemCustomFieldRequest();
        descriptionField.setFieldId("description");
        descriptionField.setDefaultValue("description default");
        List<TemplateSystemCustomFieldRequest> systemFields = new ArrayList<>();
        systemFields.add(nameField);
        systemFields.add(descriptionField);
        return systemFields;
    }

    /**
     * 校验变更组织模板时，有没有同步变更项目模板
     *
     * @param template
     */
    private void assertRefTemplate(Template template, TemplateUpdateRequest originRequest) {
        List<Template> refTemplates = organizationTemplateService.getByRefId(template.getId());
        refTemplates.forEach(refTemplate -> {
            Assertions.assertEquals(refTemplate.getEnableThirdPart(), template.getEnableThirdPart());
            Assertions.assertEquals(refTemplate.getScene(), template.getScene());
            Assertions.assertEquals(refTemplate.getRemark(), template.getRemark());
            Assertions.assertEquals(refTemplate.getName(), template.getName());
            Assertions.assertEquals(refTemplate.getInternal(), template.getInternal());
            Assertions.assertEquals(refTemplate.getCreateUser(), template.getCreateUser());
            Assertions.assertEquals(refTemplate.getScopeType(), TemplateScopeType.PROJECT.name());

            List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
            List<TemplateCustomField> systemFields = templateCustomFields.stream().filter(TemplateCustomField::getSystemField).toList();
            for (int i = 0; i < systemFields.size(); i++) {
                TemplateCustomField templateCustomField = systemFields.get(i);
                TemplateSystemCustomFieldRequest customFieldRequest = originRequest.getSystemFields().get(i);
                Assertions.assertEquals(templateCustomField.getFieldId(), customFieldRequest.getFieldId());
                Assertions.assertEquals(templateCustomField.getDefaultValue(), customFieldRequest.getDefaultValue());
            }
        });
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

    public static void assertTemplateCustomFields(TemplateUpdateRequest request, Template template) {
        BaseTemplateCustomFieldService baseTemplateCustomFieldService = CommonBeanFactory.getBean(BaseTemplateCustomFieldService.class);
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        Assertions.assertEquals(templateCustomFields.size(), request.getCustomFields().size() + request.getSystemFields().size());
        List<TemplateCustomField> systemFields = templateCustomFields.stream().filter(TemplateCustomField::getSystemField).toList();
        List<TemplateCustomField> customFields = templateCustomFields.stream().filter(field -> !field.getSystemField()).toList();
        for (int i = 0; i < customFields.size(); i++) {
            TemplateCustomField templateCustomField = customFields.get(i);
            TemplateCustomFieldRequest customFieldRequest = request.getCustomFields().get(i);
            Assertions.assertEquals(templateCustomField.getFieldId(), customFieldRequest.getFieldId());
            Assertions.assertEquals(templateCustomField.getTemplateId(), template.getId());
            Assertions.assertEquals(templateCustomField.getRequired(), customFieldRequest.getRequired());
            Assertions.assertEquals(templateCustomField.getApiFieldId(), customFieldRequest.getApiFieldId());
            Assertions.assertEquals(templateCustomField.getDefaultValue(), customFieldRequest.getDefaultValue());
        }
        for (int i = 0; i < systemFields.size(); i++) {
            TemplateCustomField templateCustomField = systemFields.get(i);
            TemplateSystemCustomFieldRequest customFieldRequest = request.getSystemFields().get(i);
            Assertions.assertEquals(templateCustomField.getFieldId(), customFieldRequest.getFieldId());
            Assertions.assertEquals(templateCustomField.getTemplateId(), template.getId());
            Assertions.assertEquals(templateCustomField.getDefaultValue(), customFieldRequest.getDefaultValue());
        }
    }

    private TemplateCustomFieldRequest getTemplateCustomFieldRequest(String scene) {
        List<CustomField> customFields = baseCustomFieldService.getByScopeIdAndScene(DEFAULT_ORGANIZATION_ID, scene);
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
        request.setScopeId("1111");
        request.setScene(TemplateScene.UI.name());
        request.setEnableThirdPart(true);
        request.setCustomFields(new ArrayList<>(0));
        request.setSystemFields(getTemplateSystemCustomFieldRequests());
        request.getSystemFields().getFirst().setDefaultValue("update");
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Template template = templateMapper.selectByPrimaryKey(request.getId());
        // 校验请求成功数据
        Assertions.assertEquals(template.getName(), request.getName());
        Assertions.assertEquals(template.getRemark(), request.getRemark());
        Assertions.assertEquals(template.getScopeId(), DEFAULT_ORGANIZATION_ID);
        Assertions.assertEquals(template.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(template.getInternal(), false);
        Assertions.assertEquals(template.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        Assertions.assertEquals(template.getScene(), scene);
        Assertions.assertEquals(template.getEnableThirdPart(), request.getEnableThirdPart());
        assertTemplateCustomFields(request, template);
        assertRefTemplate(template, request);

        // 带字段的更新
        TemplateCustomFieldRequest templateCustomFieldRequest = getTemplateCustomFieldRequest(scene);
        request.setCustomFields(List.of(templateCustomFieldRequest));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertTemplateCustomFields(request, template);

        // 不更新字段
        request.setCustomFields(null);
        String uploadFileId = doUploadTempFile(getMockMultipartFile("api-add-file_upload.JPG"));
        request.setUploadImgFileIds(List.of(uploadFileId));
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Assertions.assertEquals(baseTemplateCustomFieldService.getByTemplateId(template.getId()).size(), 3);
        assertUploadFile(uploadFileId, "api-add-file_upload.JPG");
        // 图片预览
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, uploadFileId, false)).andExpect(status().isOk());
        mockMvc.perform(getRequestBuilder(IMG_PREVIEW, DEFAULT_ORGANIZATION_ID, uploadFileId, false)).andExpect(status().isOk());
        request.setUploadImgFileIds(null);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

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
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    public void list() throws Exception {
        String scene = TemplateScene.FUNCTIONAL.name();
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(LIST, DEFAULT_ORGANIZATION_ID, scene)
                .andReturn();
        // 校验数据是否正确
        List<Template> resultList = getResultDataArray(mvcResult, Template.class);
        List<Template> Templates = baseTemplateService.getTemplates(DEFAULT_ORGANIZATION_ID, scene);
        List<String> userIds = Templates.stream().map(Template::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        for (int i = 0; i < resultList.size(); i++) {
            Template resultItem = resultList.get(i);
            Template template = Templates.get(i);
            template.setCreateUser(userNameMap.get(template.getCreateUser()));
            if (template.getInternal()) {
                // 校验内置用户名称是否翻译
                template.setName(baseTemplateService.translateInternalTemplate());
            }
            Assertions.assertEquals(template, resultItem);
            Assertions.assertEquals(resultItem.getScene(), scene);
        }

        // @@校验组织是否存在
        assertErrorCode(this.requestGet(LIST, "1111", scene), NOT_FOUND);

        // @@校验使用场景不合法
        assertErrorCode(this.requestGet(LIST, DEFAULT_ORGANIZATION_ID, "111"), TEMPLATE_SCENE_ILLEGAL);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_READ, LIST, DEFAULT_ORGANIZATION_ID, scene);
    }

    @Test
    @Order(4)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addTemplate.getId())
                .andReturn();
        // 校验数据是否正确
        TemplateDTO templateDTO = getResultData(mvcResult, TemplateDTO.class);
        assertGetTemplateDTO(templateDTO);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_READ, DEFAULT_GET, templateDTO.getId());
    }

    public static void assertGetTemplateDTO(TemplateDTO templateDTO) {
        Template template = CommonBeanFactory.getBean(TemplateMapper.class).selectByPrimaryKey(templateDTO.getId());
        Assertions.assertEquals(template, BeanUtils.copyBean(new Template(), templateDTO));
        List<TemplateCustomField> templateCustomFields = CommonBeanFactory.getBean(BaseTemplateCustomFieldService.class).getByTemplateId(template.getId());
        List<TemplateCustomFieldDTO> customFields = templateDTO.getCustomFields();
        List<TemplateCustomField> customTemplateFields = templateCustomFields.stream().filter(field -> !field.getSystemField()).toList();
        for (int i = 0; i < customFields.size(); i++) {
            TemplateCustomFieldDTO customFieldDTO = customFields.get(i);
            TemplateCustomField templateCustomField = customTemplateFields.get(i);
            Assertions.assertEquals(customFieldDTO.getFieldId(), templateCustomField.getFieldId());
            Assertions.assertEquals(customFieldDTO.getApiFieldId(), templateCustomField.getApiFieldId());
            Assertions.assertEquals(customFieldDTO.getRequired(), templateCustomField.getRequired());
            Assertions.assertEquals(templateCustomField.getTemplateId(), template.getId());
            Assertions.assertEquals(customFieldDTO.getFieldName(), "用例等级");
        }

        List<TemplateCustomFieldDTO> systemFields = templateDTO.getSystemFields();
        List<TemplateCustomField> systemTemplateFields = templateCustomFields.stream().filter(TemplateCustomField::getSystemField).toList();
        for (int i = 0; i < systemFields.size(); i++) {
            TemplateCustomFieldDTO customFieldDTO = systemFields.get(i);
            TemplateCustomField templateCustomField = systemTemplateFields.get(i);
            Assertions.assertEquals(customFieldDTO.getFieldId(), templateCustomField.getFieldId());
            Assertions.assertEquals(customFieldDTO.getDefaultValue(), templateCustomField.getDefaultValue());
        }
    }

    @Test
    @Order(5)
    public void disableOrganizationTemplate() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DISABLE_ORG_TEMPLATE, addTemplate.getScopeId(), addTemplate.getScene());
        String value = organizationParameterService.getValue(addTemplate.getScopeId(),
                organizationParameterService.getOrgTemplateEnableKeyByScene(addTemplate.getScene()));
        Assertions.assertEquals(value, BooleanUtils.toStringTrueFalse(false));

        // 将数据改回来
        changeOrgTemplateEnable(true);

        // 提高覆盖率
        this.requestGetWithOk(DISABLE_ORG_TEMPLATE, addTemplate.getScopeId(), addTemplate.getScene());

        // @@校验日志
        checkLog(addTemplate.getScene(), OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_ENABLE, DISABLE_ORG_TEMPLATE, addTemplate.getScopeId(), addTemplate.getScene());
    }

    @Test
    @Order(7)
    public void delete() throws Exception {

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addTemplate.getId()), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addTemplate.getId());
        Assertions.assertNull(templateMapper.selectByPrimaryKey(addTemplate.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseTemplateCustomFieldService.getByTemplateId(addTemplate.getId())));
        // 校验是否同步删除了项目模板
        Assertions.assertTrue(CollectionUtils.isEmpty(organizationTemplateService.getByRefId(addTemplate.getId())));

        // @@校验内置模板删除异常
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeTypeEqualTo(TemplateScopeType.ORGANIZATION.name())
                .andInternalEqualTo(true);
        Template internalTemplate = templateMapper.selectByExample(example).getFirst();
        assertErrorCode(this.requestGet(DEFAULT_DELETE, internalTemplate.getId()), INTERNAL_TEMPLATE_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE, DEFAULT_DELETE, addTemplate.getId());
    }

    @Test
    @Order(8)
    public void getOrganizationTemplateEnableConfig() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(ENABLE_CONFIG, DEFAULT_ORGANIZATION_ID);
        Map resultData = getResultData(mvcResult, Map.class);
        Assertions.assertEquals(resultData.size(), 2);
        Assertions.assertTrue((Boolean) resultData.get(TemplateScene.FUNCTIONAL.name()));
        changeOrgTemplateEnable(false);
        mvcResult = this.requestGetWithOkAndReturn(ENABLE_CONFIG, DEFAULT_ORGANIZATION_ID);
        Assertions.assertFalse((Boolean) getResultData(mvcResult, Map.class).get(TemplateScene.FUNCTIONAL.name()));
        changeOrgTemplateEnable(true);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(ENABLE_CONFIG,"1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_READ, ENABLE_CONFIG, DEFAULT_ORGANIZATION_ID);
    }

    /**
     * 测试自定义字段解析器
     */
    @Test
    @Order(9)
    public void testCustomFiledResolver() {
        baseCustomFieldTestService.addOrgTestCustomFields();
        baseCustomFieldTestService.testResolverEmptyValidate();
        baseCustomFieldTestService.testResolverErrorValidate();
        baseCustomFieldTestService.testResolverParse();
    }

    @Test
    @Order(10)
    public void testLog() {
        Assertions.assertEquals(organizationTemplateLogService.getOperationLogModule(TemplateScene.API.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE);
        Assertions.assertEquals(organizationTemplateLogService.getOperationLogModule(TemplateScene.FUNCTIONAL.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE);
        Assertions.assertEquals(organizationTemplateLogService.getOperationLogModule(TemplateScene.BUG.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE);
        Assertions.assertEquals(organizationTemplateLogService.getOperationLogModule(TemplateScene.TEST_PLAN.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_TEMPLATE);
        Assertions.assertEquals(organizationTemplateLogService.getOperationLogModule(TemplateScene.UI.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_TEMPLATE);

        Assertions.assertEquals(organizationTemplateLogService.getDisableOrganizationTemplateModule(TemplateScene.API.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API);
        Assertions.assertEquals(organizationTemplateLogService.getDisableOrganizationTemplateModule(TemplateScene.FUNCTIONAL.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL);
        Assertions.assertEquals(organizationTemplateLogService.getDisableOrganizationTemplateModule(TemplateScene.BUG.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG);
        Assertions.assertEquals(organizationTemplateLogService.getDisableOrganizationTemplateModule(TemplateScene.TEST_PLAN.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN);
        Assertions.assertEquals(organizationTemplateLogService.getDisableOrganizationTemplateModule(TemplateScene.UI.name()), OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI);
    }
}