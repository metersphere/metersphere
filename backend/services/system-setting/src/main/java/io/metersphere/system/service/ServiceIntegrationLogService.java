package io.metersphere.system.service;

import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import io.metersphere.sdk.constants.OperationLogConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.constants.OperationLogModule;
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

    public LogDTO addLog(ServiceIntegrationUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.ORGANIZATION_SERVICE_INTEGRATION,
                "创建服务集成");
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(ServiceIntegrationUpdateRequest request) {
        ServiceIntegration serviceIntegration = serviceIntegrationService.get(request.getId());
        LogDTO dto = null;
        if (serviceIntegration != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    serviceIntegration.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.ORGANIZATION_SERVICE_INTEGRATION,
                    "更新服务集成");
            dto.setOriginalValue(JSON.toJSONBytes(serviceIntegration));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ServiceIntegration serviceIntegration = serviceIntegrationService.get(id);
        if (serviceIntegration == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                serviceIntegration.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.ORGANIZATION_SERVICE_INTEGRATION,
                "删除服务集成");
        dto.setOriginalValue(JSON.toJSONBytes(serviceIntegration));
        return dto;
    }
}