package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.ApiScenarioAddRequest;
import io.metersphere.api.dto.scenario.ApiScenarioUpdateRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiScenarioLogService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    public void batchEditLog(List<ApiScenario> scenarioList, String operator, String projectId) {
        saveBatchLog(projectId, scenarioList, "/api/scenario/batch/edit", operator, OperationLogType.UPDATE.name(), true);
    }


    private void saveBatchLog(String projectId, List<ApiScenario> scenarioList, String path, String operator, String operationType, boolean isHistory) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        //取出apiTestCases所有的id为新的list
        List<String> scenarioIds = scenarioList.stream().map(ApiScenario::getId).distinct().toList();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(scenarioIds);
        List<ApiScenario> apiScenarioList = apiScenarioMapper.selectByExample(example);
        List<LogDTO> logs = new ArrayList<>();
        apiScenarioList.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(operationType)
                            .module(OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO)
                            .method(HttpMethodConstants.POST.name())
                            .path(path)
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .originalValue(JSON.toJSONBytes(item))
                            .build().getLogDTO();
                    dto.setHistory(isHistory);
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void followLog(String id, String operator) {
        ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(scenario.getProjectId());
        LogDTO dto = new LogDTO(
                scenario.getProjectId(),
                project.getOrganizationId(),
                id,
                operator,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                Translator.get("follow") + scenario.getName());

        dto.setPath("/api/scenario/follow/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(scenario));
        operationLogService.add(dto);
    }

    public void unfollowLog(String id, String operator) {
        ApiScenario apiTestCase = apiScenarioMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                operator,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                Translator.get("unfollow") + apiTestCase.getName());

        dto.setPath("/api/scenario/follow/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        operationLogService.add(dto);
    }

    public LogDTO addLog(ApiScenarioAddRequest request) {
        // todo 记录完整的场景信息
        LogDTO dto = new LogDTO(
                null,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                request.getName());
        dto.setHistory(true);
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(ApiScenarioUpdateRequest request) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        // todo 记录完整的场景信息
        LogDTO dto = new LogDTO(
                null,
                null,
                apiScenario.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                request.getName());
        dto.setHistory(true);
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO scheduleLog(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiScenario.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO)
                .sourceId(apiScenario.getId())
                .content(Translator.get("api_automation_schedule") + ":" + apiScenario.getName())
                .build().getLogDTO();
        return dto;
    }

    public LogDTO updateLog(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        // todo 记录完整的场景信息
        LogDTO dto = new LogDTO(
                null,
                null,
                apiScenario.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                apiScenario.getName());
        dto.setHistory(true);
        dto.setOriginalValue(JSON.toJSONBytes(apiScenario));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                apiScenario.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_TEST_SCENARIO_RECYCLE,
                apiScenario.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiScenario));
        return dto;
    }

    public LogDTO moveToGcLog(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                apiScenario.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                apiScenario.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiScenario));
        return dto;
    }

    public LogDTO restoreLog(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        LogDTO dto = new LogDTO(
                apiScenario.getProjectId(),
                null,
                apiScenario.getId(),
                null,
                OperationLogType.RESTORE.name(),
                OperationLogModule.API_TEST_SCENARIO_RECYCLE,
                apiScenario.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiScenario));
        return dto;
    }

    public void saveBatchOperationLog(ApiScenarioBatchOperationResponse response, String projectId, String operationType, LogInsertModule logInsertModule, String logModule, boolean isHistory) {

        if (StringUtils.isBlank(logModule)) {
            logModule = OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO;
        }
        Project project = projectMapper.selectByPrimaryKey(projectId);
        //取出apiTestCases所有的id为新的list
        List<LogDTO> logs = new ArrayList<>();
        String finalLogModule = logModule;
        response.getSuccessData().forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(operationType)
                            .module(finalLogModule)
                            .method(logInsertModule.getRequestMethod())
                            .path(logInsertModule.getRequestUrl())
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(logInsertModule.getOperator())
                            .build().getLogDTO();
                    dto.setHistory(isHistory);
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void batchScheduleConfigLog(String projectId, List<ApiScenario> scenarioList, String operator) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        scenarioList.forEach(apiScenario -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.UPDATE.name())
                            .module(OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO)
                            .sourceId(apiScenario.getId())
                            .method("POST")
                            .path("/api/scenario/batch-operation/schedule-config")
                            .createUser(operator)
                            .content(Translator.get("api_automation_schedule") + ":" + apiScenario.getName())
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public LogDTO exportExcelLog(String sourceId, String exportType, String userId, @NotNull Project project) {
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                sourceId,
                userId,
                OperationLogType.EXPORT.name(),
                OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO,
                "");
        dto.setHistory(true);
        dto.setPath("/api/scenario/export/" + exportType);
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }
}
