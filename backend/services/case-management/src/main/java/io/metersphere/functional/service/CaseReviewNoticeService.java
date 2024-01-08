package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.request.CaseReviewRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CaseReviewNoticeService {

    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;

    @Resource
    private CaseReviewMapper caseReviewMapper;

    public CaseReview getMainCaseReview(CaseReviewRequest request){
        CaseReview caseReview = null;
        if (StringUtils.isNotBlank(request.getId())) {
            caseReview = caseReviewMapper.selectByPrimaryKey(request.getId());
        }
        if (caseReview == null) {
            caseReview = new CaseReview();
        }
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        if (StringUtils.isBlank(request.getId())) {
            caseReview.setProjectId(request.getProjectId());
            caseReview.setStatus(CaseReviewStatus.PREPARED.toString());
            caseReview.setReviewPassRule(request.getReviewPassRule());
            caseReview.setPos(getNextPos(request.getProjectId()));
            caseReview.setCreateTime(System.currentTimeMillis());
            caseReview.setCreateUser(null);
        }
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(request.getTags());
        }
        caseReview.setStartTime(request.getStartTime());
        caseReview.setEndTime(request.getEndTime());
        caseReview.setUpdateTime(System.currentTimeMillis());
        caseReview.setUpdateUser(null);
        return caseReview;
    }

    public CaseReview getMainCaseReview(String reviewId){
        return caseReviewMapper.selectByPrimaryKey(reviewId);
    }

    public Long getNextPos(String projectId) {
        Long pos = extCaseReviewMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + 5000;
    }
}
