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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseLogService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;


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
}
