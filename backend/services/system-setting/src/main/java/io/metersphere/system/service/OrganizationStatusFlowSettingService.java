package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.dto.request.StatusItemAddRequest;
import io.metersphere.sdk.dto.request.StatusItemUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusFlowSettingDTO;
import io.metersphere.system.mapper.BaseProjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * @param organizationId
     * @param scene
     * @return
     */
    @Override
    public StatusFlowSettingDTO getStatusFlowSetting(String organizationId, String scene) {
        OrganizationService.checkResourceExist(organizationId);
        return super.getStatusFlowSetting(organizationId, scene);
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     * @param request
     */
    @Override
    public void updateStatusDefinition(StatusDefinitionUpdateRequest request) {
        OrganizationService.checkResourceExist(request.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(request.getScopeId(), request.getScene());
        super.updateStatusDefinition(request);
        updateRefProjectStatusDefinition(request);
    }

    /**
     * 同步更新项目级别状态定义
     *
     * @param request
     * @return
     */
    public void updateRefProjectStatusDefinition(StatusDefinitionUpdateRequest request) {
        String orgId = request.getScopeId();
        List<StatusDefinitionUpdateRequest.StatusDefinitionRequest> orgStatusDefinitions = request.getStatusDefinitions();
        if (orgStatusDefinitions == null) {
            return;
        }

        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        SubListUtils.dealForSubList(projectIds, 200, (subProjectIds) -> {
            // 每次处理200条
            List<StatusDefinition> statusDefinitions = new ArrayList<>();
            // 查询组织下所有项目的状态项
            List<StatusItem> statusItems = baseStatusItemService.getByScopeIdsAndScene(subProjectIds, request.getScene());
            Map<String, List<StatusItem>> projectStatusItemMap = statusItems
                    .stream()
                    .collect(Collectors.groupingBy(StatusItem::getScopeId));

            for (String projectId : projectStatusItemMap.keySet()) {
                List<StatusItem> projectStatusItems = projectStatusItemMap.get(projectId);
                // 构建map，键为组织状态ID，值为项目字状态ID
                Map<String, String> refFieldMap = projectStatusItems
                        .stream()
                        .collect(Collectors.toMap(StatusItem::getRefId, StatusItem::getId));
                // 根据组织状态ID，替换为项目状态ID
                List<StatusDefinition> projectStatusDefinitions = orgStatusDefinitions.stream()
                        .map(item -> {
                            StatusDefinition statusDefinition = BeanUtils.copyBean(new StatusDefinition(), item);
                            statusDefinition.setStatusId(refFieldMap.get(item.getStatusId()));
                            return statusDefinition;
                        })
                        .toList();
                statusDefinitions.addAll(projectStatusDefinitions);
            }
            // 删除项目下的状态项
            List<String> scopeStatusItemIds = statusItems
                    .stream()
                    .map(StatusItem::getId)
                    .toList();
            updateStatusDefinition(scopeStatusItemIds, statusDefinitions);
        });
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
        return statusItem;
    }

    /**
     * 同步添加项目级别状态项
     */
    public void addRefProjectStatusItem(String orgId, StatusItem orgStatusItem) {
        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        SubListUtils.dealForSubList(projectIds, 200, (subProjectIds) -> {
            List projectStatusItems = subProjectIds.stream().map(projectId -> {
                StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), orgStatusItem);
                statusItem.setScopeId((String) projectId);
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
        baseStatusItemService.deleteByRefId(orgStatusItemId);
        List<String> statusItemIds = baseStatusItemService.getStatusItemIdByRefId(orgStatusItemId);
        SubListUtils.dealForSubList(statusItemIds, 100, (subProjectIds) -> {
            // 删除相关的状态定义
            baseStatusDefinitionService.deleteByStatusIds(statusItemIds);
            // 删除相关的状态流
            baseStatusFlowService.deleteByStatusIds(statusItemIds);
        });
    }

    /**
     * 更新状态流配置
     * @param request
     */
    public void updateStatusFlow(StatusFlowUpdateRequest request) {
        OrganizationService.checkResourceExist(request.getScopeId());
        organizationTemplateService.checkOrganizationTemplateEnable(request.getScopeId(), request.getScene());
        // 同步添加项目级别状态流
        super.updateStatusFlow(request);
        updateRefProjectStatusFlow(request);
    }

    /**
     * 同步更新项目级别状态流
     *
     * @param request
     * @return
     */
    public void updateRefProjectStatusFlow(StatusFlowUpdateRequest request) {
        String orgId = request.getScopeId();
        List<StatusFlowUpdateRequest.StatusFlowRequest> statusFlowRequests = request.getStatusFlows();
        if (statusFlowRequests == null) {
            return;
        }

        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        SubListUtils.dealForSubList(projectIds, 100, (subProjectIds) -> {
            List<StatusFlow> statusFlows = new ArrayList<>();

            // 查询组织下所有项目的状态项
            List<StatusItem> statusItems = baseStatusItemService.getByScopeIdsAndScene(subProjectIds, request.getScene());
            Map<String, List<StatusItem>> projectStatusItemMap = statusItems
                    .stream()
                    .collect(Collectors.groupingBy(StatusItem::getScopeId));
            List<String> statusItemIds = statusItems.stream().map(StatusItem::getId).toList();

            for (String projectId : projectStatusItemMap.keySet()) {
                // 构建map，键为组织状态ID，值为项目字状态ID
                Map<String, String> refStatusItemMap = projectStatusItemMap.get(projectId)
                        .stream()
                        .collect(Collectors.toMap(StatusItem::getRefId, StatusItem::getId));
                // 根据组织状态ID，替换为项目状态ID
                List<StatusFlow> projectStatusFlows = statusFlowRequests.stream()
                        .map(item -> {
                            StatusFlow statusFlow = new StatusFlow();
                            statusFlow.setToId(refStatusItemMap.get(item.getToId()));
                            statusFlow.setFromId(refStatusItemMap.get(item.getFromId()));
                            return statusFlow;
                        })
                        .filter(item -> item.getToId() != null && item.getFromId() != null)
                        .toList();
                statusFlows.addAll(projectStatusFlows);
            }
            // 先删除
            baseStatusFlowService.deleteByStatusIds(statusItemIds);
            // 在添加
            baseStatusFlowService.batchAdd(statusFlows);
        });
    }
}