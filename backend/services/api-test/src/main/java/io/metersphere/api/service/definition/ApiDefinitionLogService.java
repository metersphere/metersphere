package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
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
                OperationLogModule.API_DEFINITION,
                request.getName());
        dto.setHistory(false);
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
        if (apiDefinition.getId() != null) {
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
        if (apiDefinition.getId() != null) {
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(false);
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
    public void batchDelLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, "/api/definition/batch-del", userId, OperationLogType.DELETE.name(), false);
    }

    /**
     * 批量更新接口日志
     *
     * @return
     */
    public void batchUpdateLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, "/api/definition/batch-update", userId, OperationLogType.UPDATE.name(), true);
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
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(false);
            dto.setPath("/api/definition/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinition));
            return dto;
        }
        return null;
    }

    public void batchMoveLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, "/api/definition/batch-move", userId, OperationLogType.UPDATE.name(), false);
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
    public LogDTO recoverLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if (apiDefinition.getId() != null) {
            LogDTO dto = new LogDTO(
                    request.getProjectId(),
                    null,
                    request.getId(),
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.API_DEFINITION,
                    apiDefinition.getName());
            dto.setHistory(false);
            dto.setPath("/api/definition/recover");
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
        saveBatchLog(projectId, ids, "/api/definition/batch-recover", userId, OperationLogType.RECOVER.name(), false);
    }


    /**
     * 删除回收站接口定义接口日志
     */
    public LogDTO trashDelLog(ApiDefinitionDeleteRequest request) {
        ApiDefinitionDTO apiDefinition = getOriginalValue(request.getId());
        if (apiDefinition.getId() != null) {
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
    public void batchTrashDelLog(List<String> ids, String userId, String projectId) {
        saveBatchLog(projectId, ids, "/api/definition/batch-trash-del", userId, OperationLogType.DELETE.name(), false);
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



    private void saveBatchLog(String projectId, List<String> ids, String path, String userId, String operationType, boolean isHistory) {
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                CommonBeanFactory.getBean(ApiDefinitionService.class).handleBlob(item.getId(), apiDefinitionDTO);
                BeanUtils.copyBean(apiDefinitionDTO, item);
                LogDTO dto = new LogDTO(
                        project.getId(),
                        project.getOrganizationId(),
                        item.getId(),
                        userId,
                        operationType,
                        OperationLogModule.API_DEFINITION,
                        item.getName());

                dto.setHistory(isHistory);
                dto.setPath(path);
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
                OperationLogModule.API_DEFINITION,
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
                OperationLogModule.API_DEFINITION,
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
                OperationLogModule.API_DEFINITION,
                apiDefinitionDTO.getName());
        dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
        return dto;
    }


}
