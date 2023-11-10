package io.metersphere.api.service.definition;

import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiTestCaseLogService {

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ProjectMapper projectMapper;

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(ApiTestCaseAddRequest request) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                project.getOrganizationId(),
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.API_DEFINITION,
                request.getName());

        dto.setPath("/api/testCase/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

}
