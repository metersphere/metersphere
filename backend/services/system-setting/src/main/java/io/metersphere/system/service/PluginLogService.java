package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jianxing
 * @date : 2023-8-3
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginLogService {

    @Resource
    private PluginService pluginService;

    public LogDTO addLog(PluginUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(PluginUpdateRequest request) {
        Plugin plugin = pluginService.get(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                plugin.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
                plugin.getName());
        dto.setOriginalValue(JSON.toJSONBytes(plugin));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Plugin plugin = pluginService.get(id);
        if (plugin == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                plugin.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
                plugin.getName());
        dto.setOriginalValue(JSON.toJSONBytes(plugin));
        return dto;
    }
}