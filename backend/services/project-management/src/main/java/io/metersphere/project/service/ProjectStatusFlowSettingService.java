package io.metersphere.project.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.dto.request.StatusItemAddRequest;
import io.metersphere.sdk.dto.request.StatusItemUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusFlowSettingDTO;
import io.metersphere.system.service.BaseStatusFlowSettingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public StatusFlowSettingDTO getStatusFlowSetting(String projectId, String scene) {
        ProjectService.checkResourceExist(projectId);
        return super.getStatusFlowSetting(projectId, scene);
    }

    /**
     * 设置状态定义
     * 比如设置成项目
     * @param request
     */
    @Override
    public void updateStatusDefinition(StatusDefinitionUpdateRequest request) {
        ProjectService.checkResourceExist(request.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(request.getScopeId(), request.getScene());
        super.updateStatusDefinition(request);
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
        return baseStatusItemService.add(statusItem);
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
    public void updateStatusFlow(StatusFlowUpdateRequest request) {
        ProjectService.checkResourceExist(request.getScopeId());
        projectTemplateService.checkProjectTemplateEnable(request.getScopeId(), request.getScene());
        super.updateStatusFlow(request);
    }
}