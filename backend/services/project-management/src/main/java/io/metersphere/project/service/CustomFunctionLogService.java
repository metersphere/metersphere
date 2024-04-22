package io.metersphere.project.service;

import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
import io.metersphere.project.mapper.CustomFunctionMapper;
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
 * @date: 2024/1/9 19:38
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionLogService {
    @Resource
    private CustomFunctionMapper customFunctionMapper;

    @Resource
    private CustomFunctionService customFunctionService;

    /**
     * 添加日志
     *
     * @param request
     * @return
     */
    public LogDTO addLog(CustomFunctionRequest request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_COMMON_SCRIPT,
                request.getName());
        dto.setHistory(true);
        dto.setPath("/project/custom/func/add");
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
    public LogDTO updateLog(CustomFunctionUpdateRequest request) {
        CustomFunctionDTO customFunctionDTO= getOriginalValue(request.getId());
        if(customFunctionDTO.getId() != null){
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_COMMON_SCRIPT,
                    request.getName());
            dto.setHistory(true);
            dto.setPath("/project/custom/func/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(customFunctionDTO));
            return dto;
        }
        return null;
    }

    public LogDTO updateStatusLog(CustomFunctionUpdateRequest request) {
        CustomFunctionDTO customFunctionDTO= getOriginalValue(request.getId());
        if(customFunctionDTO.getId() != null){
            LogDTO dto = new LogDTO(
                    customFunctionDTO.getProjectId(),
                    null,
                    customFunctionDTO.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_COMMON_SCRIPT,
                    customFunctionDTO.getName());
            dto.setHistory(true);
            dto.setPath("/project/custom/func/status" );
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(customFunctionDTO));
            return dto;
        }
        return null;
    }

    /**
     * 删除日志
     *
     * @param id
     * @return
     */
    public LogDTO delLog(String id) {
        CustomFunctionDTO customFunctionDTO = getOriginalValue(id);
        if(customFunctionDTO.getId() != null){
            LogDTO dto = new LogDTO(
                    customFunctionDTO.getProjectId(),
                    null,
                    customFunctionDTO.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_COMMON_SCRIPT,
                    customFunctionDTO.getName());
            dto.setHistory(true);
            dto.setPath("/project/custom/func/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(customFunctionDTO));
            return dto;
        }

        return null;
    }



    private CustomFunctionDTO getOriginalValue(String id){
        CustomFunctionDTO customFunctionDTO = new CustomFunctionDTO();
        CustomFunction customFunction = customFunctionMapper.selectByPrimaryKey(id);
        if(null != customFunction){
            customFunctionService.handleCustomFunctionBlob(id, customFunctionDTO);
            BeanUtils.copyBean(customFunctionDTO, customFunction);
        }
        return customFunctionDTO;
    }
}
