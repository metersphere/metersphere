package io.metersphere.functional.listener;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewFunctionalCase;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseExample;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseUserExample;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseUserMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.sdk.listener.Event;
import io.metersphere.sdk.listener.EventListener;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CaseEventListener implements EventListener<Event> {
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;

    @Async
    @Override
    public void onEvent(Event event) {
        LogUtils.info("CaseEventListener: " + event.module() + "：" + event.message());
        //批量修改用例评审人
        //重新提审(批量)/自动重新提审（编辑）
        //批量修改结果
        String message = event.message();
        switch (message) {
            case CaseEvent.Event.ASSOCIATE -> {
                updateCaseReviewByAssociate(event);
            }
            case CaseEvent.Event.DISASSOCIATE -> {
                updateCaseReviewByDisAssociate(event);
            }
            case CaseEvent.Event.BATCH_DISASSOCIATE -> {
                updateCaseReviewByBatchDisassociate(event);
            }
            case CaseEvent.Event.DELETE_FUNCTIONAL_CASE -> {
                updateCaseReviewByDeleteFunctionalCase(event);
            }
            case CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE -> {
                updateCaseReviewByDeleteTrashFunctionalCase(event);
            }
            case CaseEvent.Event.RECOVER_FUNCTIONAL_CASE -> {
                updateCaseReviewByRecoverFunctionalCase(event);
            }
            default -> LogUtils.info("CaseEventListener: " + event.module() + "：" + event.message());
        }

    }

    /**
     * 功能用例的回收站恢复/批量恢复重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByRecoverFunctionalCase(Event event) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = getCaseReviewFunctionalCases(event);
        Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        reviewIdMap.forEach((reviewId, caseReviewFunctionalCaseList) -> {
            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            updateCaseReview(reviewId, caseReview.getCaseCount() + caseReviewFunctionalCaseList.size(), passList.size());
        });
    }

    /**
     * 功能用例的回收站删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteTrashFunctionalCase(Event event) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = getCaseReviewFunctionalCases(event);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCases)) {
            return;
        }
        Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        reviewIdMap.forEach((reviewId, caseReviewFunctionalCaseList) -> {
            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            updateCaseReview(reviewId, caseReview.getCaseCount() - caseReviewFunctionalCaseList.size(), passList.size());
        });

    }

    /**
     * 功能用例的删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteFunctionalCase(Event event) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = getCaseReviewFunctionalCases(event);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCases)) {
            return;
        }
        Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        reviewIdMap.forEach((reviewId, caseReviewFunctionalCaseList) -> {
            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            updateCaseReview(reviewId, caseReview.getCaseCount() - caseReviewFunctionalCaseList.size(), passList.size());
            List<String> caseIdList = caseReviewFunctionalCaseList.stream().map(CaseReviewFunctionalCase::getId).toList();
            deleteReviewFunctionalCaseUser(caseIdList, reviewId);
        });

    }

    private List<CaseReviewFunctionalCase> getCaseReviewFunctionalCases(Event event) {
        Map<String, Object> paramMap = event.paramMap();
        Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
        List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
        CaseReviewFunctionalCaseExample functionalCaseExample = new CaseReviewFunctionalCaseExample();
        functionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
        return caseReviewFunctionalCaseMapper.selectByExample(functionalCaseExample);
    }

    /**
     * 1.批量取消关联重新计算用例评审的通过率和用例数
     * 2.删除用例和用例评审人的关系
     */
    private void updateCaseReviewByBatchDisassociate(Event event) {
        Map<String, Object> paramMap = event.paramMap();
        String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        Integer oldCaseCount = caseReview.getCaseCount();
        Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
        List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
        int passNumber = Integer.parseInt(paramMap.get(CaseEvent.Param.PASS_COUNT).toString());
        updateCaseReview(reviewId, oldCaseCount - caseIdList.size(), passNumber);
        //删除用例和用例评审人的关系
        deleteCaseReviewFunctionalCaseUser(event);
    }

    /**
     * 1.单独取消关联重新计算用例评审的通过率和用例数
     * 2.删除用例和用例评审人的关系
     */
    private void updateCaseReviewByDisAssociate(Event event) {
        updateCaseReviewByAssociate(event);
        //删除用例和用例评审人的关系
        deleteCaseReviewFunctionalCaseUser(event);
    }

    /**
     * 删除用例和用例评审人的关系
     */
    private void deleteCaseReviewFunctionalCaseUser(Event event) {
        Map<String, Object> paramMap = event.paramMap();
        Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
        List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
        String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
        deleteReviewFunctionalCaseUser(caseIdList, reviewId);
    }

    private void deleteReviewFunctionalCaseUser(List<String> caseIdList, String reviewId) {
        CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
        caseReviewFunctionalCaseUserExample.createCriteria().andCaseIdIn(caseIdList).andReviewIdEqualTo(reviewId);
        caseReviewFunctionalCaseUserMapper.deleteByExample(caseReviewFunctionalCaseUserExample);
    }

    /**
     * 关联用例（单独/批量）重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByAssociate(Event event) {
        Map<String, Object> paramMap = event.paramMap();
        String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
        int caseCount = Integer.parseInt(paramMap.get(CaseEvent.Param.CASE_COUNT).toString());
        int passNumber = Integer.parseInt(paramMap.get(CaseEvent.Param.PASS_COUNT).toString());
        updateCaseReview(reviewId, caseCount, passNumber);
    }

    /**
     * 重新计算用例评审的通过率和用例数
     */
    private void updateCaseReview(String reviewId, int caseCount, int passNumber) {
        CaseReview caseReview = new CaseReview();
        caseReview.setId(reviewId);
        //更新用例数量
        caseReview.setCaseCount(caseCount);
        //通过率
        BigDecimal passCount = BigDecimal.valueOf(passNumber);
        BigDecimal totalCount = BigDecimal.valueOf(caseReview.getCaseCount());
        if (totalCount.compareTo(BigDecimal.ZERO) == 0) {
            caseReview.setPassRate(BigDecimal.ZERO);
        } else {
            BigDecimal passRate = passCount.divide(totalCount, 2, RoundingMode.HALF_UP);
            caseReview.setPassRate(passRate);
        }
        caseReviewMapper.updateByPrimaryKeySelective(caseReview);
    }
}
