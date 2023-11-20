package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseUpdateRequest;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiTestCaseLogService {

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

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
                OperationLogModule.API_DEFINITION_CASE,
                request.getName());

        dto.setPath("/api/testCase/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                apiTestCase.getName());

        dto.setPath("/api/testCase/delete/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO moveToGcLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                apiTestCase.getName());

        dto.setPath("/api/testCase/move-gc/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO recoverLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.RECOVER.name(),
                OperationLogModule.API_DEFINITION_CASE,
                apiTestCase.getName());

        dto.setPath("/api/testCase/recover/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO followLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                Translator.get("follow") + apiTestCase.getName());

        dto.setPath("/api/testCase/follow/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO unfollowLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                Translator.get("unfollow") + apiTestCase.getName());

        dto.setPath("/api/testCase/unfollow/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO updateLog(ApiTestCaseUpdateRequest request) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                request.getName());

        dto.setPath("/api/testCase/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                apiTestCase.getName());

        dto.setPath("/api/testCase/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public void deleteBatchLog(List<ApiTestCase> apiTestCases, String operator, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        apiTestCases.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.DELETE.name())
                            .module(OperationLogModule.API_DEFINITION_CASE)
                            .method(HttpMethodConstants.POST.name())
                            .path("/api/testCase/batch/delete")
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void batchToGcLog(List<ApiTestCase> apiTestCases, String operator, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        apiTestCases.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.DELETE.name())
                            .module(OperationLogModule.API_DEFINITION_CASE)
                            .method(HttpMethodConstants.POST.name())
                            .path("/api/testCase/batch/move-gc")
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void batchEditLog(List<ApiTestCase> apiTestCases, String operator, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        apiTestCases.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.DELETE.name())
                            .module(OperationLogModule.API_DEFINITION_CASE)
                            .method(HttpMethodConstants.POST.name())
                            .path("/api/testCase/batch/move-gc")
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }
}
