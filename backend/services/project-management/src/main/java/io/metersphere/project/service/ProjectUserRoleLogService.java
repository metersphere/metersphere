package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectUserRoleEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberEditRequest;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectUserRoleLogService {

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ProjectMapper projectMapper;

    /**
     * 新增项目-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO addLog(ProjectUserRoleEditRequest request) {
        Project project = getProject(request.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 更新项目-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(ProjectUserRoleEditRequest request) {
        Project project = getProject(request.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                request.getName());

        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andIdEqualTo(request.getId());
        UserRole userRole = userRoleMapper.selectByExample(example).getFirst();
        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        dto.setModifiedValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 删除项目-用户组
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(id);
        Project project = getProject(userRole.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        return dto;
    }

    /**
     * 更新项目-用户组-权限
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
     * 更新项目-用户组-成员
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO editMemberLog(ProjectUserRoleMemberEditRequest request) {
        Project project = getProject(request.getProjectId());
        UserRole userRole = userRoleMapper.selectByPrimaryKey(request.getUserRoleId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        return dto;
    }

    private LogDTO getLog(String roleId) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(roleId);
        Project project = getProject(userRole.getScopeId());
        return new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());
    }

    private Project getProject(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }
}
