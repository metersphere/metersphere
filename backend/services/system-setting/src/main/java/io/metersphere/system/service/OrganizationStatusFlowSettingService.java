package io.metersphere.system.service;

import io.metersphere.sdk.constants.BugStatusDefinitionType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusDefinitionExample;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.dto.sdk.request.StatusItemUpdateRequest;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationStatusFlowSettingService extends BaseStatusFlowSettingService {

    @Resource
    private OrganizationTemplateService organizationTemplateService;
    @Resource
    private BaseProjectMapper baseProjectMapper;

    /**
     * 查询状态流设置
     *
     * @param organizationId
     * @param scene
     * @return
     */
    @Override
    public List<StatusItemDTO> getStatusFlowSetting(String organizationId, String scene) {
        OrganizationService.checkResourceExist(organizationId);
        return super.getStatusFlowSetting(organizationId, scene);
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     *
     * @param request
     */
    public void updateStatusDefinition(StatusDefinitionUpdateRequest request) {
        StatusItem statusItem = baseStatusItemService.getWithCheck(request.getStatusId());
        OrganizationService.checkResourceExist(statusItem.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(statusItem.getScopeId(), statusItem.getScene());
        super.updateStatusDefinition(statusItem, request);
        // 同步更新项目级别状态定义
        updateRefProjectStatusDefinition(statusItem, request);
    }

    /**
     * 同步更新项目级别状态定义
     *
     * @param request
     * @return
     */
    public void updateRefProjectStatusDefinition(StatusItem orgStatusItem, StatusDefinitionUpdateRequest request) {
        handleRefSingleChoice(orgStatusItem, request);
        List<StatusItem> statusItems = baseStatusItemService.getByRefId(request.getStatusId());
        SubListUtils.dealForSubList(statusItems, 200, (subStatusItems) -> {
            // 每次处理200条
            if (request.getEnable()) {
                List<StatusDefinition> projectStatusDefinitions = subStatusItems.stream().map(statusItem -> {
                    StatusDefinition statusDefinition = new StatusDefinition();
                    statusDefinition.setStatusId(statusItem.getId());
                    statusDefinition.setDefinitionId(request.getDefinitionId());
                    return statusDefinition;
                }).toList();
                baseStatusDefinitionService.batchAdd(projectStatusDefinitions);
            } else {
                List<String> projectStatusIds = subStatusItems.stream()
                        .map(StatusItem::getId)
                        .toList();
                baseStatusDefinitionService.deleteByStatusIdsAndDefinitionId(projectStatusIds, request.getDefinitionId());
            }
        });
    }


    /**
     * 处理单选的状态定义
     *
     * @param request
     */
    private void handleRefSingleChoice(StatusItem orgStatusItem, StatusDefinitionUpdateRequest request) {
        if (StringUtils.equals(orgStatusItem.getScene(), TemplateScene.BUG.name())) {
            BugStatusDefinitionType statusDefinitionType = BugStatusDefinitionType.getStatusDefinitionType(request.getDefinitionId());
            if (!statusDefinitionType.getIsSingleChoice()) {
                return;
            }
            List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgStatusItem.getScopeId());
            SubListUtils.dealForSubList(projectIds, 200, (subProjectIds) -> {
                // 查询组织下所有项目的状态项
                List<StatusItem> statusItems = baseStatusItemService.getByScopeIdsAndScene(subProjectIds, orgStatusItem.getScene());
                List<String> statusItemIds = statusItems.stream().map(StatusItem::getId).toList();
                StatusDefinitionExample example = new StatusDefinitionExample();
                example.createCriteria()
                        .andStatusIdIn(statusItemIds)
                        .andDefinitionIdEqualTo(request.getDefinitionId());
                // 如果是单选，需要将其他状态取消勾选
                statusDefinitionMapper.deleteByExample(example);
            });
        }
    }

    /**
     * 添加状态选项
     *
     * @param request
     * @return
     */
    public StatusItem addStatusItem(StatusItemAddRequest request) {
        OrganizationService.checkResourceExist(request.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(request.getScopeId(), request.getScene());
        StatusItem statusItem = new StatusItem();
        BeanUtils.copyBean(statusItem, request);
        statusItem.setScopeType(TemplateScopeType.ORGANIZATION.name());
        statusItem = baseStatusItemService.add(statusItem);
        // 同步添加项目级别状态项
        addRefProjectStatusItem(request.getScopeId(), statusItem);
        // 处理所有状态都可以流转到该状态
        super.handleAllTransferTo(request, statusItem.getId());
        return statusItem;
    }

    /**
     * 同步添加项目级别状态项
     */
    public void addRefProjectStatusItem(String orgId, StatusItem orgStatusItem) {
        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        SubListUtils.dealForSubList(projectIds, 200, (subProjectIds) -> {
            List<StatusItem> projectStatusItems = subProjectIds.stream().map(projectId -> {
                StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), orgStatusItem);
                statusItem.setScopeId(projectId);
                statusItem.setRefId(orgStatusItem.getId());
                statusItem.setScopeType(TemplateScopeType.PROJECT.name());
                return statusItem;
            }).toList();
            baseStatusItemService.batchAdd(projectStatusItems);
        });
    }

    /**
     * 修改状态选项
     *
     * @param request
     * @return
     */
    public StatusItem updateStatusItem(StatusItemUpdateRequest request) {
        StatusItem originStatusItem = baseStatusItemService.getWithCheck(request.getId());
        OrganizationService.checkResourceExist(originStatusItem.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(originStatusItem.getScopeId(), originStatusItem.getScene());
        baseStatusItemService.handleInternalNameUpdate(request, originStatusItem);
        if (StringUtils.isAllBlank(request.getName(), request.getRemark())) {
            // 避免没有字段更新，报错
            return originStatusItem;
        }
        StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), request);
        statusItem.setScopeId(originStatusItem.getScopeId());
        statusItem.setScene(originStatusItem.getScene());
        baseStatusItemService.checkUpdateExist(statusItem);
        statusItem = baseStatusItemService.update(statusItem);

        // 同步添加项目级别状态项
        updateRefProjectStatusItem(statusItem);
        return statusItem;
    }

    /**
     * 同步更新项目级别状态项
     */
    public void updateRefProjectStatusItem(StatusItem orgStatusItem) {
        List<StatusItem> projectStatusItems = baseStatusItemService.getByRefId(orgStatusItem.getId());
        StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), orgStatusItem);
        projectStatusItems.forEach(projectTemplate -> {
            statusItem.setId(projectTemplate.getId());
            statusItem.setScopeId(projectTemplate.getScopeId());
            statusItem.setRefId(orgStatusItem.getId());
            baseStatusItemService.update(statusItem);
        });
    }

    /**
     * 删除状态选项
     *
     * @param id
     * @return
     */
    @Override
    public void deleteStatusItem(String id) {
        StatusItem statusItem = baseStatusItemService.getWithCheck(id);
        OrganizationService.checkResourceExist(statusItem.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(statusItem.getScopeId(), statusItem.getScene());
        // 同步删除项目级别状态项
        deleteRefStatusItem(id);
        super.deleteStatusItem(id);
    }

    /**
     * 同步删除项目级别状态项
     *
     * @param orgStatusItemId
     */
    public void deleteRefStatusItem(String orgStatusItemId) {
        // 删除关联的项目状态项
        List<String> statusItemIds = baseStatusItemService.getStatusItemIdByRefId(orgStatusItemId);
        SubListUtils.dealForSubList(statusItemIds, 100, (subStatusItemIds) -> {
            // 删除相关的状态定义
            baseStatusDefinitionService.deleteByStatusIds(subStatusItemIds);
            // 删除相关的状态流
            baseStatusFlowService.deleteByStatusIds(subStatusItemIds);
        });
        baseStatusItemService.deleteByRefId(orgStatusItemId);
    }

    /**
     * 更新状态流配置
     *
     * @param request
     */
    public void updateStatusFlow(StatusFlowUpdateRequest request) {
        StatusItem fromStatusItem = baseStatusItemService.getWithCheck(request.getFromId());
        StatusItem toStatusItem = baseStatusItemService.getWithCheck(request.getToId());
        OrganizationService.checkResourceExist(fromStatusItem.getScopeId());
        OrganizationService.checkResourceExist(toStatusItem.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(fromStatusItem.getScopeId(), fromStatusItem.getScene());
        super.updateStatusFlow(request);
        // 同步添加项目级别状态流
        updateRefProjectStatusFlow(request);
    }

    /**
     * 同步更新项目级别状态流
     *
     * @param request
     * @return
     */
    public void updateRefProjectStatusFlow(StatusFlowUpdateRequest request) {

        // 获取from和to状态项
        List<StatusItem> fromStatusItems = baseStatusItemService.getByRefId(request.getFromId());
        Map<String, StatusItem> fromStatusItemMap = fromStatusItems.stream()
                .collect(Collectors.toMap(StatusItem::getScopeId, Function.identity()));
        List<StatusItem> toStatusItems = baseStatusItemService.getByRefId(request.getToId());
        Map<String, StatusItem> toStatusItemMap = toStatusItems.stream()
                .collect(Collectors.toMap(StatusItem::getScopeId, Function.identity()));

        if (request.getEnable()) {
            // 同步添加项目级别状态流
            List<StatusFlow> statusFlows = new ArrayList<>();
            for (String projectId : fromStatusItemMap.keySet()) {
                String fromId = fromStatusItemMap.get(projectId).getId();
                String toId = toStatusItemMap.get(projectId).getId();
                if (StringUtils.isNotBlank(fromId) && StringUtils.isNotBlank(toId)) {
                    StatusFlow statusFlow = new StatusFlow();
                    statusFlow.setFromId(fromId);
                    statusFlow.setToId(toId);
                    statusFlow.setId(IDGenerator.nextStr());
                    statusFlows.add(statusFlow);
                }
            }
            SubListUtils.dealForSubList(statusFlows, 200, baseStatusFlowService::batchAdd);
        } else {
            // 同步删除项目级别状态流
            List<String> subProjectFromIds = new ArrayList<>();
            List<String> subProjectToIds = new ArrayList<>();
            for (String projectId : fromStatusItemMap.keySet()) {
                String fromId = fromStatusItemMap.get(projectId).getId();
                String toId = toStatusItemMap.get(projectId).getId();
                if (StringUtils.isNotBlank(fromId) && StringUtils.isNotBlank(toId)) {
                    subProjectFromIds.add(fromId);
                    subProjectToIds.add(toId);
                    if (subProjectFromIds.size() > 100) {
                        // 分批删除
                        baseStatusFlowService.deleteByFromIdsAndToIds(subProjectFromIds, subProjectToIds);
                        subProjectFromIds.clear();
                        subProjectToIds.clear();
                    }
                }
            }
            baseStatusFlowService.deleteByFromIdsAndToIds(subProjectFromIds, subProjectToIds);
        }
    }

    @Override
    public List<StatusItem> sortStatusItem(String organizationId, String scene, List<String> statusIds) {
        OrganizationService.checkResourceExist(organizationId);
        organizationTemplateService.checkOrganizationTemplateEnable(organizationId, scene);
        List<StatusItem> statusItems = super.sortStatusItem(organizationId, scene, statusIds);
        // 同步更新项目级别状态项
        for (StatusItem statusItem : statusItems) {
            StatusItem copyStatusItem = new StatusItem();
            copyStatusItem.setPos(statusItem.getPos());
            baseStatusItemService.updateByRefId(copyStatusItem, statusItem.getId());
        }
        return statusItems;
    }
}
