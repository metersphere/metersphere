package io.metersphere.system.service;


import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.mapper.AuthSourceMapper;
import io.metersphere.system.request.AuthSourceRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthSourceLogService {
    @Resource
    private AuthSourceMapper authSourceMapper;

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(AuthSourceRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                request.getId(),
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_PARAMETER_SETTING,
                request.getName());

        dto.setModifiedValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 修改接口日志
     *
     * @param request
     * @return
     */
    public LogDTO updateLog(AuthSourceRequest request) {
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(request.getId());
        LogDTO dto = null;
        if (authSource != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PARAMETER_SETTING,
                    request.getName());

            dto.setOriginalValue(JSON.toJSONBytes(authSource));
        }
        return dto;
    }

    public LogDTO updateLog(String id) {
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(id);
        LogDTO dto = null;
        if (authSource != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    id,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PARAMETER_SETTING,
                    authSource.getName());

            dto.setOriginalValue(JSON.toJSONBytes(authSource));
        }
        return dto;
    }

    /**
     * 删除接口日志
     *
     * @param id
     * @return
     */
    public LogDTO deleteLog(String id) {
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(id);
        if (authSource == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                authSource.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SYSTEM_PARAMETER_SETTING,
                authSource.getName());

        dto.setOriginalValue(JSON.toJSONBytes(authSource));
        return dto;
    }

}
