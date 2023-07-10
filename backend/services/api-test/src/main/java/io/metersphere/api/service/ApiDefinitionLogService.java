package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.constants.HttpMethodConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiDefinitionLogService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    /**
     * 添加接口日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(ApiDefinitionDTO request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                request.getId(),
                request.getCreateUser(),
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
    public LogDTO updateLog(ApiDefinitionDTO request) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());

        LogDTO dto = new LogDTO(
                request.getProjectId(),
                request.getId(),
                request.getCreateUser(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION,
                request.getName());

        dto.setPath("/api/definition/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        if (apiDefinition == null) {
            return dto;
        }
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
        return dto;
    }

    /**
     * 删除接口日志
     *
     * @param request
     * @return
     */
    public LogDTO delLog(ApiDefinitionDTO request) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                request.getId(),
                request.getCreateUser(),
                OperationLogType.DELETE.name(),
                OperationLogModule.API_DEFINITION,
                request.getName());

        dto.setPath("/api/definition/delete");
        dto.setMethod(HttpMethodConstants.POST.name());
        if (apiDefinition == null) {
            return dto;
        }
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
        return dto;
    }

    /**
     * 批量删除接口日志
     *
     * @return
     */
    public List<LogDTO> batchDelLog(List<String> ids) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        apiDefinitions.forEach(item -> {
            LogDTO dto = new LogDTO(
                    item.getProjectId(),
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
        return dtoList;
    }

    /**
     * 批量更新接口日志
     * @return
     */
    public List<LogDTO> batchUpdateLog(List<String> ids) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        apiDefinitions.forEach(item -> {
            LogDTO dto = new LogDTO(
                    item.getProjectId(),
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
}
