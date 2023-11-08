package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectLogService {

    @Resource
    private ProjectMapper projectMapper;

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
                    OperationLogModule.PROJECT_MANAGEMENT,
                    request.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }
}
