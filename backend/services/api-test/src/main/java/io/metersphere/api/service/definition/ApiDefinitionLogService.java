package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiDefinitionLogService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ApiDefinitionService apiDefinitionService;

    @Resource
    private ProjectMapper projectMapper;

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
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        if(apiDefinition != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    request.getName());

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
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        if(apiDefinition != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());

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
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId());
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
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId());
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        apiDefinitions.forEach(item -> {
            LogDTO dto = new LogDTO(
                    item.getProjectId(),
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    item.getName());

            dto.setPath("/api/definition/batch-update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        return dtoList;
    }

    public LogDTO copyLog(ApiDefinitionCopyRequest request) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        if(apiDefinition != null){
            LogDTO dto = new LogDTO(
                    apiDefinition.getProjectId(),
                    null,
                    null,
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());

            dto.setPath("/api/definition/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    public List<LogDTO> batchMoveLog(ApiDefinitionBatchMoveRequest request) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId());
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        apiDefinitions.forEach(item -> {
            LogDTO dto = new LogDTO(
                    item.getProjectId(),
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.API_DEFINITION,
                    item.getName());

            dto.setPath("/api/definition/batch-move");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        return dtoList;
    }

    public LogDTO followLog(String id) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
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
}
