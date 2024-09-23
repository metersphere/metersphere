package io.metersphere.functional.service;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.provider.CaseReviewCaseProvider;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DeleteCaseReviewService {

    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewUserMapper caseReviewUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewFunctionalCaseArchiveMapper caseReviewFunctionalCaseArchiveMapper;
    @Resource
    private CaseReviewFollowerMapper caseReviewFollowerMapper;
    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private CaseReviewCaseProvider caseReviewCaseProvider;

    public void deleteCaseReviewResource(List<String> ids, String projectId) {
        // TODO 删除各种关联关系？ 1.关联用例(功能/接口/场景/ui/性能)？ 2.评审和评审人 3. 归档的用例 4. 关注人 5.评审历史 6. 操作记录
        // 1.刪除评审与功能用例关联关系
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdIn(ids);
        List<CaseReviewFunctionalCase> reviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        caseReviewFunctionalCaseMapper.deleteByExample(caseReviewFunctionalCaseExample);
        // 2. 删除评审和评审人
        CaseReviewUserExample caseReviewUserExample = new CaseReviewUserExample();
        caseReviewUserExample.createCriteria().andReviewIdIn(ids);
        caseReviewUserMapper.deleteByExample(caseReviewUserExample);
        // 3. 删除归档的用例
        CaseReviewFunctionalCaseArchiveExample archiveExample = new CaseReviewFunctionalCaseArchiveExample();
        archiveExample.createCriteria().andReviewIdIn(ids);
        caseReviewFunctionalCaseArchiveMapper.deleteByExample(archiveExample);
        // 5.删除评审历史
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andReviewIdIn(ids);
        caseReviewHistoryMapper.deleteByExample(caseReviewHistoryExample);
        // 4.删除关注人
        CaseReviewFollowerExample caseReviewFollowerExample = new CaseReviewFollowerExample();
        caseReviewFollowerExample.createCriteria().andReviewIdIn(ids);
        caseReviewFollowerMapper.deleteByExample(caseReviewFollowerExample);

        // TODO: 6.删除操作记录
        //删除评审
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andIdIn(ids).andProjectIdEqualTo(projectId);
        caseReviewMapper.deleteByExample(caseReviewExample);

        // 7. 批量刷新评审中所有用例的评审状态
        Map<String, List<CaseReviewFunctionalCase>> reviewFunctionalCaseMap = reviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        reviewFunctionalCaseMap.forEach((reviewId, reviewFunctionalCaseList) -> caseReviewCaseProvider.refreshReviewCaseStatus(reviewFunctionalCaseList));
    }
}
