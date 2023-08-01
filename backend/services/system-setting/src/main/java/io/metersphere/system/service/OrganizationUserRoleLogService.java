package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.request.OrganizationUserRoleEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberEditRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationUserRoleLogService {

    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 新增组织-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO addLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.ORGANIZATION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 更新组织-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                null,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.ORGANIZATION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 删除组织-用户组
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                null,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.ORGANIZATION_USER_ROLE,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole));
        return dto;
    }

    /**
     * 更新组织-用户组-权限
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updatePermissionSettingLog(PermissionSettingUpdateRequest request) {
        LogDTO dto = getLog(request.getUserRoleId());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 更新组织-用户组-成员
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO editMemberLog(OrganizationUserRoleMemberEditRequest request) {
        LogDTO dto = getLog(request.getUserRoleId());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    private LogDTO getLog(String roleId) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(roleId);
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                null,
                null,
                null,
                OperationLogModule.ORGANIZATION_USER_ROLE,
                userRole.getName());
    }
}
