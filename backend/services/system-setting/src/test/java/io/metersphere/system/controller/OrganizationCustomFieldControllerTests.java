package io.metersphere.system.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.system.base.BaseTest;
import io.metersphere.sdk.dto.CustomFieldDTO;
import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;
import io.metersphere.sdk.dto.request.CustomFieldUpdateRequest;
import io.metersphere.system.domain.*;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.service.BaseCustomFieldOptionService;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.controller.param.CustomFieldUpdateRequestDefinition;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.service.OrganizationCustomFieldService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationCustomFieldControllerTests extends BaseTest {
    private static final String BASE_PATH = "/organization/custom/field/";

    private static final String LIST = "list/{0}/{1}";

    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private BaseCustomFieldOptionService baseCustomFieldOptionService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OrganizationCustomFieldService organizationCustomFieldService;
    private static CustomField addCustomField;
    private static CustomField anotherAddCustomField;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        // @@校验没有数据的情况
        this.requestGetWithOk(LIST, DEFAULT_ORGANIZATION_ID, TemplateScene.UI.name());
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        // @@请求成功
        CustomFieldUpdateRequest request = new CustomFieldUpdateRequest();
        request.setScene(TemplateScene.FUNCTIONAL.name());
        request.setName("test");
        request.setType(CustomFieldType.SELECT.name());
        request.setRemark("AAA");
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        CustomFieldOptionRequest customFieldOptionRequest = new CustomFieldOptionRequest();
        customFieldOptionRequest.setValue("1111");
        customFieldOptionRequest.setText("test");
        List<CustomFieldOptionRequest> optionRequests = Arrays.asList(customFieldOptionRequest);
        request.setOptions(optionRequests);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);

        CustomField resultData = getResultData(mvcResult, CustomField.class);
        CustomField customField = customFieldMapper.selectByPrimaryKey(resultData.getId());
        this.addCustomField = customField;
        // 校验请求成功数据
        request.setOptions(null);
        request.setId(customField.getId());
        Assertions.assertEquals(request, BeanUtils.copyBean(new CustomFieldUpdateRequest(), customField));
        Assertions.assertEquals(customField.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(customField.getInternal(), false);
        Assertions.assertEquals(customField.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        List<CustomFieldOption> options = baseCustomFieldOptionService.getByFieldId(customField.getId());
        for (int i = 0; i < options.size(); i++) {
            CustomFieldOptionRequest optionRequestItem = optionRequests.get(i);
            CustomFieldOption optionItem = options.get(i);
            Assertions.assertEquals(optionRequestItem.getText(), optionItem.getText());
            Assertions.assertEquals(optionRequestItem.getValue(), optionItem.getValue());
            Assertions.assertEquals(false, optionItem.getInternal());
            Assertions.assertEquals(customField.getId(), optionItem.getFieldId());
        }
        assertRefCustomField(customField);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), CUSTOM_FIELD_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        request.setName("test1");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // 插入另一条数据，用户更新时重名校验
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anotherAddCustomField = customFieldMapper.selectByPrimaryKey(getResultData(anotherMvcResult, CustomField.class).getId());

        // @@校验日志
        checkLog(this.addCustomField.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(CustomFieldUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_CUSTOM_FIELD_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        // @@请求成功
        CustomFieldUpdateRequest request = new CustomFieldUpdateRequest();
        request.setId(addCustomField.getId());
        request.setScene(TemplateScene.FUNCTIONAL.name());
        request.setName("test2");
        request.setType(CustomFieldType.SELECT.name());
        request.setRemark("AAA1");
        request.setScopeId("1111");
        CustomFieldOptionRequest customFieldOptionRequest = new CustomFieldOptionRequest();
        customFieldOptionRequest.setValue("11112");
        customFieldOptionRequest.setText("test1");
        List<CustomFieldOptionRequest> optionRequests = Arrays.asList(customFieldOptionRequest);
        request.setOptions(optionRequests);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        CustomField customField = customFieldMapper.selectByPrimaryKey(request.getId());
        // 校验请求成功数据
        request.setOptions(null);
        request.setId(customField.getId());
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        Assertions.assertEquals(request, BeanUtils.copyBean(new CustomFieldUpdateRequest(), customField));
        Assertions.assertEquals(customField.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(customField.getInternal(), false);
        Assertions.assertEquals(customField.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        List<CustomFieldOption> options = baseCustomFieldOptionService.getByFieldId(customField.getId());
        for (int i = 0; i < options.size(); i++) {
            CustomFieldOptionRequest optionRequestItem = optionRequests.get(i);
            CustomFieldOption optionItem = options.get(i);
            Assertions.assertEquals(optionRequestItem.getText(), optionItem.getText());
            Assertions.assertEquals(optionRequestItem.getValue(), optionItem.getValue());
            Assertions.assertEquals(false, optionItem.getInternal());
            Assertions.assertEquals(customField.getId(), optionItem.getFieldId());
        }
        assertRefCustomField(customField);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@重名校验异常
        request.setName(anotherAddCustomField.getName());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), CUSTOM_FIELD_EXIST);

        // @校验 NOT_FOUND 异常
        request.setId("1111");
        request.setName("1111");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(addCustomField.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        updatedGroupParamValidateTest(CustomFieldUpdateRequestDefinition.class, DEFAULT_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_CUSTOM_FIELD_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    public void list() throws Exception {
        String scene = TemplateScene.FUNCTIONAL.name();
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(LIST, DEFAULT_ORGANIZATION_ID, scene)
                .andReturn();
        // 校验数据是否正确
        List<CustomField> resultList = getResultDataArray(mvcResult, CustomField.class);
        List<CustomField> customFields = baseCustomFieldService.getByScopeIdAndScene(DEFAULT_ORGANIZATION_ID, scene);
        List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        for (int i = 0; i < resultList.size(); i++) {
            CustomField resultItem = resultList.get(i);
            CustomField customField = customFields.get(i);
            customField.setCreateUser(userNameMap.get(customField.getCreateUser()));
            if (customField.getInternal()) {
                // 校验内置用户名称是否翻译
                customField.setName(baseCustomFieldService.translateInternalField(customField.getName()));
            }
            Assertions.assertEquals(customField, resultItem);
            Assertions.assertEquals(resultItem.getScene(), scene);
        }

        // @@校验组织是否存在
        assertErrorCode(this.requestGet(LIST, "1111", scene), NOT_FOUND);

        // @@校验使用场景不合法
        assertErrorCode(this.requestGet(LIST, DEFAULT_ORGANIZATION_ID, "111"), TEMPLATE_SCENE_ILLEGAL);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_CUSTOM_FIELD_READ, LIST, DEFAULT_ORGANIZATION_ID, scene);
    }

    @Test
    @Order(4)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addCustomField.getId())
                .andReturn();
        // 校验数据是否正确
        CustomFieldDTO customFieldDTO = getResultData(mvcResult, CustomFieldDTO.class);
        List<CustomFieldOption> options = customFieldDTO.getOptions();
        CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldDTO.getId());
        Assertions.assertEquals(customField, BeanUtils.copyBean(new CustomField(), customFieldDTO));
        Assertions.assertEquals(options, baseCustomFieldOptionService.getByFieldId(customField.getId()));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_CUSTOM_FIELD_READ, DEFAULT_GET, customFieldDTO.getId());
    }

    @Test
    @Order(5)
    public void delete() throws Exception {
        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addCustomField.getId()), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addCustomField.getId());
        Assertions.assertNull(customFieldMapper.selectByPrimaryKey(addCustomField.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseCustomFieldOptionService.getByFieldId(addCustomField.getId())));

        // @@校验内置字段删除异常
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andInternalEqualTo(true);
        CustomField internalCustomField = customFieldMapper.selectByExample(example).get(0);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, internalCustomField.getId()), INTERNAL_CUSTOM_FIELD_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addCustomField.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_CUSTOM_FIELD_DELETE, DEFAULT_DELETE, addCustomField.getId());
    }

    /**
     * 校验变更组织字段时，有没有同步变更项目字段
     */
    private void assertRefCustomField(CustomField customField) {
        List<CustomField> refFields = organizationCustomFieldService.getByRefId(customField.getId());
        List<Project> orgProjects = getProjectByOrgId(customField.getScopeId());
        // 校验所有项目下是否都有同步变更
    /*    Assertions.assertEquals(getCustomFieldByScopeId(customField.getScopeId()).size(),
                getCustomFieldByScopeId(orgProjects.get(0).getId()).size() * orgProjects.size());*/
        refFields.forEach(refField -> {
            Assertions.assertEquals(refField.getScene(), customField.getScene());
            Assertions.assertEquals(refField.getRemark(), customField.getRemark());
            Assertions.assertEquals(refField.getName(), customField.getName());
            Assertions.assertEquals(refField.getInternal(), customField.getInternal());
            Assertions.assertEquals(refField.getCreateUser(), customField.getCreateUser());
            Assertions.assertEquals(refField.getType(), customField.getType());
            Assertions.assertEquals(refField.getScopeType(), TemplateScopeType.PROJECT.name());
        });
    }

    private List<Project> getProjectByOrgId(String orgId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(orgId);
        return projectMapper.selectByExample(projectExample);
    }

    private List<CustomField> getCustomFieldByScopeId(String scopeId) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(scopeId);
        return customFieldMapper.selectByExample(example);
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