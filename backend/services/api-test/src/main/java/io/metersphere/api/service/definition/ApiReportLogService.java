package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiReportLogService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private ApiReportMapper apiReportMapper;


    public LogDTO deleteLog(String id) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiReport.getProjectId());
        LogDTO dto = new LogDTO(
                apiReport.getProjectId(),
                project.getOrganizationId(),
                apiReport.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_REPORT,
                apiReport.getName());

        dto.setPath("/api/report/case/delete/" + apiReport.getId());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiReport));
        return dto;
    }

    public LogDTO updateLog(String id) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiReport.getProjectId());
        LogDTO dto = new LogDTO(
                apiReport.getProjectId(),
                project.getOrganizationId(),
                apiReport.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_REPORT,
                apiReport.getName());

        dto.setPath("/api/report/case/rename/" + apiReport.getId() + "/" + apiReport.getName());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiReport));
        return dto;
    }

    public void batchDeleteLog(List<String> ids, String userId, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<ApiReport> apiReports = extApiReportMapper.selectApiReportByIds(ids);
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

            dto.setPath("/api/report/case/batch/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiReport));
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }

    public void exportLog(List<ApiReport> reports, String userId, String projectId, String path) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        reports.forEach(report -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    project.getOrganizationId(),
                    report.getId(),
                    userId,
                    OperationLogType.EXPORT.name(),
                    OperationLogModule.API_REPORT,
                    report.getName());
            dto.setPath(path);
            dto.setMethod(HttpMethodConstants.POST.name());
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }
}
