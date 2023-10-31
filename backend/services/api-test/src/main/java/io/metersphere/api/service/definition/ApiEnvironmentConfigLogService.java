package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiEnvironmentConfigLogService {

    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void addLog(ApiEnvironmentConfig env, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        Environment environment = environmentMapper.selectByPrimaryKey(env.getEnvironmentId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.API_DEFINITION_ENVIRONMENT)
                .method(HttpMethodConstants.GET.name())
                .path("/api/definition/env/add/")
                .sourceId(env.getId())
                .content(environment.getName())
                .createUser(env.getCreateUser())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

}
