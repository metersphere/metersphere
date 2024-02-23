package io.metersphere.api.service.debug;

import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.dto.debug.ApiDebugAddRequest;
import io.metersphere.api.dto.debug.ApiDebugUpdateRequest;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDebugLogService {

    @Resource
    private ApiDebugService apiDebugService;

    public LogDTO addLog(ApiDebugAddRequest request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(ApiDebugUpdateRequest request) {
        ApiDebug apiDebug = apiDebugService.get(request.getId());
        LogDTO dto = null;
        if (apiDebug != null) {
            dto = new LogDTO(
                    apiDebug.getProjectId(),
                    null,
                    apiDebug.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG,
                    apiDebug.getName());
            dto.setOriginalValue(JSON.toJSONBytes(apiDebug));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ApiDebug apiDebug = apiDebugService.get(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                apiDebug.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG,
                apiDebug.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiDebug));
        return dto;
    }

    public LogDTO moveLog(String id) {
        ApiDebug apiDebug = apiDebugService.get(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                apiDebug.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG,
                apiDebug.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiDebug));
        return dto;
    }
}