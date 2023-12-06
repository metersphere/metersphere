package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.EnvironmentGroupRequest;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentGroupLogService {


    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;

    public LogDTO addLog(EnvironmentGroupRequest request) {
        Project project = getProject(request.getProjectId());
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_ENVIRONMENT_GROUP,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }


    public LogDTO updateLog(EnvironmentGroupRequest request) {
        Project project = getProject(request.getProjectId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_ENVIRONMENT_GROUP,
                request.getName());
        EnvironmentGroup environment = environmentGroupMapper.selectByPrimaryKey(request.getId());
        dto.setOriginalValue(JSON.toJSONBytes(environment));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        EnvironmentGroup environment = environmentGroupMapper.selectByPrimaryKey(id);
        Project project = getProject(environment.getProjectId());
        return new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_ENVIRONMENT_GROUP,
                environment.getName());
    }


    private Project getProject(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }
}
