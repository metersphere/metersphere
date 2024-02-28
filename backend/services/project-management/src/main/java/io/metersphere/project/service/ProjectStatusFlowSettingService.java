package io.metersphere.project.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.dto.sdk.request.StatusItemUpdateRequest;
import io.metersphere.system.service.BaseStatusFlowSettingService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectStatusFlowSettingService extends BaseStatusFlowSettingService {

    @Resource
    private ProjectTemplateService projectTemplateService;
    /**
     * 查询状态流设置
     * @param projectId
     * @param scene
     * @return
     */
    @Override
    public List<StatusItemDTO> getStatusFlowSetting(String projectId, String scene) {
        ProjectService.checkResourceExist(projectId);
        return super.getStatusFlowSetting(projectId, scene);
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     * @param request
     */
    public void updateStatusDefinition(StatusDefinitionUpdateRequest request) {
        StatusItem statusItem = baseStatusItemService.getWithCheck(request.getStatusId());
        ProjectService.checkResourceExist(statusItem.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(statusItem.getScopeId(), statusItem.getScene());
        super.updateStatusDefinition(statusItem, request);
    }

    /**
     * 添加状态选项
     *
     * @param request
     * @return
     */
    public StatusItem addStatusItem(StatusItemAddRequest request) {
        ProjectService.checkResourceExist(request.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(request.getScopeId(), request.getScene());
        StatusItem statusItem = new StatusItem();
        BeanUtils.copyBean(statusItem, request);
        statusItem.setScopeType(TemplateScopeType.PROJECT.name());
        statusItem = baseStatusItemService.add(statusItem);
        // 处理所有状态都可以流转到该状态
        super.handleAllTransferTo(request, statusItem.getId());
        return statusItem;
    }

    /**
     * 修改状态选项
     *
     * @param request
     * @return
     */
    public StatusItem updateStatusItem(StatusItemUpdateRequest request) {
        StatusItem originStatusItem = baseStatusItemService.getWithCheck(request.getId());
        ProjectService.checkResourceExist(originStatusItem.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(originStatusItem.getScopeId(), originStatusItem.getScene());
        baseStatusItemService.handleInternalNameUpdate(request, originStatusItem);
        if (StringUtils.isAllBlank(request.getName(), request.getRemark())) {
            // 避免没有字段更新，报错
            return originStatusItem;
        }
        StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), request);
        statusItem.setScopeId(originStatusItem.getScopeId());
        statusItem.setScene(originStatusItem.getScene());
        baseStatusItemService.checkUpdateExist(statusItem);
        return baseStatusItemService.update(statusItem);
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
        ProjectService.checkResourceExist(statusItem.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(statusItem.getScopeId(), statusItem.getScene());
        super.deleteStatusItem(id);
    }

    /**
     * 更新状态流配置
     * @param request
     */
    @Override
    public void updateStatusFlow(StatusFlowUpdateRequest request) {
        StatusItem fromStatusItem = baseStatusItemService.getWithCheck(request.getFromId());
        StatusItem toStatusItem = baseStatusItemService.getWithCheck(request.getToId());
        ProjectService.checkResourceExist(fromStatusItem.getScopeId());
        ProjectService.checkResourceExist(toStatusItem.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(fromStatusItem.getScopeId(), fromStatusItem.getScene());
        super.updateStatusFlow(request);
    }

    @Override
    public List<StatusItem> sortStatusItem(String projectId, String scene, List<String> statusIds) {
        ProjectService.checkResourceExist(projectId);
        projectTemplateService.checkProjectTemplateEnable(projectId, scene);
        return super.sortStatusItem(projectId, scene, statusIds);
    }
}