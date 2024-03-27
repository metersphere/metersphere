package io.metersphere.system.service;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.sdk.constants.BugStatusDefinitionType;
import io.metersphere.sdk.constants.DefaultBugStatusItem;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.controller.handler.result.CommonResultCode;
import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusDefinitionExample;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.mapper.StatusDefinitionMapper;
import io.metersphere.system.mapper.StatusItemMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseStatusFlowSettingService {

    @Resource
    protected StatusItemMapper statusItemMapper;
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
     *
     * @param scopeId
     * @param scene
     * @return
     */
    public List<StatusItemDTO> getStatusFlowSetting(String scopeId, String scene) {
        List<StatusItem> statusItems = baseStatusItemService.getStatusItems(scopeId, scene);
        statusItems = baseStatusItemService.translateInternalStatusItem(statusItems);
        List<String> statusIds = statusItems.stream().map(StatusItem::getId).toList();

        // 获取状态定义
        Map<String, List<StatusDefinition>> statusDefinitionMap = baseStatusDefinitionService.getStatusDefinitions(statusIds)
                .stream()
                .collect(Collectors.groupingBy(StatusDefinition::getStatusId));

        // 获取状态流
        Map<String, List<StatusFlow>> statusFlowMap = baseStatusFlowService.getStatusFlows(statusIds).stream()
                .collect(Collectors.groupingBy(StatusFlow::getFromId));

        return statusItems.stream().map(statusItem -> {
                    StatusItemDTO statusItemDTO = BeanUtils.copyBean(new StatusItemDTO(), statusItem);
                    List<StatusDefinition> statusDefinitions = statusDefinitionMap.get(statusItem.getId());
                    List<String> statusDefinitionIds = statusDefinitions == null ? new ArrayList<>() : statusDefinitions
                            .stream()
                            .map(StatusDefinition::getDefinitionId)
                            .collect(Collectors.toList());

                    List<StatusFlow> statusFlows = statusFlowMap.get(statusItem.getId());
                    List<String> statusFlowTargets = statusFlows == null ? new ArrayList<>() : statusFlows
                            .stream()
                            .map(StatusFlow::getToId)
                            .collect(Collectors.toList());

                    statusItemDTO.setStatusFlowTargets(statusFlowTargets);
                    statusItemDTO.setStatusDefinitions(statusDefinitionIds);
                    return statusItemDTO;
                }).sorted(Comparator.comparing(StatusItemDTO::getPos))
                .collect(Collectors.toList());

    }

    /**
     * 设置状态定义
     * 比如设置成项目
     *
     * @param request
     */
    protected void updateStatusDefinition(StatusItem statusItem, StatusDefinitionUpdateRequest request) {
        handleSingleChoice(statusItem, request);
        if (request.getEnable()) {
            baseStatusDefinitionService.add(BeanUtils.copyBean(new StatusDefinition(), request));
        } else {
            baseStatusDefinitionService.delete(request.getStatusId(), request.getDefinitionId());
        }
    }

    /**
     * 处理单选的状态定义
     * @param statusItem
     * @param request
     */
    private void handleSingleChoice(StatusItem statusItem, StatusDefinitionUpdateRequest request) {
        if (StringUtils.equals(statusItem.getScene(), TemplateScene.BUG.name())) {
            BugStatusDefinitionType statusDefinitionType = BugStatusDefinitionType.getStatusDefinitionType(request.getDefinitionId());
            if (!statusDefinitionType.getIsSingleChoice()) {
                return;
            }
            // 如果是单选，需要将其他状态取消勾选
            if (request.getEnable()) {
                List<String> statusIds = baseStatusItemService.getByScopeIdAndScene(statusItem.getScopeId(), statusItem.getScene())
                        .stream()
                        .map(StatusItem::getId)
                        .toList();
                StatusDefinitionExample example = new StatusDefinitionExample();
                example.createCriteria()
                        .andStatusIdIn(statusIds)
                        .andDefinitionIdEqualTo(request.getDefinitionId());
                statusDefinitionMapper.deleteByExample(example);
            } else {
                // 单选默认必选，不能取消
                throw new MSException(CommonResultCode.STATUS_DEFINITION_REQUIRED_ERROR);
            }
        }
    }

    /**
     * 更新状态流配置
     *
     * @param request
     */
    protected void updateStatusFlow(StatusFlowUpdateRequest request) {
        if (request.getEnable()) {
            baseStatusFlowService.add(BeanUtils.copyBean(new StatusFlow(), request));
        } else {
            baseStatusFlowService.delete(request.getFromId(), request.getToId());
        }
    }

    protected void deleteStatusItem(String id) {
        baseStatusItemService.delete(id);
        baseStatusDefinitionService.deleteByStatusId(id);
        baseStatusFlowService.deleteByStatusId(id);
    }

    /**
     * 初始化组织或者项目的默认状态流配置
     *
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
                statusFlow.setId(IDGenerator.nextStr());
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
     *
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

    /**
     * 当勾选了所有状态都可以流转到该状态
     * 添加对应的状态流转
     * @param request
     * @param addStatusItemId
     */
    protected void handleAllTransferTo(StatusItemAddRequest request, String addStatusItemId) {
        if (BooleanUtils.isTrue(request.getAllTransferTo())) {
            List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(request.getScopeId(), request.getScene());
            List<StatusFlowUpdateRequest> statusFlowUpdateRequests = statusItems.stream()
                    // 过滤自己
                    .filter(item -> !StringUtils.equals(item.getId(), addStatusItemId))
                    .map(item -> {
                        StatusFlowUpdateRequest statusFlowRequest = new StatusFlowUpdateRequest();
                        statusFlowRequest.setFromId(item.getId());
                        statusFlowRequest.setToId(addStatusItemId);
                        statusFlowRequest.setEnable(true);
                        return statusFlowRequest;
                    }).toList();
            statusFlowUpdateRequests.forEach(this::updateStatusFlow);
        }
    }

    protected List<StatusItem> sortStatusItem(String scopeId, String scene, List<String> statusIds) {
        baseStatusItemService.checkStatusScope(scopeId, statusIds);
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(scopeId, scene);
        statusItems.sort(Comparator.comparingInt(item -> statusIds.indexOf(item.getId())));
        AtomicInteger pos = new AtomicInteger();
        statusItems = statusItems.stream().map(item -> {
            StatusItem statusItem = new StatusItem();
            statusItem.setId(item.getId());
            statusItem.setPos(pos.getAndIncrement());
            return statusItem;
        }).toList();
        for (StatusItem statusItem : statusItems) {
            baseStatusItemService.update(statusItem);
        }
        return statusItems;
    }

    /**
     * 获取所有状态选项
     * @return 状态选项集合
     */
    public List<SelectOption> getAllStatusOption(String scopeId, String scene) {
        // 获取所有状态选项值
        List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(scopeId, scene);
        statusItems = baseStatusItemService.translateInternalStatusItem(statusItems);
        return statusItems.stream().map(item -> new SelectOption(item.getName(), item.getId())).toList();
    }

    /**
     * 获取状态流转选项
     * @param scopeId 项目或组织ID
     * @param scene 场景
     * @param targetStatusId 目标状态ID
     * @return 状态选项集合
     */
    public List<SelectOption> getStatusTransitions(String scopeId, String scene, String targetStatusId) {
        StatusItem targetStatus = statusItemMapper.selectByPrimaryKey(targetStatusId);
        if (StringUtils.isBlank(targetStatusId) || targetStatus == null) {
            // 创建或该目标状态被删除时, 获取开始状态的选项值即可
            List<StatusItem> statusItems = baseStatusItemService.getByScopeIdAndScene(scopeId, scene);
            statusItems = baseStatusItemService.translateInternalStatusItem(statusItems);
            List<String> statusIds = statusItems.stream().map(StatusItem::getId).toList();
            if (CollectionUtils.isEmpty(statusIds)) {
                return List.of();
            }
            StatusDefinitionExample example = new StatusDefinitionExample();
            example.createCriteria().andStatusIdIn(statusIds).andDefinitionIdEqualTo(BugStatusDefinitionType.START.name());
            List<StatusDefinition> statusDefinitions = statusDefinitionMapper.selectByExample(example);
            List<String> startIds = statusDefinitions.stream().map(StatusDefinition::getStatusId).toList();
            return statusItems.stream().filter(item -> startIds.contains(item.getId()))
                    .map(item -> new SelectOption(item.getName(), item.getId())).toList();
        } else {
            //修改时, 获取当前状态的流转选项值即可
            List<StatusFlow> nextStatusFlows = baseStatusFlowService.getNextStatusFlows(targetStatusId);
            List<String> toIds = new ArrayList<>();
            if (CollectionUtils.isEmpty(nextStatusFlows)) {
                // 当前状态选项值没有下一步流转选项值, 返回当前状态即可
                toIds = List.of(targetStatusId);
            } else {
                toIds = ListUtils.union(nextStatusFlows.stream().map(StatusFlow::getToId).collect(Collectors.toList()), List.of(targetStatusId));
            }
            List<StatusItem> statusItems = baseStatusItemService.getToStatusItemByScopeIdAndScene(scopeId, scene, toIds);
            statusItems = baseStatusItemService.translateInternalStatusItem(statusItems);
            return statusItems.stream().map(item -> new SelectOption(item.getName(), item.getId())).toList();
        }
    }
}