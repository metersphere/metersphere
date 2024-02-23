package io.metersphere.project.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.dto.sdk.request.StatusItemUpdateRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.StatusItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-10-16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectStatusFlowSettingLogService {

    @Resource
    private ProjectStatusFlowSettingService projectStatusFlowSettingService;

    @Resource
    private StatusItemMapper statusItemMapper;


    public LogDTO updateStatusDefinitionLog(StatusDefinitionUpdateRequest request) {
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(request.getStatusId());
        return updateStatusFlowSettingLog(statusItem.getScopeId(), statusItem.getScene());
    }

    public LogDTO addStatusItemLog(StatusItemAddRequest request) {
        return updateStatusFlowSettingLog(request.getScopeId(), request.getScene());
    }

    public LogDTO updateStatusItemLog(StatusItemUpdateRequest request) {
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(request.getId());
        return updateStatusFlowSettingLog(statusItem.getScopeId(), statusItem.getScene());
    }

    public LogDTO deleteStatusItemLog(String id) {
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(id);
        return updateStatusFlowSettingLog(statusItem.getScopeId(), statusItem.getScene());
    }

    public LogDTO updateStatusFlowLog(StatusFlowUpdateRequest request) {
        StatusItem statusItem = statusItemMapper.selectByPrimaryKey(request.getFromId());
        return updateStatusFlowSettingLog(statusItem.getScopeId(), statusItem.getScene());
    }

    public LogDTO updateStatusFlowSettingLog(String scopeId, String scene) {
        List<StatusItemDTO> statusItemDTOS = projectStatusFlowSettingService.getStatusFlowSetting(scopeId, scene);
        LogDTO dto = new LogDTO(
                null,
                null,
                scopeId,
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(scene),
                Translator.get("status_flow.name"));
        dto.setOriginalValue(JSON.toJSONBytes(statusItemDTOS));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case BUG:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_WORKFLOW;
            default:
                return null;
        }
    }
}