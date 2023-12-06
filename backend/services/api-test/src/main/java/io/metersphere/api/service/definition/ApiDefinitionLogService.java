package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionLogService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ApiDefinitionService apiDefinitionService;

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

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
                OperationLogModule.API_DEFINITION,
                request.getName());
        dto.setHistory(true);
        dto.setPath("/api/definition/add");
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
        if(apiDefinition.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    request.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    /**
     * 删除接口日志
     *
     * @param request
     * @return
     */
    public LogDTO delLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if(apiDefinition.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }

        return null;
    }

    /**
     * 批量删除接口日志
     *
     * @return
     */
    public List<LogDTO> batchDelLog(ApiDefinitionBatchRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids).andDeletedEqualTo(false);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                LogDTO dto = new LogDTO(
                        item.getProjectId(),
                        "",
                        item.getId(),
                        item.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.API_DEFINITION,
                        item.getName());
                dto.setHistory(true);
                dto.setPath("/api/definition/batch-delete");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                dtoList.add(dto);
            });
        }

        return dtoList;
    }

    /**
     * 批量更新接口日志
     *
     * @return
     */
    public List<LogDTO> batchUpdateLog(ApiDefinitionBatchUpdateRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                LogDTO dto = new LogDTO(
                        item.getProjectId(),
                        "",
                        item.getId(),
                        item.getCreateUser(),
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.API_DEFINITION,
                        item.getName());
                dto.setHistory(true);
                dto.setPath("/api/definition/batch-update");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public LogDTO copyLog(ApiDefinitionCopyRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if(apiDefinition.getId() != null){
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    null,
                    null,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    public List<LogDTO> batchMoveLog(ApiDefinitionBatchMoveRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                LogDTO dto = new LogDTO(
                    item.getProjectId(),
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    item.getName());
                dto.setHistory(true);
                dto.setPath("/api/definition/batch-move");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public LogDTO followLog(String id) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(id);
        if(apiDefinition.getId() != null){
            Project project = projectMapper.selectByPrimaryKey(apiDefinition.getProjectId());
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    Translator.get("follow") + apiDefinition.getName());

            dto.setPath("/api/definition/follow/" + id);
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
    public LogDTO restoreLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if(apiDefinition.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/restore");
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
    public List<LogDTO> batchRestoreLog(ApiDefinitionBatchRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids).andDeletedEqualTo(false);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                LogDTO dto = new LogDTO(
                        item.getProjectId(),
                        "",
                        item.getId(),
                        item.getCreateUser(),
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.API_DEFINITION,
                        item.getName());
                dto.setHistory(true);
                dto.setPath("/api/definition/batch-restore");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                dtoList.add(dto);
            });
        }

        return dtoList;
    }


    /**
     * 删除回收站接口定义接口日志
     */
    public LogDTO trashDelLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if(apiDefinition.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());

            dto.setPath("/api/definition/trash-del");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }

        return null;
    }

    /**
     * 删除回收站接口定义接口日志
     */
    public List<LogDTO> batchTrashDelLog(ApiDefinitionBatchRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids).andDeletedEqualTo(true);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                LogDTO dto = new LogDTO(
                        item.getProjectId(),
                        "",
                        item.getId(),
                        item.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.API_DEFINITION,
                        item.getName());

                dto.setPath("/api/definition/batch-trash-del");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(item));
                dtoList.add(dto);
            });
        }

        return dtoList;
    }

    private ApiDefinitionDTO getOriginalValue(String id){
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        if(null != apiDefinition){
            // 2. 使用Optional避免空指针异常
            apiDefinitionService.handleBlob(id, apiDefinitionDTO);
            BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        }
        return apiDefinitionDTO;
    }



}
