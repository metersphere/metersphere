package io.metersphere.project.controller;

import io.metersphere.sdk.constants.OrganizationParameterConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.TemplateCustomFieldDTO;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.dto.request.TemplateUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.param.TemplateUpdateRequestDefinition;
import io.metersphere.system.domain.*;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.BaseTemplateCustomFieldService;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.service.BaseUserService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;
import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectTemplateControllerTests extends BaseTest {
    private static final String BASE_PATH = "/project/template/";
    private static final String LIST = "list/{0}/{1}";
    private static final String SET_DEFAULT = "set-default/{0}";

    @Resource
    private TemplateMapper templateMapper;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;

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
        this.defaultTemplate = templates.get(0);
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

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);

        Template resultData = getResultData(mvcResult, Template.class);
        Template template = templateMapper.selectByPrimaryKey(resultData.getId());
        this.addTemplate = template;
        // 校验请求成功数据
        request.setId(template.getId());
        TemplateUpdateRequest copyRequest = BeanUtils.copyBean(new TemplateUpdateRequest(), request);
        copyRequest.setCustomFields(null);
        Assertions.assertEquals(copyRequest, BeanUtils.copyBean(new TemplateUpdateRequest(), template));
        Assertions.assertEquals(template.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(template.getInternal(), false);
        Assertions.assertEquals(template.getScopeType(), TemplateScopeType.PROJECT.name());
        asserTemplateCustomFields(request, template);

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
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anotherTemplateField = templateMapper.selectByPrimaryKey(getResultData(anotherMvcResult, Template.class).getId());

        // @@校验日志
        checkLog(this.addTemplate.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(TemplateUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_ADD, DEFAULT_ADD, request);
    }

    private void asserTemplateCustomFields(TemplateUpdateRequest request, Template template) {
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        Assertions.assertEquals(templateCustomFields.size(), request.getCustomFields().size());
        for (int i = 0; i < templateCustomFields.size(); i++) {
            TemplateCustomField templateCustomField = templateCustomFields.get(i);
            TemplateCustomFieldRequest customFieldRequest = request.getCustomFields().get(i);
            Assertions.assertEquals(templateCustomField.getFieldId(), customFieldRequest.getFieldId());
            Assertions.assertEquals(templateCustomField.getTemplateId(), template.getId());
            Assertions.assertEquals(templateCustomField.getRequired(), customFieldRequest.getRequired());
            Assertions.assertEquals(templateCustomField.getApiFieldId(), customFieldRequest.getApiFieldId());
            Assertions.assertEquals(templateCustomField.getDefaultValue(), customFieldRequest.getDefaultValue());
        }
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
        asserTemplateCustomFields(request, template);

        // 带字段的更新
        TemplateCustomFieldRequest templateCustomFieldRequest = getTemplateCustomFieldRequest(scene);
        request.setCustomFields(List.of(templateCustomFieldRequest));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        asserTemplateCustomFields(request, template);

        // 不更新字段
        request.setCustomFields(null);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Assertions.assertEquals(baseTemplateCustomFieldService.getByTemplateId(template.getId()).size(), 1);

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
        List<Template> resultList = getResultDataArray(mvcResult, Template.class);
        List<Template> templates = baseTemplateService.getTemplates(DEFAULT_PROJECT_ID, scene);
        List<String> userIds = templates.stream().map(Template::getCreateUser).toList();
        Map<String, String> userNameMap = baseUserService.getUserNameMap(userIds);
        for (int i = 0; i < resultList.size(); i++) {
            Template resultItem = resultList.get(i);
            Template template = templates.get(i);
            template.setCreateUser(userNameMap.get(template.getCreateUser()));
            if (template.getInternal()) {
                // 校验内置用户名称是否翻译
                template.setName(baseTemplateService.translateInternalTemplate(template.getName()));
            }
            Assertions.assertEquals(template, resultItem);
            Assertions.assertEquals(resultItem.getScene(), scene);
        }

        // @@校验组织是否存在
        assertErrorCode(this.requestGet(LIST, "1111", scene), NOT_FOUND);

        // @@校验使用场景不合法
        assertErrorCode(this.requestGet(LIST, DEFAULT_PROJECT_ID, "111"), TEMPLATE_SCENE_ILLEGAL);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, LIST, DEFAULT_PROJECT_ID, scene);
    }

    @Test
    @Order(4)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addTemplate.getId())
                .andReturn();
        // 校验数据是否正确
        TemplateDTO templateDTO = getResultData(mvcResult, TemplateDTO.class);
        Template template = templateMapper.selectByPrimaryKey(templateDTO.getId());
        Assertions.assertEquals(template, BeanUtils.copyBean(new Template(), templateDTO));
        List<TemplateCustomFieldDTO> customFields = templateDTO.getCustomFields();
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        for (int i = 0; i < customFields.size(); i++) {
            TemplateCustomFieldDTO customFieldDTO = customFields.get(i);
            TemplateCustomField templateCustomField = templateCustomFields.get(i);
            Assertions.assertEquals(customFieldDTO.getFieldId(), templateCustomField.getFieldId());
            Assertions.assertEquals(customFieldDTO.getApiFieldId(), templateCustomField.getApiFieldId());
            Assertions.assertEquals(customFieldDTO.getRequired(), templateCustomField.getRequired());
            Assertions.assertEquals(templateCustomField.getTemplateId(), template.getId());
            Assertions.assertEquals(customFieldDTO.getFieldName(), "优先级");
        }

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, DEFAULT_GET, templateDTO.getId());
    }

    @Test
    @Order(5)
    public void setDefaultTemplate() throws Exception {
        changeOrgTemplateEnable(false);
        // @@请求成功
        this.requestGetWithOk(SET_DEFAULT, addTemplate.getId());
        Template template = templateMapper.selectByPrimaryKey(addTemplate.getId());
        assertSetDefaultTemplate(template);

        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestGet(SET_DEFAULT, addTemplate.getId()), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, SET_DEFAULT, addTemplate.getScopeId(), addTemplate.getScene());
    }

    @Test
    @Order(6)
    public void delete() throws Exception {
        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addTemplate.getId()), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@校验删除默认模板
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addTemplate.getId()), DEFAULT_TEMPLATE_PERMISSION);
        // 设置回来，保证正常删除
        this.requestGetWithOk(SET_DEFAULT, defaultTemplate.getId());
        
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addTemplate.getId());
        Assertions.assertNull(templateMapper.selectByPrimaryKey(addTemplate.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseTemplateCustomFieldService.getByTemplateId(addTemplate.getId())));

        // @@校验内置模板删除异常
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeTypeEqualTo(TemplateScopeType.PROJECT.name())
                .andInternalEqualTo(true);
        Template internalTemplate = templateMapper.selectByExample(example).get(0);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, internalTemplate.getId()), INTERNAL_TEMPLATE_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addTemplate.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_DELETE, DEFAULT_DELETE, addTemplate.getId());
    }

    private void assertSetDefaultTemplate(Template template) {
        Assertions.assertTrue(template.getEnableDefault());
        int defaultCount = getTemplateByScopeId(addTemplate.getScopeId())
                .stream()
                .filter(Template::getEnableDefault)
                .toList().size();
        Assertions.assertEquals(defaultCount, 1);
    }

    private List<Template> getTemplateByScopeId(String scopeId) {
        TemplateExample example = new TemplateExample();
        example.createCriteria().andScopeIdEqualTo(scopeId);
        return templateMapper.selectByExample(example);
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
}