package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ApiDefinitionMockService apiDefinitionMockService;

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
            apiDefinitionMockService.handleMockConfig(id, apiDefinitionMockDTO);
            BeanUtils.copyBean(apiDefinitionMockDTO, apiDefinitionMock);
        }
        return apiDefinitionMockDTO;
    }

}
