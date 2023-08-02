package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class LicenseLogService {

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO addLog() {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SYSTEM_AUTHORIZATION_MANAGEMENT,
                "License授权");

        dto.setPath("/license/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }

}
