package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationProjectLogService {

    @Resource
    private ProjectMapper projectMapper;

    /**
     * 添加接口日志
     *
     * @return
     */
    public LogDTO addLog(AddProjectRequest project) {
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                project.getName());

        dto.setOriginalValue(JSON.toJSONBytes(project));
        return dto;
    }

    /**
     * @param request
     * @return
     */
    public LogDTO updateLog(UpdateProjectRequest request) {
        Project project = projectMapper.selectByPrimaryKey(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    project.getId(),
                    project.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    public LogDTO updateLog(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    project.getId(),
                    project.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_PROJECT,
                    project.getName());
            dto.setMethod(HttpMethodConstants.GET.name());

            dto.setOriginalValue(JSON.toJSONBytes(project));
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
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    id,
                    project.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    /**
     * 恢复项目
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());
            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }
}
