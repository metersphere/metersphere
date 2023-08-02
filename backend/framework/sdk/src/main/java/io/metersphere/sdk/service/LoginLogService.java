package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoginLogService {

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO loginLog() {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.LOGIN.name(),
                OperationLogConstants.SYSTEM,
                "登录");
        return dto;
    }

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO logoutLog() {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.LOGIN.name(),
                OperationLogConstants.SYSTEM,
                "登出");
        return dto;
    }

}
