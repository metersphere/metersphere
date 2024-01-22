package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiScenarioReportLogService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;


    public void deleteLog(String id) {
        ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(scenarioReport.getProjectId());
        LogDTO dto = new LogDTO(
                scenarioReport.getProjectId(),
                project.getOrganizationId(),
                scenarioReport.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_REPORT,
                scenarioReport.getName());

        dto.setPath("/api/report/scenario/delete/" + scenarioReport.getId());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(scenarioReport));
        operationLogService.add(dto);
    }

    public void updateLog(String id) {
        ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(scenarioReport.getProjectId());
        LogDTO dto = new LogDTO(
                scenarioReport.getProjectId(),
                project.getOrganizationId(),
                scenarioReport.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_REPORT,
                scenarioReport.getName());

        dto.setPath("/api/report/scenario/rename/" + scenarioReport.getId() + "/" + scenarioReport.getName());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(scenarioReport));
        operationLogService.add(dto);
    }

    @Async
    public void batchDeleteLog(List<String> ids, String userId, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<ApiScenarioReport> apiReports = extApiScenarioReportMapper.selectApiReportByIds(ids, true);
        List<LogDTO> logs = new ArrayList<>();
        apiReports.forEach(apiReport -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    project.getOrganizationId(),
                    apiReport.getId(),
                    userId,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_REPORT,
                    apiReport.getName());

            dto.setPath("/api/report/scenario/batch/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiReport));
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }
}
