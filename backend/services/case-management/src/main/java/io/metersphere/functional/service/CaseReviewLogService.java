package io.metersphere.functional.service;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.request.CaseReviewRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewLogService {

    @Resource
    private CaseReviewMapper caseReviewMapper;

    /**
     * 新增用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO addCaseReviewLog(CaseReviewRequest requests) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.CASE_REVIEW,
                requests.getName());

        dto.setPath("/case/review/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }

    /**
     * 更新用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO updateCaseReviewLog(CaseReviewRequest requests) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(requests.getId());
        if (caseReview ==null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                caseReview.getCreateUser(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.CASE_REVIEW,
                caseReview.getName());

        dto.setPath("/case/review/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(caseReview));
        return dto;
    }
}
