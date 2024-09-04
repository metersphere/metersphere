package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiDefinitionBlobExample;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.aspect.OperationLogAspect;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionLogService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

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
    public LogDTO addLog(ApiDefinitionAddRequest request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                request.getName());

        dto.setHistory(true);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 修改接口日志
     *
     * @param request
     * @return
     */
    public LogDTO updateLog(ApiDefinitionUpdateRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if (apiDefinition.getId() != null) {
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                    request.getName());
            dto.setHistory(true);
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    public LogDTO moveToGcLog(String id) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiDefinition.getProjectId());
        LogDTO dto = new LogDTO(
                apiDefinition.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                apiDefinition.getName());

        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(apiDefinition.getProjectId());
        LogDTO dto = new LogDTO(
                apiDefinition.getProjectId(),
                project.getOrganizationId(),
                id,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.API_TEST_MANAGEMENT_RECYCLE,
                apiDefinition.getName());

        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
        operationLogService.deleteBySourceIds(List.of(id));
        return dto;
    }


    /**
     * 批量删除接口日志
     *
     * @return
     */
    public void batchDelLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, userId, OperationLogType.DELETE.name(), false, null);
    }

    /**
     * 批量更新接口日志
     *
     * @return
     */
    public void batchUpdateLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, userId, OperationLogType.UPDATE.name(), true, null);
    }

    public LogDTO copyLog(ApiDefinitionCopyRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if (apiDefinition.getId() != null) {
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    null,
                    null,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(false);
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    public void batchMoveLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, userId, OperationLogType.UPDATE.name(), true, null);
    }

    public LogDTO followLog(String id) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(id);
        if (apiDefinition.getId() != null) {
            Project project = projectMapper.selectByPrimaryKey(apiDefinition.getProjectId());
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                    Translator.get("follow") + apiDefinition.getName());

            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    /**
     * 恢复回收站接口定义接口日志
     *
     * @param request
     * @return
     */
    public LogDTO recoverLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if (apiDefinition.getId() != null) {
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_RECYCLE,
                    apiDefinition.getName());
            dto.setHistory(false);
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }

        return null;
    }


    /**
     * 批量恢复回收站接口定义接口日志
     *
     * @return
     */
    public void batchRecoverLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, userId, OperationLogType.RECOVER.name(), false, OperationLogModule.API_TEST_MANAGEMENT_RECYCLE);
    }

    /**
     * 删除回收站接口定义接口日志
     */
    public void batchTrashDelLog(List<String> ids, String userId, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                BeanUtils.copyBean(apiDefinitionDTO, item);
                LogDTO dto = new LogDTO(
                        project.getId(),
                        project.getOrganizationId(),
                        item.getId(),
                        userId,
                        OperationLogType.DELETE.name(),
                        OperationLogModule.API_TEST_MANAGEMENT_RECYCLE,
                        item.getName());

                dto.setHistory(false);
                dto.setPath(OperationLogAspect.getPath());
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
                dtoList.add(dto);
            });
            operationLogService.batchAdd(dtoList);
        }
    }

    private ApiDefinitionDTO getOriginalValue(String id) {
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        if (null != apiDefinition) {
            // 2. 使用Optional避免空指针异常
            CommonBeanFactory.getBean(ApiDefinitionService.class).handleBlob(id, apiDefinitionDTO);
            BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        }
        return apiDefinitionDTO;
    }


    private void saveBatchLog(String projectId, List<String> ids, String userId, String operationType, boolean isHistory, String logModule) {
        if (StringUtils.isBlank(logModule)) {
            logModule = OperationLogModule.API_TEST_MANAGEMENT_DEFINITION;
        }
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            ApiDefinitionBlobExample blobExample = new ApiDefinitionBlobExample();
            blobExample.createCriteria().andIdIn(ids);
            List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionBlobMapper.selectByExampleWithBLOBs(blobExample);
            //生成map 使用流  key是id value是blob
            Map<String, ApiDefinitionBlob> blobMap = apiDefinitionBlobs.stream().collect(Collectors.toMap(ApiDefinitionBlob::getId, b -> b));
            String finalLogModule = logModule;
            apiDefinitions.forEach(item -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                ApiDefinitionBlob blob = blobMap.get(item.getId());
                if (null != blob) {
                    apiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class));
                    if (blob.getResponse() != null) {
                        List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
                        apiDefinitionDTO.setResponse(httpResponses);
                    }
                }
                BeanUtils.copyBean(apiDefinitionDTO, item);
                LogDTO dto = new LogDTO(
                        project.getId(),
                        project.getOrganizationId(),
                        item.getId(),
                        userId,
                        operationType,
                        finalLogModule,
                        item.getName());

                dto.setHistory(isHistory);
                dto.setPath(OperationLogAspect.getPath());
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
                dtoList.add(dto);
            });
            operationLogService.batchAdd(dtoList);
        }
    }

    public Long saveOperationHistoryLog(ApiDefinitionDTO apiDefinitionDTO, String userId, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                apiDefinitionDTO.getId(),
                userId,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                apiDefinitionDTO.getName());
        dto.setHistory(true);
        dto.setPath("/api/definition/operation-history/save");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
        operationLogService.add(dto);
        return dto.getId();
    }

    public Long recoverOperationHistoryLog(ApiDefinitionDTO apiDefinitionDTO, String userId, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                apiDefinitionDTO.getId(),
                userId,
                OperationLogType.RECOVER.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                apiDefinitionDTO.getName());
        dto.setHistory(true);
        dto.setPath("/api/definition/operation-history/recover");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
        operationLogService.add(dto);
        return dto.getId();
    }

    public LogDTO moveLog(String id) {
        ApiDefinitionDTO apiDefinitionDTO = getOriginalValue(id);
        Project project = projectMapper.selectByPrimaryKey(apiDefinitionDTO.getProjectId());
        LogDTO dto = new LogDTO(
                apiDefinitionDTO.getProjectId(),
                project.getOrganizationId(),
                apiDefinitionDTO.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                apiDefinitionDTO.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
        return dto;
    }


    public LogDTO exportExcelLog(ApiDefinitionBatchExportRequest request, String exportType, String userId, String orgId) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                orgId,
                request.getFileId(),
                userId,
                OperationLogType.EXPORT.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                "");
        dto.setHistory(true);
        dto.setPath("/api/definition/export/" + exportType);
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }
}
