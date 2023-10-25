package io.metersphere.system.controller;

import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.dto.request.StatusItemAddRequest;
import io.metersphere.sdk.dto.request.StatusItemUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class OrganizationStatusFlowSettingControllerTest extends BaseTest {

    private static final String BASE_PATH = "/organization/status/flow/setting/";
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
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET, DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
        List<StatusItemDTO> statusItemDTOS = getResultDataArray(mvcResult, StatusItemDTO.class);

        assertDefaultStatusFlowSettingInit(statusItemDTOS);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_READ, GET, DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
    }

    public static void assertDefaultStatusFlowSettingInit( List<StatusItemDTO> statusItemDTOS) {
        Map<String, String> nameIdMap = statusItemDTOS.stream()
                .collect(Collectors.toMap(StatusItem::getId, StatusItem::getName));
        // 校验默认的状态定义是否初始化正确
        Assertions.assertEquals(statusItemDTOS.size(), DefaultBugStatusItem.values().length);
        for (DefaultBugStatusItem defaultBugStatusItem : DefaultBugStatusItem.values()) {
            StatusItemDTO statusItemDTO = statusItemDTOS.stream()
                    .filter(item -> item.getName().equals(Translator.get("status_item." + defaultBugStatusItem.getName())))
                    .findFirst()
                    .get();

            // 校验默认的状态定义是否初始化正确
            List<String> defaultDefinitionTypes = defaultBugStatusItem.getDefinitionTypes()
                    .stream()
                    .map(BugStatusDefinitionType::name)
                    .toList();
            List<String> definitionTypes = statusItemDTO.getStatusDefinitions();
            Assertions.assertEquals(defaultDefinitionTypes, definitionTypes);

            // 校验默认的状态流是否初始化正确
            List<String> defaultFlowTargets = defaultBugStatusItem.getStatusFlowTargets();
            List<String> flowTargets = statusItemDTO.getStatusFlowTargets()
                    .stream()
                    .map(nameIdMap::get)
                    .toList();
            Assertions.assertEquals(defaultFlowTargets.stream().map(i -> BaseStatusItemService.translateInternalStatusItem(i)).toList(),
                    flowTargets);
        }
    }

    @Test
    @Order(1)
    public void addStatusItem() throws Exception {
        StatusItemAddRequest request = new StatusItemAddRequest();
        request.setName("test");
        request.setRemark("test desc");
        request.setScene(TemplateScene.BUG.name());
        request.setScopeId(DEFAULT_ORGANIZATION_ID);

        // @@校验请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(STATUS_ADD, request);
        String statusItemId = getResultData(mvcResult, StatusItem.class).getId();
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(statusItemId);
        StatusItem requestStatusItem = BeanUtils.copyBean(new StatusItem(), request);
        requestStatusItem.setId(statusItem.getId());
        requestStatusItem.setScopeType(statusItem.getScopeType());
        requestStatusItem.setInternal(statusItem.getInternal());
        requestStatusItem.setPos(baseStatusItemService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, TemplateScene.BUG.name()).size());
        Assertions.assertEquals(statusItem.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        Assertions.assertEquals(statusItem.getInternal(), false);
        Assertions.assertEquals(requestStatusItem, statusItem);
        assertRefStatusItem(statusItem);
        this.addStatusItem = statusItem;

        // @@重名校验异常
        assertErrorCode(this.requestPost(STATUS_ADD, request), STATUS_ITEM_EXIST);

        // @校验是否开启组织模板
        request.setName("test2");
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_ADD, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @校验 allTransferTo
        request.setName("test2");
        request.setAllTransferTo(true);
        mvcResult = this.requestPostWithOkAndReturn(STATUS_ADD, request);
        statusItemId = getResultData(mvcResult, StatusItem.class).getId();
        assertAllTransferTo(statusItemId, request.getScopeId());
        assertRefAllTransferTo(statusItemId);

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_ADD, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_ORGANIZATION_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusItemAddRequestDefinition.class, STATUS_ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_ADD, request);
    }

    /**
     * 校验变更组织状态项时，有没有同步变更项目状态项
     *
     * @param orgStatusItemId
     */
    private void assertRefAllTransferTo(String orgStatusItemId) {
        List<StatusItem> refStatusItems = baseStatusItemService.getByRefId(orgStatusItemId);
        refStatusItems.forEach(refStatusItem -> {
            assertAllTransferTo(refStatusItem.getId(), refStatusItem.getScopeId());
        });
    }

    /**
     * 校验 allTransferTo
     * @param statusItemId
     */
    public static void assertAllTransferTo(String statusItemId, String scopeId) {
        BaseStatusFlowService baseStatusFlowService = CommonBeanFactory.getBean(BaseStatusFlowService.class);
        BaseStatusItemService baseStatusItemService = CommonBeanFactory.getBean(BaseStatusItemService.class);
        List<StatusFlow> statusFlows = baseStatusFlowService.getStatusFlows(List.of(statusItemId));
        Map<String, List<StatusFlow>> formMap = statusFlows.stream().collect(Collectors.groupingBy(StatusFlow::getToId));
        Assertions.assertEquals(1, formMap.size());
        Assertions.assertTrue(formMap.containsKey(statusItemId));
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(scopeId, TemplateScene.BUG.name());
        Assertions.assertEquals(statusFlows.size() + 1, statusItems.size());
        for (StatusItem item : statusItems) {
            if (!StringUtils.equals(item.getId(), statusItemId)) {
                Assertions.assertEquals(statusFlows.stream().filter(i -> StringUtils.equals(i.getFromId(), item.getId())).count(), 1);
            }
        }
    }

    /**
     * 校验变更组织状态项时，有没有同步变更项目状态项
     *
     * @param orgStatusItem
     */
    private void assertRefStatusItem(StatusItem orgStatusItem) {
        List<StatusItem> refStatusItems = baseStatusItemService.getByRefId(orgStatusItem.getId());
        refStatusItems.forEach(refStatusItem -> {
            Assertions.assertEquals(refStatusItem.getRefId(), orgStatusItem.getId());
            Assertions.assertEquals(refStatusItem.getScene(), orgStatusItem.getScene());
            Assertions.assertEquals(refStatusItem.getRemark(), orgStatusItem.getRemark());
            Assertions.assertEquals(refStatusItem.getName(), orgStatusItem.getName());
            Assertions.assertEquals(refStatusItem.getInternal(), orgStatusItem.getInternal());
            Assertions.assertEquals(refStatusItem.getScopeType(), TemplateScopeType.PROJECT.name());
        });
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
        Assertions.assertEquals(statusItem.getScopeType(), TemplateScopeType.ORGANIZATION.name());
        Assertions.assertEquals(statusItem.getInternal(), false);
        Assertions.assertEquals(statusItem.getName(), request.getName());
        Assertions.assertEquals(statusItem.getRemark(), request.getRemark());
        assertRefStatusItem(statusItem);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@重名校验异常
        request.setName(DefaultBugStatusItemName.NEW);
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), STATUS_ITEM_EXIST);

        // @@校验资源不存在
        request.setId("1111");
        assertErrorCode(this.requestPost(STATUS_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_ORGANIZATION_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusItemUpdateRequestDefinition.class, STATUS_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_UPDATE, request);
    }


    @Test
    @Order(3)
    public void updateStatusDefinition() throws Exception {
        StatusDefinitionUpdateRequest request = new StatusDefinitionUpdateRequest();
        request.setScene(TemplateScene.BUG.name());
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        StatusDefinitionUpdateRequest.StatusDefinitionRequest statusDefinition = new StatusDefinitionUpdateRequest.StatusDefinitionRequest();
        statusDefinition.setStatusId(addStatusItem.getId());
        statusDefinition.setDefinitionId(BugStatusDefinitionType.END.name());
        request.setStatusDefinitions(List.of(statusDefinition));

        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_DEFINITION_UPDATE, request);
        assertUpdateStatusDefinition(DEFAULT_ORGANIZATION_ID);
        // 校验项目是否同步修改
        assertUpdateStatusDefinition(DEFAULT_PROJECT_ID);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@状态不存在
        request.getStatusDefinitions().get(0).setStatusId("1111");
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_DEFINITION_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_ORGANIZATION_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusDefinitionUpdateRequestDefinition.class, STATUS_DEFINITION_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_DEFINITION_UPDATE, request);
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
        request.setScopeId(DEFAULT_ORGANIZATION_ID);
        request.setScene(TemplateScene.BUG.name());

        StatusFlowUpdateRequest.StatusFlowRequest statusFlow = new StatusFlowUpdateRequest.StatusFlowRequest();
        statusFlow.setFromId(addStatusItem.getId());
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdEqualTo(DEFAULT_ORGANIZATION_ID).andSceneEqualTo(TemplateScene.BUG.name());
        StatusItem targetStatusItem = statusItemMapper.selectByExample(example).stream()
                .filter(item -> !StringUtils.equals(addStatusItem.getId(), item.getId()))
                .findFirst()
                .get();
        statusFlow.setToId(targetStatusItem.getId());
        request.setStatusFlows(List.of(statusFlow));

        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_FLOW_UPDATE, request);
        assertUpdateStatusFlow(DEFAULT_ORGANIZATION_ID, statusFlow);
        // 校验项目是否同步修改
        StatusFlowUpdateRequest.StatusFlowRequest projectStatusFlow = new StatusFlowUpdateRequest.StatusFlowRequest();
        projectStatusFlow.setFromId(baseStatusItemService.getByRefId(statusFlow.getFromId()).get(0).getId());
        projectStatusFlow.setToId(baseStatusItemService.getByRefId(statusFlow.getToId()).get(0).getId());
        assertUpdateStatusFlow(DEFAULT_PROJECT_ID, projectStatusFlow);

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@状态不存在
        request.getStatusFlows().get(0).setToId("1111");
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        request.setScopeId("1111");
        assertErrorCode(this.requestPost(STATUS_FLOW_UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(DEFAULT_ORGANIZATION_ID, OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(StatusFlowUpdateRequestDefinition.class, STATUS_FLOW_UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_FLOW_UPDATE, request);
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
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestGet(STATUS_DELETE, addStatusItem.getId()), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@请求成功
        this.requestGetWithOk(STATUS_DELETE, addStatusItem.getId());
        Assertions.assertNull(statusItemMapper.selectByPrimaryKey(addStatusItem.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusItemService.getStatusItemIdByRefId(addStatusItem.getId())));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusDefinitionService.getStatusDefinitions(List.of(addStatusItem.getId()))));
        Assertions.assertTrue(CollectionUtils.isEmpty(baseStatusFlowService.getStatusFlows(List.of(addStatusItem.getId()))));

        // @@校验 NOT_FOUND 异常
        assertErrorCode(this.requestGet(STATUS_DELETE, "1111"), NOT_FOUND);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_DELETE, addStatusItem.getId());

        // @@校验日志
        checkLog(DEFAULT_ORGANIZATION_ID, OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_DELETE, addStatusItem.getId());
    }

    @Test
    @Order(6)
    public void sortStatusItem() throws Exception {
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
        List<String> statusIds = statusItems.stream().map(StatusItem::getId).toList().reversed();
        // @@校验请求成功
        this.requestPostWithOkAndReturn(STATUS_SORT, statusIds, DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
        assertSortStatusItem(DEFAULT_ORGANIZATION_ID, statusIds);
        // 校验同步更新项目状态
        assertSortStatusItem();

        // @校验是否开启组织模板
        changeOrgTemplateEnable(false);
        assertErrorCode(this.requestPost(STATUS_SORT, statusIds, DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name()), ORGANIZATION_TEMPLATE_PERMISSION);
        changeOrgTemplateEnable(true);

        // @@状态不存在
        assertErrorCode(this.requestPost(STATUS_SORT, List.of("1111"), DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name()), STATUS_ITEM_NOT_EXIST);

        // @@校验组织是否存在
        assertErrorCode(this.requestPost(STATUS_SORT, statusIds, "111", TemplateScene.BUG.name()), NOT_FOUND);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, STATUS_SORT, List.of(), DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
    }

    private void assertSortStatusItem() {
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(DEFAULT_ORGANIZATION_ID, TemplateScene.BUG.name());
        List<StatusItem> projectStatusItems = baseStatusItemService.getByScopeIdAndScene(DEFAULT_PROJECT_ID, TemplateScene.BUG.name());
        for (int i = 0; i < projectStatusItems.size(); i++) {
            StatusItem projectStatusItem = projectStatusItems.get(i);
            StatusItem statusItem = statusItems.stream().filter(item -> StringUtils.equals(item.getId(), projectStatusItem.getRefId()))
                    .findFirst().orElse(null);
            Assertions.assertEquals(statusItem.getPos(), projectStatusItem.getPos());
        }
    }

    public static void assertSortStatusItem(String scopeId, List<String> statusIds) {
        BaseStatusItemService baseStatusItemService = CommonBeanFactory.getBean(BaseStatusItemService.class);
        List<StatusItem> statusItems = baseStatusItemService.getByScopeId(scopeId);
        for (int i = 0; i < statusIds.size(); i++) {
            String statusId = statusIds.get(i);
            // 根据statusId 查询
            StatusItem statusItem = statusItems.stream().filter(item -> StringUtils.equals(item.getId(), statusId))
                    .findFirst().orElse(null);
            Assertions.assertEquals(statusItem.getPos(), i);
        }
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