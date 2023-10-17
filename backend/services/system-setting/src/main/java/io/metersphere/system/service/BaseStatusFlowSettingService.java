package io.metersphere.system.service;

import io.metersphere.sdk.constants.DefaultBugStatusItem;
import io.metersphere.sdk.constants.BugStatusDefinitionType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusDefinitionExample;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusFlowSettingDTO;
import io.metersphere.system.mapper.StatusDefinitionMapper;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseStatusFlowSettingService {

    @Resource
    protected StatusDefinitionMapper statusDefinitionMapper;
    @Resource
    protected BaseStatusDefinitionService baseStatusDefinitionService;
    @Resource
    protected BaseStatusItemService baseStatusItemService;
    @Resource
    protected BaseStatusFlowService baseStatusFlowService;

    /**
     * 查询状态流设置
     * @param scopeId
     * @param scene
     * @return
     */
    public StatusFlowSettingDTO getStatusFlowSetting(String scopeId, String scene) {
        StatusFlowSettingDTO statusFlowSetting = new StatusFlowSettingDTO();
        List<StatusItem> statusItems = baseStatusItemService.getStatusItems(scopeId, scene);
        statusItems = baseStatusItemService.translateInternalStatusItem(statusItems);
        List<String> statusIds = statusItems.stream().map(StatusItem::getId).toList();

        statusFlowSetting.setStatusDefinitionTypes(
                Arrays.stream(BugStatusDefinitionType.values())
                        .map(BugStatusDefinitionType::name).toList()
        );
        statusFlowSetting.setStatusItems(statusItems);
        statusFlowSetting.setStatusDefinitions(baseStatusDefinitionService.getStatusDefinitions(statusIds));
        statusFlowSetting.setStatusFlows(baseStatusFlowService.getStatusFlows(statusIds));
        return statusFlowSetting;
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     * @param request
     */
    public void updateStatusDefinition(StatusDefinitionUpdateRequest request) {
        List<StatusDefinitionUpdateRequest.StatusDefinitionRequest> statusDefinitionsRequests = request.getStatusDefinitions();
        List<StatusDefinition> statusDefinitions = statusDefinitionsRequests.stream()
                .map(statusFlowRequest -> BeanUtils.copyBean(new StatusDefinition(), statusFlowRequest))
                .toList();
        List<String> statusIds = getStatusIds(statusDefinitionsRequests);
        // 校验状态项是否存在
        baseStatusItemService.checkStatusScope(request.getScopeId(), statusIds);
        // 查询项目下所有的状态项
        List<String> scopeStatusItemIds = baseStatusItemService.getByScopeIdAndScene(request.getScopeId(), request.getScene())
                .stream()
                .map(StatusItem::getId)
                .toList();
        updateStatusDefinition(scopeStatusItemIds, statusDefinitions);
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     * @param scopeStatusItemIds
     * @param statusDefinitions
     */
    public void updateStatusDefinition(List<String> scopeStatusItemIds, List<StatusDefinition> statusDefinitions) {
        if (CollectionUtils.isEmpty(statusDefinitions)) {
            return;
        }
        // 先删除组织或项目下的所有定义
        StatusDefinitionExample example = new StatusDefinitionExample();
        example.createCriteria().andStatusIdIn(scopeStatusItemIds);
        statusDefinitionMapper.deleteByExample(example);
        // 再添加
        statusDefinitionMapper.batchInsert(statusDefinitions);
    }

    private List<String> getStatusIds(List<StatusDefinitionUpdateRequest.StatusDefinitionRequest> statusDefinitions) {
        List<String> statusIds = statusDefinitions.stream()
                .map(StatusDefinitionUpdateRequest.StatusDefinitionRequest::getStatusId)
                .toList();
        return statusIds;
    }

    /**
     * 更新状态流配置
     * @param request
     */
    protected void updateStatusFlow( StatusFlowUpdateRequest request) {
        List<StatusFlowUpdateRequest.StatusFlowRequest> statusFlows = request.getStatusFlows();
        List<String> statusIds = baseStatusFlowService.getStatusIds(statusFlows);
        baseStatusItemService.checkStatusScope(request.getScopeId(), statusIds);
        List<String> statusItemIds = baseStatusItemService.getByScopeIdAndScene(request.getScopeId(), request.getScene())
                .stream()
                .map(StatusItem::getId).toList();
        baseStatusFlowService.updateStatusFlow(statusItemIds, request.getStatusFlows());
    }

    protected void deleteStatusItem(String id) {
        baseStatusItemService.delete(id);
        baseStatusDefinitionService.deleteByStatusId(id);
        baseStatusFlowService.deleteByStatusId(id);
    }

    /**
     * 初始化组织或者项目的默认状态流配置
     * @param projectId
     * @param scopeType
     */
    public void initBugDefaultStatusFlowSetting(String projectId, TemplateScopeType scopeType) {
        List<StatusItem> statusItems = new ArrayList<>();
        List<StatusDefinition> statusDefinitions = new ArrayList<>();
        List<StatusFlow> statusFlows = new ArrayList<>();
        for (DefaultBugStatusItem defaultBugStatusItem : DefaultBugStatusItem.values()) {
            // 创建默认的状态项
            StatusItem statusItem = new StatusItem();
            statusItem.setName(defaultBugStatusItem.getName());
            statusItem.setScene(TemplateScene.BUG.name());
            statusItem.setInternal(true);
            statusItem.setScopeType(scopeType.name());
            statusItem.setRemark(defaultBugStatusItem.getRemark());
            statusItem.setScopeId(projectId);
            statusItems.add(statusItem);
        }
        statusItems = baseStatusItemService.batchAdd(statusItems);

        Map<String, String> statusNameMap = statusItems.stream().collect(Collectors.toMap(StatusItem::getName, StatusItem::getId));
        for (DefaultBugStatusItem defaultBugStatusItem : DefaultBugStatusItem.values()) {

            // 创建默认的状态定义
            List<BugStatusDefinitionType> definitionTypes = defaultBugStatusItem.getDefinitionTypes();
            for (BugStatusDefinitionType definitionType : definitionTypes) {
                StatusDefinition statusDefinition = new StatusDefinition();
                statusDefinition.setStatusId(statusNameMap.get(defaultBugStatusItem.getName()));
                statusDefinition.setDefinitionId(definitionType.name());
                statusDefinitions.add(statusDefinition);
            }

            // 创建默认的状态流
            String fromStatusId = statusNameMap.get(defaultBugStatusItem.getName());
            List<String> statusFlowTargets = defaultBugStatusItem.getStatusFlowTargets();
            for (String statusFlowTarget : statusFlowTargets) {
                StatusFlow statusFlow = new StatusFlow();
                statusFlow.setId(UUID.randomUUID().toString());
                statusFlow.setFromId(fromStatusId);
                statusFlow.setToId(statusNameMap.get(statusFlowTarget));
                statusFlows.add(statusFlow);
            }
        }
        baseStatusDefinitionService.batchAdd(statusDefinitions);
        baseStatusFlowService.batchAdd(statusFlows);
    }

    /**
     * 删除组织或项目下状态流
     * @param scopeId
     */
    public void deleteByScopeId(String scopeId) {
        List<String> statusItemIds = baseStatusItemService.getByScopeId(scopeId)
                .stream()
                .map(StatusItem::getId)
                .toList();
        baseStatusFlowService.deleteByStatusIds(statusItemIds);
        baseStatusDefinitionService.deleteByStatusIds(statusItemIds);
        baseStatusItemService.deleteByScopeId(scopeId);
    }
}