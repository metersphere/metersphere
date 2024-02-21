package io.metersphere.functional.service;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewExample;
import io.metersphere.functional.domain.CaseReviewModuleExample;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.CaseReviewModuleMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CleanupCaseReviewResourceService implements CleanupProjectResourceService {
    @Resource
    private DeleteCaseReviewService deleteCaseReviewService;
    @Resource
    private CaseReviewModuleMapper caseReviewModuleMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关用例评审资源");
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andProjectIdEqualTo(projectId);
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
        List<String> ids = caseReviews.stream().map(CaseReview::getId).toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            deleteCaseReviewService.deleteCaseReviewResource(ids, projectId);
        }
        //删除模块
        CaseReviewModuleExample caseReviewModuleExample = new CaseReviewModuleExample();
        caseReviewModuleExample.createCriteria().andProjectIdEqualTo(projectId);
        caseReviewModuleMapper.deleteByExample(caseReviewModuleExample);
    }

}
