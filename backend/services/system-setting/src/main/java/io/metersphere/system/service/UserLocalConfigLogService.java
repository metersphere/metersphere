package io.metersphere.system.service;

import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserLocalConfig;
import io.metersphere.system.dto.UserLocalConfigUpdateRequest;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.UserLocalConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserLocalConfigLogService {

    @Resource
    private UserLocalConfigMapper userLocalConfigMapper;

    public LogDTO updateLog(UserLocalConfigUpdateRequest request) {
        UserLocalConfig userLocalConfig = userLocalConfigMapper.selectByPrimaryKey(request.getId());
        if (userLocalConfig != null) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.PERSONAL_INFORMATION_LOCAL_CONFIG)
                    .method(HttpMethodConstants.POST.name())
                    .path("/user/local/config/update")
                    .sourceId(userLocalConfig.getId())
                    .content(userLocalConfig.getUserUrl())
                    .originalValue(JSON.toJSONBytes(userLocalConfig))
                    .build().getLogDTO();
            return dto;
        }
        return null;
    }

    public LogDTO enableLog(String id) {
        UserLocalConfig userLocalConfig = userLocalConfigMapper.selectByPrimaryKey(id);
        if (userLocalConfig != null) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.PERSONAL_INFORMATION_LOCAL_CONFIG)
                    .method(HttpMethodConstants.GET.name())
                    .path("/user/local/config/enable")
                    .sourceId(id)
                    .content(userLocalConfig.getUserUrl())
                    .originalValue(JSON.toJSONBytes(userLocalConfig))
                    .build().getLogDTO();
            return dto;
        }
        return null;
    }

    public LogDTO disableLog(String id) {
        UserLocalConfig userLocalConfig = userLocalConfigMapper.selectByPrimaryKey(id);
        if (userLocalConfig != null) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.PERSONAL_INFORMATION_LOCAL_CONFIG)
                    .method(HttpMethodConstants.GET.name())
                    .path("/user/local/config/disable")
                    .sourceId(id)
                    .content(userLocalConfig.getUserUrl())
                    .originalValue(JSON.toJSONBytes(userLocalConfig))
                    .build().getLogDTO();
            return dto;
        }
        return null;
    }

}
