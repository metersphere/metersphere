package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.api.domain.ApiDefinitionMockConfigExample;
import io.metersphere.api.domain.ApiDefinitionMockExample;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.aspect.OperationLogAspect;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: LAN
 * @date: 2023/12/7 17:29
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionMockLogService {

    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;

    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;

    /**
     * 添加日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(ApiDefinitionMockAddRequest request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.API_TEST_MANAGEMENT_MOCK,
                request.getName());
        dto.setHistory(true);
        dto.setPath("/api/definition/mock/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 修改日志
     *
     * @param request
     * @return
     */
    public LogDTO updateLog(ApiDefinitionMockUpdateRequest request) {
        ApiDefinitionMockDTO apiDefinitionMock = getOriginalValue(request.getId());
        if(apiDefinitionMock.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_MOCK,
                    request.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/mock/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionMock));
            return dto;
        }
        return null;
    }

    public LogDTO updateEnableLog(String id) {
        ApiDefinitionMockDTO apiDefinitionMock = getOriginalValue(id);
        if(apiDefinitionMock.getId() != null){
            LogDTO dto = new LogDTO(
                    apiDefinitionMock.getProjectId(),
                    null,
                    apiDefinitionMock.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_MOCK,
                    apiDefinitionMock.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/mock/enable/" + id);
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionMock));
            return dto;
        }
        return null;
    }

    /**
     * 删除日志
     *
     * @param request
     * @return
     */
    public LogDTO delLog(ApiDefinitionMockRequest request) {
        ApiDefinitionMockDTO apiDefinitionMock = getOriginalValue(request.getId());
        if(apiDefinitionMock.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_MOCK,
                    apiDefinitionMock.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/mock/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionMock));
            return dto;
        }

        return null;
    }

    public LogDTO copyLog(ApiDefinitionMockRequest request) {
        ApiDefinitionMockDTO apiDefinitionMock = getOriginalValue(request.getId());
        if(apiDefinitionMock.getId() != null){
            LogDTO dto = new LogDTO(
                    apiDefinitionMock.getProjectId(),
                    null,
                    null,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_MOCK,
                    apiDefinitionMock.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/mock/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionMock));
            return dto;
        }
        return null;
    }

    private ApiDefinitionMockDTO getOriginalValue(String id){
        ApiDefinitionMockDTO apiDefinitionMockDTO = new ApiDefinitionMockDTO();
        ApiDefinitionMock apiDefinitionMock = apiDefinitionMockMapper.selectByPrimaryKey(id);
        if(null != apiDefinitionMock){
            handleMockConfig(id, apiDefinitionMockDTO);
            BeanUtils.copyBean(apiDefinitionMockDTO, apiDefinitionMock);
        }
        return apiDefinitionMockDTO;
    }

    public void handleMockConfig(String id, ApiDefinitionMockDTO apiDefinitionMockDTO) {
        Optional<ApiDefinitionMockConfig> apiDefinitionMockConfigOptional = Optional.ofNullable(apiDefinitionMockConfigMapper.selectByPrimaryKey(id));
        apiDefinitionMockConfigOptional.ifPresent(config -> {
            apiDefinitionMockDTO.setMockMatchRule(ApiDataUtils.parseObject(new String(config.getMatching()), MockMatchRule.class));
            apiDefinitionMockDTO.setResponse(ApiDataUtils.parseObject(new String(config.getResponse()), MockResponse.class));
        });
    }

    public void batchEditLog(List<ApiDefinitionMock> apiMocks, String operator, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<String> mockIds = apiMocks.stream().map(ApiDefinitionMock::getId).distinct().toList();
        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria().andIdIn(mockIds);
        List<ApiDefinitionMock> apiMockLists = apiDefinitionMockMapper.selectByExample(example);
        Map<String, ApiDefinitionMock> mockMap = apiMockLists.stream().collect(Collectors.toMap(ApiDefinitionMock::getId, a -> a));
        ApiDefinitionMockConfigExample blobExample = new ApiDefinitionMockConfigExample();
        blobExample.createCriteria().andIdIn(mockIds);
        List<ApiDefinitionMockConfig> blobList = apiDefinitionMockConfigMapper.selectByExampleWithBLOBs(blobExample);
        Map<String, ApiDefinitionMockConfig> blobMap = blobList.stream().collect(Collectors.toMap(ApiDefinitionMockConfig::getId, a -> a));
        List<LogDTO> logs = new ArrayList<>();
        apiMocks.forEach(item -> {
                    ApiDefinitionMockDTO mockDTO = new ApiDefinitionMockDTO();
                    BeanUtils.copyBean(mockDTO, mockMap.get(item.getId()));
                    if (blobMap.get(item.getId()) != null) {
                        mockDTO.setMockMatchRule(ApiDataUtils.parseObject(new String(blobMap.get(item.getId()).getMatching()), MockMatchRule.class));
                        mockDTO.setResponse(ApiDataUtils.parseObject(new String(blobMap.get(item.getId()).getResponse()), MockResponse.class));
                    }
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.UPDATE.name())
                            .module(OperationLogModule.API_TEST_MANAGEMENT_MOCK)
                            .method(HttpMethodConstants.POST.name())
                            .path(OperationLogAspect.getPath())
                            .sourceId(item.getId())
                            .content(item.getName())
                            .createUser(operator)
                            .originalValue(JSON.toJSONBytes(mockDTO))
                            .build().getLogDTO();
                    dto.setHistory(true);
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
    }

    public void deleteBatchLog(List<ApiDefinitionMock> mockList, String userId, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> logs = new ArrayList<>();
        mockList.forEach(item -> {
                    LogDTO dto = LogDTOBuilder.builder()
                            .projectId(project.getId())
                            .organizationId(project.getOrganizationId())
                            .type(OperationLogType.DELETE.name())
                            .module(OperationLogModule.API_TEST_MANAGEMENT_MOCK)
                            .method(HttpMethodConstants.POST.name())
                            .sourceId(item.getId())
                            .content(item.getName())
                            .path(OperationLogAspect.getPath())
                            .createUser(userId)
                            .build().getLogDTO();
                    logs.add(dto);
                }
        );
        operationLogService.batchAdd(logs);
        operationLogService.deleteBySourceIds(mockList.stream().map(ApiDefinitionMock::getId).toList());
    }
}
