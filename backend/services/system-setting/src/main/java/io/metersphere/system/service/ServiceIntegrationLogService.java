package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * @author jianxing
 * @date : 2023-8-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ServiceIntegrationLogService {

    @Resource
    private ServiceIntegrationService serviceIntegrationService;
    @Resource
    private BasePluginService basePluginService;

    public LogDTO addLog(ServiceIntegrationUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_SERVICE,
                getName(request.getPluginId()));
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    private String getName(String pluginId) {
        return basePluginService.get(pluginId).getName();
    }

    public LogDTO updateLog(ServiceIntegrationUpdateRequest request) {
        ServiceIntegration serviceIntegration = serviceIntegrationService.get(request.getId());
        LogDTO dto = null;
        if (serviceIntegration != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    serviceIntegration.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_SERVICE,
                    getName(serviceIntegration.getPluginId()));
            dto.setOriginalValue(JSON.toJSONBytes(serviceIntegration));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ServiceIntegration serviceIntegration = serviceIntegrationService.get(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                serviceIntegration.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_ORGANIZATION_SERVICE,
                getName(serviceIntegration.getPluginId()));
        dto.setOriginalValue(JSON.toJSONBytes(serviceIntegration));
        return dto;
    }
}