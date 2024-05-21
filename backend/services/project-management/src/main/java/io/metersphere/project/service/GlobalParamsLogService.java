package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.GlobalParamsRequest;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.ProjectParameter;
import io.metersphere.sdk.mapper.ProjectParameterMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalParamsLogService {

    @Resource
    private ProjectParameterMapper projectParametersMapper;
    @Resource
    private ProjectMapper projectMapper;


    public LogDTO addLog(GlobalParamsRequest request) {
        Project project = getProject(request.getProjectId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_ENVIRONMENT,
                Translator.get("global_request_header"));

        dto.setOriginalValue(JSON.toJSONBytes(request.getGlobalParams()));
        return dto;
    }

    public LogDTO updateLog(GlobalParamsRequest request) {
        Project project = getProject(request.getProjectId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_ENVIRONMENT,
                Translator.get("global_request_header"));
        ProjectParameter projectParameters = projectParametersMapper.selectByPrimaryKey(request.getId());
        dto.setOriginalValue(projectParameters.getParameters());
        dto.setModifiedValue(JSON.toJSONBytes(request.getGlobalParams()));
        return dto;
    }

    private Project getProject(String id) {
        return projectMapper.selectByPrimaryKey(id);
    }
}
