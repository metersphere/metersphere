package io.metersphere.functional.provider;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.service.ReviewSendNoticeService;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.notice.constants.NoticeConstants;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CaseReviewCaseProvider implements BaseCaseProvider {

    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private ReviewSendNoticeService reviewSendNoticeService;
    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;
    @Resource
    private ExtCaseReviewHistoryMapper extCaseReviewHistoryMapper;

    @Async
    @Override
    public void updateCaseReview(Map<String, Object> paramMap) {
        String event = paramMap.get(CaseEvent.Param.EVENT_NAME).toString();
        switch (event) {
            case CaseEvent.Event.ASSOCIATE -> updateCaseReviewByAssociate(paramMap);
            case CaseEvent.Event.DISASSOCIATE -> updateCaseReviewByDisAssociate(paramMap);
            case CaseEvent.Event.BATCH_DISASSOCIATE -> updateCaseReviewByBatchDisassociate(paramMap);
            case CaseEvent.Event.DELETE_FUNCTIONAL_CASE -> updateCaseReviewByDeleteFunctionalCase(paramMap);
            case CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE -> updateCaseReviewByDeleteTrashFunctionalCase(paramMap);
            case CaseEvent.Event.RECOVER_FUNCTIONAL_CASE -> updateCaseReviewByRecoverFunctionalCase(paramMap);
            case CaseEvent.Event.REVIEW_FUNCTIONAL_CASE -> updateCaseReviewByReviewFunctionalCase(paramMap);
            case CaseEvent.Event.BATCH_UPDATE_REVIEWER -> updateCaseReviewByBatchUpdateReviewer(paramMap);
            default -> LogUtils.info("CaseProvider: " + event);
        }

    }


    /**
     * 批量修改评审人 重新计算通过率和用例数
     *
     * @param paramMap paramMap
     */
    private void updateCaseReviewByBatchUpdateReviewer(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            int caseCount = Integer.parseInt(paramMap.get(CaseEvent.Param.CASE_COUNT).toString());
            int passCount = Integer.parseInt(paramMap.get(CaseEvent.Param.PASS_COUNT).toString());
            int unCompletedCaseCount = Integer.parseInt(paramMap.get(CaseEvent.Param.UN_COMPLETED_COUNT).toString());
            updateCaseReview(reviewId, caseCount, passCount, unCompletedCaseCount, paramMap.get(CaseEvent.Param.USER_ID).toString());
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.BATCH_UPDATE_REVIEWER + "事件更新失败", e.getMessage());
        }

    }

    /**
     * 1.关联用例（单独/批量）重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByAssociate(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            //获取关联前的caseIds
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListIncludes(reviewId, caseIdList, false);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            int caseCount = Integer.parseInt(paramMap.get(CaseEvent.Param.CASE_COUNT).toString()) + caseReviewFunctionalCases.size();
            int passNumber = passList.size();
            List<CaseReviewFunctionalCase> unCompletedCaseList = getUnCompletedCaseList(caseReviewFunctionalCases, new ArrayList<>());
            updateCaseReview(reviewId, caseCount, passNumber, unCompletedCaseList.size() + Integer.parseInt(paramMap.get(CaseEvent.Param.CASE_COUNT).toString()), paramMap.get(CaseEvent.Param.USER_ID).toString());
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.ASSOCIATE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 2.
     * 1.单独取消关联重新计算用例评审的通过率和用例数
     * 2.删除用例和用例评审人的关系
     */
    private void updateCaseReviewByDisAssociate(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            if (caseReview == null) {
                return;
            }
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            //获取与选中case无关的其他case
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(List.of(reviewId), caseIdList, false);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            int caseCount = caseReviewFunctionalCases.size();
            int passNumber = passList.size();
            List<CaseReviewFunctionalCase> unCompletedCaseList = getUnCompletedCaseList(caseReviewFunctionalCases, new ArrayList<>());
            updateCaseReview(reviewId, caseReview.getCaseCount() - caseCount, passNumber, unCompletedCaseList.size(), paramMap.get(CaseEvent.Param.USER_ID).toString());
            //删除用例和用例评审人的关系
            deleteCaseReviewFunctionalCaseUser(paramMap);
            //将评审历史状态置为true
            extCaseReviewHistoryMapper.updateDelete(caseIdList, reviewId, true);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DISASSOCIATE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 3.
     * 1.批量取消关联重新计算用例评审的通过率和用例数
     * 2.删除用例和用例评审人的关系
     */
    private void updateCaseReviewByBatchDisassociate(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            CaseReview caseReviewOld = caseReviewMapper.selectByPrimaryKey(reviewId);
            Integer oldCaseCount = caseReviewOld.getCaseCount();
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (CollectionUtils.isEmpty(caseIdList)) {
                return;
            }
            int passNumber = Integer.parseInt(paramMap.get(CaseEvent.Param.PASS_COUNT).toString());
            int unCompletedCount = Integer.parseInt(paramMap.get(CaseEvent.Param.UN_COMPLETED_COUNT).toString());
            updateCaseReview(reviewId, oldCaseCount - caseIdList.size(), passNumber, unCompletedCount, paramMap.get(CaseEvent.Param.USER_ID).toString());
            //删除用例和用例评审人的关系
            deleteCaseReviewFunctionalCaseUser(paramMap);
            //将评审历史状态置为true
            extCaseReviewHistoryMapper.updateDelete(caseIdList, reviewId, true);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.BATCH_DISASSOCIATE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 4.功能用例的删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            startUpdateCaseReview(paramMap, caseIdList);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DELETE_FUNCTIONAL_CASE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 5.功能用例的回收站删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteTrashFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (startUpdateCaseReview(paramMap, caseIdList)) return;
            CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
            caseReviewFunctionalCaseUserExample.createCriteria().andCaseIdIn(caseIdList);
            caseReviewFunctionalCaseUserMapper.deleteByExample(caseReviewFunctionalCaseUserExample);
            //从回收站删除也删除关联关系
            CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
            caseReviewFunctionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
            caseReviewFunctionalCaseMapper.deleteByExample(caseReviewFunctionalCaseExample);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 6.功能用例的回收站恢复/批量恢复重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByRecoverFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            CaseReviewFunctionalCaseExample functionalCaseExample = new CaseReviewFunctionalCaseExample();
            functionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
            List<CaseReviewFunctionalCase> recoverCases = caseReviewFunctionalCaseMapper.selectByExample(functionalCaseExample);
            if (CollectionUtils.isEmpty(recoverCases)) {
                return;
            }
            List<String> reviewIds = recoverCases.stream().map(CaseReviewFunctionalCase::getReviewId).distinct().toList();
            Map<String, List<CaseReviewFunctionalCase>> recoverCaseMap = recoverCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(reviewIds, caseIdList, false);
            Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
            CaseReviewExample caseReviewExample = new CaseReviewExample();
            caseReviewExample.createCriteria().andIdIn(reviewIds);
            List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
            Map<String, CaseReview> reviewMap = caseReviews.stream().collect(Collectors.toMap(CaseReview::getId, t -> t));
            reviewMap.forEach((reviewId, caseReview) -> {
                List<CaseReviewFunctionalCase> recoverCaseList = recoverCaseMap.get(reviewId);
                List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList = reviewIdMap.get(reviewId);
                if (CollectionUtils.isNotEmpty(caseReviewFunctionalCaseList)) {
                    caseReviewFunctionalCaseList.addAll(recoverCaseList);
                } else {
                    caseReviewFunctionalCaseList = recoverCaseList;
                }
                List<String> ids = caseReviewFunctionalCaseList.stream().map(CaseReviewFunctionalCase::getId).toList();
                List<CaseReviewFunctionalCase> unCompleteList = getUnCompletedCaseList(caseReviewFunctionalCaseList, ids);
                List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
                updateCaseReview(reviewId, caseReview.getCaseCount() + caseReviewFunctionalCaseList.size(), passList.size(), unCompleteList.size(), paramMap.get(CaseEvent.Param.USER_ID).toString());
            });
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.RECOVER_FUNCTIONAL_CASE + "事件更新失败", e.getMessage());
        }

    }


    /**
     * 7.评审用例/批量评审用例重新计算用例评审的通过率和用例评审状态和发送通知
     */
    private void updateCaseReviewByReviewFunctionalCase(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(List.of(reviewId), caseIdList, false);
            List<CaseReviewFunctionalCase> unCompletedCaseList = getUnCompletedCaseList(caseReviewFunctionalCases, new ArrayList<>());
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();

            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            boolean completed = false;

            String status = paramMap.get(CaseEvent.Param.STATUS).toString();
            List<String> completedStatusList = new ArrayList<>();
            completedStatusList.add(FunctionalCaseReviewStatus.PASS.toString());
            completedStatusList.add(FunctionalCaseReviewStatus.UN_PASS.toString());
            if (!completedStatusList.contains(status) || CollectionUtils.isNotEmpty(unCompletedCaseList)) {
                caseReview.setStatus(CaseReviewStatus.UNDERWAY.toString());
            } else {
                completed = true;
                caseReview.setStatus(CaseReviewStatus.COMPLETED.toString());
            }
            int passNumber = passList.size();

            int pass = Integer.parseInt(paramMap.get(CaseEvent.Param.PASS_COUNT).toString());
            passNumber += pass;
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

            if (completed) {
                reviewSendNoticeService.sendNotice(new ArrayList<>(), paramMap.get(CaseEvent.Param.USER_ID).toString(), reviewId, NoticeConstants.TaskType.CASE_REVIEW_TASK, NoticeConstants.Event.REVIEW_COMPLETED);
            }
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.REVIEW_FUNCTIONAL_CASE + "事件更新失败", e.getMessage());
        }
    }

    /**
     * 准备更新caseReview
     */
    private boolean startUpdateCaseReview(Map<String, Object> paramMap, List<String> caseIdList) {
        CaseReviewFunctionalCaseExample functionalCaseExample = new CaseReviewFunctionalCaseExample();
        functionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
        List<CaseReviewFunctionalCase> caseReviewFunctionalReviews = caseReviewFunctionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(caseReviewFunctionalReviews)) {
            return true;
        }
        Map<String, List<CaseReviewFunctionalCase>> caseReviewMap = caseReviewFunctionalReviews.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        List<String> reviewIds = caseReviewFunctionalReviews.stream().map(CaseReviewFunctionalCase::getReviewId).distinct().toList();
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(reviewIds, caseIdList, false);
        Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        CaseReviewExample caseReviewExample = new CaseReviewExample();
        caseReviewExample.createCriteria().andIdIn(reviewIds);
        List<CaseReview> caseReviews = caseReviewMapper.selectByExample(caseReviewExample);
        Map<String, CaseReview> reviewMap = caseReviews.stream().collect(Collectors.toMap(CaseReview::getId, t -> t));
        reviewIdMap.forEach((reviewId, caseReviewFunctionalCaseList) -> {
            List<String> ids = caseReviewFunctionalCaseList.stream().map(CaseReviewFunctionalCase::getId).toList();
            CaseReview caseReview = reviewMap.get(reviewId);
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases1 = caseReviewMap.get(reviewId);
            List<CaseReviewFunctionalCase> unCompleteList = getUnCompletedCaseList(caseReviewFunctionalCaseList, ids);
            List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
            updateCaseReview(reviewId, caseReview.getCaseCount() - caseReviewFunctionalCases1.size(), passList.size(), unCompleteList.size(), paramMap.get(CaseEvent.Param.USER_ID).toString());
        });
        return false;
    }


    /**
     * 获取未评审完成的用例
     */
    private static List<CaseReviewFunctionalCase> getUnCompletedCaseList(List<CaseReviewFunctionalCase> caseReviewFunctionalCases, List<String> ids) {
        List<String> statusList = new ArrayList<>();
        statusList.add(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        statusList.add(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        statusList.add(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        if (CollectionUtils.isNotEmpty(ids)) {
            return caseReviewFunctionalCases.stream().filter(t -> statusList.contains(t.getStatus()) && !ids.contains(t.getId())).toList();
        } else {
            return caseReviewFunctionalCases.stream().filter(t -> statusList.contains(t.getStatus())).toList();
        }
    }

    /**
     * 重新计算用例评审的通过率和用例数以及评审状态
     */
    private void updateCaseReview(String reviewId, int caseCount, int passNumber, int unCompleteCount, String userId) {
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
        boolean completed = false;
        if (unCompleteCount > 0) {
            caseReview.setStatus(CaseReviewStatus.UNDERWAY.toString());
        } else {
            completed = true;
            caseReview.setStatus(CaseReviewStatus.COMPLETED.toString());
        }
        caseReviewMapper.updateByPrimaryKeySelective(caseReview);
        if (completed) {
            reviewSendNoticeService.sendNotice(new ArrayList<>(), userId, reviewId, NoticeConstants.TaskType.CASE_REVIEW_TASK, NoticeConstants.Event.REVIEW_COMPLETED);
        }
    }

    /**
     * 删除用例和用例评审人的关系
     */
    private void deleteCaseReviewFunctionalCaseUser(Map<String, Object> paramMap) {
        Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
        List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
        String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
        CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
        caseReviewFunctionalCaseUserExample.createCriteria().andCaseIdIn(caseIdList).andReviewIdEqualTo(reviewId);
        caseReviewFunctionalCaseUserMapper.deleteByExample(caseReviewFunctionalCaseUserExample);
    }

}
