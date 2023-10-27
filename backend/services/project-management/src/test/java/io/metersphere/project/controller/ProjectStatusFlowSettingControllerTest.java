package io.metersphere.project.controller;

import io.metersphere.sdk.constants.*;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.dto.sdk.request.StatusItemUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.OrganizationStatusFlowSettingControllerTest;
import io.metersphere.system.controller.param.StatusDefinitionUpdateRequestDefinition;
import io.metersphere.system.controller.param.StatusFlowUpdateRequestDefinition;
import io.metersphere.system.controller.param.StatusItemAddRequestDefinition;
import io.metersphere.system.controller.param.StatusItemUpdateRequestDefinition;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OrganizationParameterMapper;
import io.metersphere.system.mapper.StatusItemMapper;
import io.metersphere.system.service.BaseStatusDefinitionService;
import io.metersphere.system.service.BaseStatusFlowService;
import io.metersphere.system.service.BaseStatusItemService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;
import static io.metersphere.system.controller.handler.result.CommonResultCode.STATUS_ITEM_EXIST;
import static io.metersphere.system.controller.handler.result.CommonResultCode.STATUS_ITEM_NOT_EXIST;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectStatusFlowSettingControllerTest extends BaseTest {

    private static final String BASE_PATH = "/project/status/flow/setting/";
    private static final String GET = "get/{0}/{1}";
    private static final String STATUS_DEFINITION_UPDATE = "status/definition/update";
    private static final String STATUS_ADD = "status/add";
    private static final String STATUS_UPDATE = "status/update";
    private static final String STATUS_SORT = "/status/sort/{0}/{1}";
    private static final String STATUS_DELETE = "status/delete/{0}";
    private static final String STATUS_FLOW_UPDATE = "status/flow/update";

    private static StatusItem addStatusItem;

    @Resource
    private StatusItemMapper statusItemMapper;
    @Resource
    private OrganizationParameterMapper organizationParameterMapper;
    @Resource
    private BaseStatusItemService baseStatusItemService;
    @Resource
    private BaseStatusFlowService baseStatusFlowService;
    @Resource
    private BaseStatusDefinitionService baseStatusDefinitionService;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void getStatusFlowSetting() throws Exception {
        // @@校验没有数据的情况
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET, DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
        List<StatusItemDTO> statusItemDTOS = getResultDataArray(mvcResult, StatusItemDTO.class);
        OrganizationStatusFlowSettingControllerTest.assertDefaultStatusFlowSettingInit(statusItemDTOS);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_READ, GET, DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
    }


    @Test
    @Order(1)
    public void addStatusItem() throws Exception {
        // 开启项目模板
        changeOrgTemplateEnable(false);
        StatusItemAddRequest request = new StatusItemAddRequest();
        request.setName("test");
        request.setRemark("test desc");
        request.setScene(TemplateScene.BUG.name());
        request.setScopeId(DEFAULT_PROJECT_ID);

        // @@校验请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(STATUS_ADD, request);
        String statusItemId = getResultData(mvcResult, StatusItem.class).getId();
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(statusItemId);
        StatusItem requestStatusItem = BeanUtils.copyBean(new StatusItem(), request);
        requestStatusItem.setId(statusItem.getId());
        requestStatusItem.setScopeType(statusItem.getScopeType());
        requestStatusItem.setInternal(statusItem.getInternal());
        requestStatusItem.setPos(baseStatusItemService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, TemplateScene.BUG.name()).size());
        Assertions.assertEquals(statusItem.getScopeType(), TemplateScopeType.PROJECT.name());
        Assertions.assertEquals(statusItem.getInternal(), false);
        Assertions.assertEquals(requestStatusItem, statusItem);
        this.addStatusItem = statusItem;

        // @@重名校验异常
        assertErrorCode(this.requestPost(STATUS_ADD, request), STATUS_ITEM_EXIST);

        // @校验是否开启组织模板
        request.setName("test2");
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(STATUS_ADD, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        request.setName("test3");
        request.setAllTransferTo(true);
        mvcResult = this.requestPostWithOkAndReturn(STATUS_ADD, request);
        statusItemId = getResultData(mvcResult, StatusItem.class).getId();
        OrganizationStatusFlowSettingControllerTest.assertAllTransferTo(statusItemId, request.getScopeId());

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_ADD, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusItemAddRequestDefinition.class, STATUS_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_ADD, request);
    }

    @Test
    @Order(2)
    public void updateStatusItem() throws Exception {
        StatusItemUpdateRequest request = new StatusItemUpdateRequest();
        request.setId(addStatusItem.getId());
        request.setName("test1");
        request.setRemark("test1 desc");

        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_UPDATE, request);
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(request.getId());
        StatusItem requestStatusItem = BeanUtils.copyBean(new StatusItem(), request);
        requestStatusItem.setId(statusItem.getId());
        Assertions.assertEquals(statusItem.getScopeType(), TemplateScopeType.PROJECT.name());
        Assertions.assertEquals(statusItem.getInternal(), false);
        Assertions.assertEquals(statusItem.getName(), request.getName());
        Assertions.assertEquals(statusItem.getRemark(), request.getRemark());

        // @校验是否开启组织模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@重名校验异常
        request.setName(DefaultBugStatusItemName.NEW);
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), STATUS_ITEM_EXIST);

        // @@校验资源不存在
        request.setId("1111");
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusItemUpdateRequestDefinition.class, STATUS_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_UPDATE, request);
    }


    @Test
    @Order(3)
    public void updateStatusDefinition() throws Exception {
        StatusDefinitionUpdateRequest request = new StatusDefinitionUpdateRequest();
        request.setScene(TemplateScene.BUG.name());
        request.setScopeId(DEFAULT_PROJECT_ID);
        StatusDefinitionUpdateRequest.StatusDefinitionRequest statusDefinition = new StatusDefinitionUpdateRequest.StatusDefinitionRequest();
        statusDefinition.setStatusId(addStatusItem.getId());
        statusDefinition.setDefinitionId(BugStatusDefinitionType.END.name());
        request.setStatusDefinitions(List.of(statusDefinition));

        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_DEFINITION_UPDATE, request);
        assertUpdateStatusDefinition(DEFAULT_PROJECT_ID);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@状态不存在
        request.getStatusDefinitions().get(0).setStatusId("1111");
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusDefinitionUpdateRequestDefinition.class, STATUS_DEFINITION_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_DEFINITION_UPDATE, request);
    }

    private void assertUpdateStatusDefinition(String scopeId) {
        List<String> statusIds = baseStatusItemService.getByScopeIdAndScene(scopeId, TemplateScene.BUG.name()).stream().map(StatusItem::getId).toList();
        List<StatusDefinition> statusDefinitions = baseStatusDefinitionService.getStatusDefinitions(statusIds);
        Assertions.assertEquals(statusDefinitions.size(), 1);
        Assertions.assertEquals(statusDefinitions.get(0).getDefinitionId(), BugStatusDefinitionType.END.name());
    }

    @Test
    @Order(4)
    public void updateStatusFlow() throws Exception {
        StatusFlowUpdateRequest request = new StatusFlowUpdateRequest();
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setScene(TemplateScene.BUG.name());

        StatusFlowUpdateRequest.StatusFlowRequest statusFlow = new StatusFlowUpdateRequest.StatusFlowRequest();
        statusFlow.setFromId(addStatusItem.getId());
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_PROJECT_ID).andSceneEqualTo(TemplateScene.BUG.name());
        StatusItem targetStatusItem = statusItemMapper.selectByExample(example).stream()
                .filter(item -> !StringUtils.equals(addStatusItem.getId(), item.getId()))
                .findFirst()
                .get();
        statusFlow.setToId(targetStatusItem.getId());
        request.setStatusFlows(List.of(statusFlow));

        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_FLOW_UPDATE, request);
        assertUpdateStatusFlow(DEFAULT_PROJECT_ID, statusFlow);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@状态不存在
        request.getStatusFlows().get(0).setToId("1111");
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusFlowUpdateRequestDefinition.class, STATUS_FLOW_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_FLOW_UPDATE, request);
    }

    private void assertUpdateStatusFlow(String scopeId, StatusFlowUpdateRequest.StatusFlowRequest statusFlow) {
        List<String> statusIds = baseStatusItemService.getByScopeIdAndScene(scopeId, TemplateScene.BUG.name()).stream()
                .map(StatusItem::getId).toList();
        List<StatusFlow> statusFlows = baseStatusFlowService.getStatusFlows(statusIds);
        Assertions.assertEquals(statusFlows.size(), 1);
        Assertions.assertEquals(statusFlows.get(0).getFromId(), statusFlow.getFromId());
        Assertions.assertEquals(statusFlows.get(0).getToId(), statusFlow.getToId());
    }

    @Test
    @Order(5)
    public void deleteStatusItem() throws Exception {
        // @校验是否开启组织模板
        changeOrgTemplateEnable(true);
        assertErrorCode(this.requestGet(STATUS_DELETE, addStatusItem.getId()), PROJECT_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(false);

        // @@请求成功
        this.requestGetWithOk(STATUS_DELETE, addStatusItem.getId());
        Assertions.assertNull(statusItemMapper.selectByPrimaryKey(addStatusItem.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusItemService.getStatusItemIdByRefId(addStatusItem.getId())));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusDefinitionService.getStatusDefinitions(List.of(addStatusItem.getId()))));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusFlowService.getStatusFlows(List.of(addStatusItem.getId()))));

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(STATUS_DELETE, "1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_DELETE, addStatusItem.getId());

        // @@校验日志
        checkLog(DEFAULT_PROJECT_ID, OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_TEMPLATE_UPDATE, STATUS_DELETE, addStatusItem.getId());
    }

    @Order(6)
    public void sortStatusItem() throws Exception {
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
        List<String> statusIds = statusItems.stream().map(StatusItem::getId).collect(Collectors.toList());
        Collections.reverse(statusIds);
        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_SORT, statusIds, DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
        OrganizationStatusFlowSettingControllerTest.assertSortStatusItem(DEFAULT_PROJECT_ID, statusIds);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_SORT, statusIds, DEFAULT_PROJECT_ID, TemplateScene.BUG.name()), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@状态不存在
        assertErrorCode(this.requestPost(STATUS_SORT, List.of("1111"), DEFAULT_PROJECT_ID, TemplateScene.BUG.name()), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        assertErrorCode(this.requestPost(STATUS_SORT, statusIds, "111", TemplateScene.BUG.name()), NOT_FOUND);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_SORT, List.of(), DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
    }

    private void changeOrgTemplateEnable(boolean enable) {
        if (enable) {
            organizationParameterMapper.deleteByPrimaryKey(DEFAULT_ORGANIZATION_ID, OrganizationParameterConstants.ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
        } else {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            organizationParameter.setParamKey(OrganizationParameterConstants.ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            if (organizationParameterMapper.selectByPrimaryKey(DEFAULT_ORGANIZATION_ID,
                    OrganizationParameterConstants.ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY) == null) {
                organizationParameterMapper.insert(organizationParameter);
            }
        }
    }
}