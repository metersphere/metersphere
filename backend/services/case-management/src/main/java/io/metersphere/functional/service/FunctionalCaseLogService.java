package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCaseFollower;
import io.metersphere.functional.domain.FunctionalCaseFollowerExample;
import io.metersphere.functional.mapper.FunctionalCaseFollowerMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.request.FunctionalCaseEditRequest;
import io.metersphere.functional.request.FunctionalCaseFollowerRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    private FunctionalCaseFollowerMapper functionalCaseFollowerMapper;


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
     * 关注取消关注日志
     *
     * @param request
     * @return
     */
    public LogDTO editFollower(FunctionalCaseFollowerRequest request) {
        FunctionalCaseFollowerExample example = new FunctionalCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(request.getFunctionalCaseId()).andUserIdEqualTo(request.getUserId());
        List<FunctionalCaseFollower> caseFollowers = functionalCaseFollowerMapper.selectByExample(example);
        String content = "";
        if (CollectionUtils.isNotEmpty(caseFollowers)) {
            content = Translator.get("un_follow_functional_case");
        } else {
            content = Translator.get("follow_functional_case");
        }
        LogDTO dto = new LogDTO(
                null,
                null,
                request.getFunctionalCaseId(),
                request.getUserId(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                content);
        dto.setPath("/functional/case/follower/" + request.getFunctionalCaseId());
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }
}
