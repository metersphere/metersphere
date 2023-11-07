package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseLogService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private OperationLogService operationLogService;


    //TODO 日志(需要修改)

    /**
     * 新增用例 日志
     *
     * @param requests
     * @param files
     * @return
     */
    public LogDTO addFunctionalCaseLog(FunctionalCaseAddRequest requests, List<MultipartFile> files) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                requests.getName());

        dto.setPath("/functional/case/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }


    /**
     * 更新用例 日志
     *
     * @param requests
     * @param files
     * @return
     */
    public LogDTO updateFunctionalCaseLog(FunctionalCaseEditRequest requests, List<MultipartFile> files) {
        //TODO 获取原值
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                requests.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                requests.getName());

        dto.setPath("/functional/case/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(requests));
        return dto;
    }


    /**
     * 删除用例 日志
     *
     * @param request
     * @return
     */
    public LogDTO deleteFunctionalCaseLog(FunctionalCaseDeleteRequest request) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getId());
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/delete");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }

    public void batchDelLog(List<FunctionalCase> functionalCases, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        functionalCases.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    item.getName());

            dto.setPath("/functional/case/module/delete/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }

    /**
     * 恢复项目
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    "",
                    id,
                    functionalCase.getCreateUser(),
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    functionalCase.getName());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }

    /**
     * 彻底删除
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteTrashCaseLog(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase != null) {
            LogDTO dto = new LogDTO(
                    functionalCase.getProjectId(),
                    null,
                    functionalCase.getId(),
                    functionalCase.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    functionalCase.getName());

            dto.setPath("/functional/case/trash/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            return dto;
        }
        return null;
    }
}
