package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.api.domain.ApiTestCaseBlobExample;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseLogDTO;
import io.metersphere.api.dto.definition.ApiTestCaseUpdateRequest;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.sdk.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiTestCaseLogService {

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;

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

        dto.setPath("/api/case/add");
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

        dto.setPath("/api/case/delete/" + id);
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

        dto.setPath("/api/case/move-gc/" + id);
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

        dto.setPath("/api/case/recover/" + id);
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

        dto.setPath("/api/case/follow/" + id);
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

        dto.setPath("/api/case/unfollow/" + id);
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCase));
        return dto;
    }

    public LogDTO updateLog(ApiTestCaseUpdateRequest request) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(request.getId());
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                request.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                request.getName());

        dto.setPath("/api/case/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        ApiTestCaseLogDTO apiTestCaseDTO = new ApiTestCaseLogDTO();
        BeanUtils.copyBean(apiTestCaseDTO, apiTestCase);
        apiTestCaseDTO.setRequest(ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCaseDTO));
        return dto;
    }

    public LogDTO updateLog(String id) {
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(id);
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiTestCase.getProjectId());
        LogDTO dto = new LogDTO(
                apiTestCase.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION_CASE,
                apiTestCase.getName());

        dto.setPath("/api/case/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        ApiTestCaseLogDTO apiTestCaseDTO = new ApiTestCaseLogDTO();
        BeanUtils.copyBean(apiTestCaseDTO, apiTestCase);
        apiTestCaseDTO.setRequest(ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));
        dto.setOriginalValue(JSON.toJSONBytes(apiTestCaseDTO));
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
                            .path("/api/case/batch/delete")
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
        saveBatchLog(projectId, apiTestCases, "/api/case/batch/move-gc", operator, OperationLogType.DELETE.name());
    }

    public void batchEditLog(List<ApiTestCase> apiTestCases, String operator, String projectId) {
        saveBatchLog(projectId, apiTestCases, "/api/case/batch/edit", operator, OperationLogType.UPDATE.name());
    }

    public void batchRecoverLog(List<ApiTestCase> apiTestCases, String operator, String projectId) {
        saveBatchLog(projectId, apiTestCases, "/api/case/recover", operator, OperationLogType.RECOVER.name());
    }

    private void saveBatchLog(String projectId, List<ApiTestCase> apiTestCases, String path, String operator, String operationType) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        //取出apiTestCases所有的id为新的list
        List<String> caseId = apiTestCases.stream().map(ApiTestCase::getId).distinct().toList();
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(caseId);
        List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(example);
        //apiTestCaseList按id生成新的map key为id value为ApiTestCase
        Map<String, ApiTestCase> caseMap = apiTestCaseList.stream().collect(Collectors.toMap(ApiTestCase::getId, a -> a));
        ApiTestCaseBlobExample blobExample = new ApiTestCaseBlobExample();
        blobExample.createCriteria().andIdIn(caseId);
        List<ApiTestCaseBlob> blobList = apiTestCaseBlobMapper.selectByExampleWithBLOBs(blobExample);
        //blobList按id生成新的map key为id value为ApiTestCaseBlob
        Map<String, ApiTestCaseBlob> blobMap = blobList.stream().collect(Collectors.toMap(ApiTestCaseBlob::getId, a -> a));
        List<LogDTO> logs = new ArrayList<>();
        apiTestCases.forEach(item -> {
                    ApiTestCaseLogDTO apiTestCaseDTO = new ApiTestCaseLogDTO();
                    BeanUtils.copyBean(apiTestCaseDTO, caseMap.get(item.getId()));
                    if (blobMap.get(item.getId()) != null) {
                        apiTestCaseDTO.setRequest(ApiDataUtils.parseObject(new String(blobMap.get(item.getId()).getRequest()), AbstractMsTestElement.class));
                    }
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(operationType)
                            .module(OperationLogModule.API_DEFINITION_CASE)
                            .method(HttpMethodConstants.POST.name())
                            .path(path)
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .originalValue(JSON.toJSONBytes(apiTestCaseDTO))
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }
}
