package io.metersphere.project.controller;

import io.metersphere.project.service.ProjectCustomFieldLogService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.param.CustomFieldUpdateRequestDefinition;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.dto.sdk.request.CustomFieldUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.TemplateCustomFieldMapper;
import io.metersphere.system.service.BaseCustomFieldOptionService;
import io.metersphere.system.service.BaseCustomFieldService;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;
import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.*;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCustomFieldControllerTests extends BaseTest {
    private static final String BASE_PATH = "/project/custom/field/";

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
    private TemplateCustomFieldMapper templateCustomFieldMapper;
    @Resource
    private ProjectCustomFieldLogService projectCustomFieldLogService;
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
        this.requestGetWithOk(LIST, DEFAULT_PROJECT_ID, TemplateScene.UI.name());
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        // 开启项目模板
        changeOrgTemplateEnable(false);
        // @@请求成功
        CustomFieldUpdateRequest request = new CustomFieldUpdateRequest();
        request.setScene(TemplateScene.FUNCTIONAL.name());
        request.setName("test");
        request.setType(CustomFieldType.SELECT.name());
        request.setRemark("AAA");
        request.setScopeId(DEFAULT_PROJECT_ID);
        CustomFieldOptionRequest customFieldOptionRequest = new CustomFieldOptionRequest();
        customFieldOptionRequest.setValue("1111");
        customFieldOptionRequest.setText("test");
        customFieldOptionRequest.setPos(1);
        request.setEnableOptionKey(true);
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
        Assertions.assertEquals(customField.getScopeType(), TemplateScopeType.PROJECT.name());
        List<CustomFieldOption> options = baseCustomFieldOptionService.getByFieldId(customField.getId());
        for (int i = 0; i < options.size(); i++) {
            CustomFieldOptionRequest optionRequestItem = optionRequests.get(i);
            CustomFieldOption optionItem = options.get(i);
            Assertions.assertEquals(optionRequestItem.getText(), optionItem.getText());
            Assertions.assertEquals(optionRequestItem.getValue(), optionItem.getValue());
            Assertions.assertEquals(false, optionItem.getInternal());
            Assertions.assertEquals(customField.getId(), optionItem.getFieldId());
        }

        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), CUSTOM_FIELD_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        request.setName("test1");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // 插入另一条数据，用户更新时重名校验
        request.setType(CustomFieldType.MEMBER.name());
        request.setScopeId(DEFAULT_PROJECT_ID);
        MvcResult anotherMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anotherAddCustomField = customFieldMapper.selectByPrimaryKey(getResultData(anotherMvcResult, CustomField.class).getId());

        request.setType(CustomFieldType.MULTIPLE_MEMBER.name());
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setName("testAAA");
        this.requestPostWithOkAndReturn(DEFAULT_ADD, request);

        // @@校验日志
        checkLog(this.addCustomField.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(CustomFieldUpdateRequestDefinition.class, DEFAULT_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_ADD, DEFAULT_ADD, request);
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
        request.setEnableOptionKey(false);
        CustomFieldOptionRequest customFieldOptionRequest = new CustomFieldOptionRequest();
        customFieldOptionRequest.setValue("11112");
        customFieldOptionRequest.setText("test1");
        customFieldOptionRequest.setPos(1);
        List<CustomFieldOptionRequest> optionRequests = Arrays.asList(customFieldOptionRequest);
        request.setOptions(optionRequests);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        CustomField customField = customFieldMapper.selectByPrimaryKey(request.getId());
        // 校验请求成功数据
        request.setOptions(null);
        request.setId(customField.getId());
        request.setScopeId(DEFAULT_PROJECT_ID);
        Assertions.assertEquals(request, BeanUtils.copyBean(new CustomFieldUpdateRequest(), customField));
        Assertions.assertEquals(customField.getCreateUser(), ADMIN.getValue());
        Assertions.assertEquals(customField.getInternal(), false);
        Assertions.assertEquals(customField.getScopeType(), TemplateScopeType.PROJECT.name());
        List<CustomFieldOption> options = baseCustomFieldOptionService.getByFieldId(customField.getId());
        for (int i = 0; i < options.size(); i++) {
            CustomFieldOptionRequest optionRequestItem = optionRequests.get(i);
            CustomFieldOption optionItem = options.get(i);
            Assertions.assertEquals(optionRequestItem.getText(), optionItem.getText());
            Assertions.assertEquals(optionRequestItem.getValue(), optionItem.getValue());
            Assertions.assertEquals(false, optionItem.getInternal());
            Assertions.assertEquals(customField.getId(), optionItem.getFieldId());
        }

        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("functional_priority");
        CustomFieldUpdateRequest internalRequest = BeanUtils.copyBean(new CustomFieldUpdateRequest(), customFieldMapper.selectByExample(example).getFirst());
        internalRequest.setName("aaaa");
        this.requestPostWithOk(DEFAULT_UPDATE, internalRequest);
        Assertions.assertEquals(customFieldMapper.selectByExample(example).getFirst().getInternal(), true);
        // 内置字段名称不能修改
        Assertions.assertEquals(customFieldMapper.selectByExample(example).getFirst().getName(), "functional_priority");

        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

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
        List<CustomFieldDTO> resultList = getResultDataArray(mvcResult, CustomFieldDTO.class);
        List<CustomField> customFields = baseCustomFieldService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, scene);
        List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
        for (int i = 0; i < resultList.size(); i++) {
            CustomField resultItem = BeanUtils.copyBean(new CustomField(), resultList.get(i));
            CustomField customField = customFields.get(i);
            customField.setCreateUser(userNameMap.get(customField.getCreateUser()));
            if (customField.getInternal()) {
                // 校验内置用户名称是否翻译
                customField.setName(baseCustomFieldService.translateInternalField(customField.getName()));
            }
            Assertions.assertEquals(customField, resultItem);
            Assertions.assertEquals(resultItem.getScene(), scene);

            if (StringUtils.equalsAny(resultItem.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                List<CustomFieldOption> options = resultList.get(i).getOptions();
                Assertions.assertEquals(options.size(), 1);
                Assertions.assertEquals(options.getFirst().getValue(), "CREATE_USER");
                Assertions.assertEquals(options.getFirst().getText(), "创建人");
            } else if (CustomFieldType.getHasOptionValueSet().contains(resultItem.getType())) {
                // 有下拉框选项的校验选项
                Assertions.assertEquals(resultList.get(i).getOptions().stream().sorted(Comparator.comparing(CustomFieldOption::getValue)).toList(),
                        baseCustomFieldOptionService.getByFieldId(customField.getId()).stream().sorted(Comparator.comparing(CustomFieldOption::getValue)).toList());
            }
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
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addCustomField.getId())
                .andReturn();
        // 校验数据是否正确
        CustomFieldDTO customFieldDTO = getResultData(mvcResult, CustomFieldDTO.class);
        List<CustomFieldOption> options = customFieldDTO.getOptions();
        CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldDTO.getId());
        Assertions.assertEquals(customField, BeanUtils.copyBean(new CustomField(), customFieldDTO));
        Assertions.assertEquals(options, baseCustomFieldOptionService.getByFieldId(customField.getId()));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, DEFAULT_GET, customFieldDTO.getId());
    }

    @Test
    @Order(5)
    public void delete() throws Exception {
        // @校验是否开启项目模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestGet(DEFAULT_DELETE, addCustomField.getId()), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addCustomField.getId());
        Assertions.assertNull(customFieldMapper.selectByPrimaryKey(addCustomField.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseCustomFieldOptionService.getByFieldId(addCustomField.getId())));
        Assertions.assertTrue(CollectionUtils.isEmpty(getTemplateCustomField(addCustomField.getId())));

        // @@校验内置字段删除异常
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andInternalEqualTo(true);
        CustomField internalCustomField = customFieldMapper.selectByExample(example).getFirst();
        assertErrorCode(this.requestGet(DEFAULT_DELETE, internalCustomField.getId()), INTERNAL_CUSTOM_FIELD_PERMISSION);

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(DEFAULT_DELETE, "1111"), NOT_FOUND);

        // @@校验日志
        checkLog(addCustomField.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_DELETE, DEFAULT_DELETE, addCustomField.getId());
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

    private List<TemplateCustomField> getTemplateCustomField(String id) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria()
                .andFieldIdEqualTo(id);
        return templateCustomFieldMapper.selectByExample(example);
    }

    @Test
    @Order(10)
    public void testLog() {
        Assertions.assertEquals(projectCustomFieldLogService.getOperationLogModule(TemplateScene.API.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_FIELD);
        Assertions.assertEquals(projectCustomFieldLogService.getOperationLogModule(TemplateScene.FUNCTIONAL.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_FIELD);
        Assertions.assertEquals(projectCustomFieldLogService.getOperationLogModule(TemplateScene.BUG.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_FIELD);
        Assertions.assertEquals(projectCustomFieldLogService.getOperationLogModule(TemplateScene.TEST_PLAN.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_FIELD);
        Assertions.assertEquals(projectCustomFieldLogService.getOperationLogModule(TemplateScene.UI.name()), OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_FIELD);
    }
}