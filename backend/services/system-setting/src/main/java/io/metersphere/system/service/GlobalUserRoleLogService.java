package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.dto.sdk.request.UserRoleUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统设置的接口增删改查都是针对全局用户组
 *
 * @author jianxing
 * @date : 2023-6-8
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalUserRoleLogService {
    @Resource
    private BaseUserRoleService baseUserRoleService;

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(UserRoleUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * @param request
     * @return
     */
    public LogDTO updateLog(UserRoleUpdateRequest request) {
        UserRole userRole = baseUserRoleService.get(request.getId());
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    userRole.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JSON.toJSONBytes(userRole));
        }
        return dto;
    }

    public LogDTO updateLog(PermissionSettingUpdateRequest request) {
        UserRole userRole = baseUserRoleService.get(request.getUserRoleId());
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getUserRoleId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JSON.toJSONBytes(request));
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
        UserRole userRole = baseUserRoleService.get(id);
        if (userRole == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole));
        return dto;
    }

}
