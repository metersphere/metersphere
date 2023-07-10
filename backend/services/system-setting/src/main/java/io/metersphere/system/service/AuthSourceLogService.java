package io.metersphere.system.service;


import io.metersphere.constants.HttpMethodConstants;
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

    private static final String PRE_URI = "/system/authsource";

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(AuthSourceRequest request) {
        LogDTO dto = new LogDTO(
                "system",
                request.getId(),
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_PARAMETER_SETTING,
                request.getName());

        dto.setPath(PRE_URI + "/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
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
        if (authSource != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PARAMETER_SETTING,
                    request.getName());

            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(authSource));
            return dto;
        }
        return null;
    }

    public LogDTO updateLog(String id) {
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(id);
        if (authSource != null) {
            LogDTO dto = new LogDTO(
                    "system",
                    id,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PARAMETER_SETTING,
                    authSource.getName());

            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(authSource));
            return dto;
        }
        return null;
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
                "system",
                authSource.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SYSTEM_PARAMETER_SETTING,
                authSource.getName());

        dto.setPath("/delete");
        dto.setMethod(HttpMethodConstants.POST.name());

        dto.setOriginalValue(JSON.toJSONBytes(authSource));
        return dto;
    }

}
