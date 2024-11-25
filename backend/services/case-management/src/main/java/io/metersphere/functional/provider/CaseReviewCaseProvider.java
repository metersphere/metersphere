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
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseReviewCaseProvider implements BaseCaseProvider {

    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
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

    public static final String UN_REVIEWED_COUNT = "unReviewedCount";

    public static final String UNDER_REVIEWED_COUNT = "underReviewedCount";

    public static final String PASS_COUNT = "passCount";

    public static final String UN_PASS_COUNT = "unPassCount";

    public static final String RE_REVIEWED_COUNT = "reReviewedCount";


    @Async
    @Override
    public void updateCaseReview(Map<String, Object> paramMap) {
        String event = paramMap.get(CaseEvent.Param.EVENT_NAME).toString();
        switch (event) {
            case CaseEvent.Event.ASSOCIATE -> updateCaseReviewByAssociate(paramMap);
            case CaseEvent.Event.DISASSOCIATE -> updateCaseReviewByDisAssociate(paramMap);
            case CaseEvent.Event.DELETE_FUNCTIONAL_CASE -> updateCaseReviewByDeleteFunctionalCase(paramMap);
            case CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE -> updateCaseReviewByDeleteTrashFunctionalCase(paramMap);
            case CaseEvent.Event.RECOVER_FUNCTIONAL_CASE -> updateCaseReviewByRecoverFunctionalCase(paramMap);
            case CaseEvent.Event.REVIEW_FUNCTIONAL_CASE -> updateCaseReviewByReviewFunctionalCase(paramMap);
            default -> LogUtils.info("CaseProvider: " + event);
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
            List<CaseReviewFunctionalCase> beforeCaseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(List.of(reviewId), caseIdList, false);
            int caseCount = caseIdList.size() + beforeCaseReviewFunctionalCases.size();
            Map<String, Integer> mapCount = getCaseCountMap(beforeCaseReviewFunctionalCases);
            //新关联的都是未评审的，这里加上
            Integer i = mapCount.get(UN_REVIEWED_COUNT);
            mapCount.put(UN_REVIEWED_COUNT, i + caseIdList.size());
            updateCaseReview(reviewId, caseCount, mapCount, paramMap.get(CaseEvent.Param.USER_ID).toString());
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.ASSOCIATE + "事件更新失败：{}", e.getMessage());
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
            int caseCount = caseReviewFunctionalCases.size();
            Map<String, Integer> caseCountMap = getCaseCountMap(caseReviewFunctionalCases);
            updateCaseReview(reviewId, caseCount, caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());
            //删除用例和用例评审人的关系
            deleteCaseReviewFunctionalCaseUser(paramMap);
            //将评审历史状态置为true
            extCaseReviewHistoryMapper.updateDelete(caseIdList, reviewId, true);
            //检查更新用例的评审状态。如果用例没有任何评审关联，就置为未评审, 否则置为更新建时间最晚的那个
            updateCaseStatus(caseIdList);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DISASSOCIATE + "事件更新失败：{}", e.getMessage());
        }
    }

    @Async
    public void refreshReviewCaseStatus(List<CaseReviewFunctionalCase> reviewCases) {
        if (CollectionUtils.isNotEmpty(reviewCases)) {
            List<String> caseIds = reviewCases.stream().map(CaseReviewFunctionalCase::getCaseId).collect(Collectors.toList());
            updateCaseStatus(caseIds);
        }
    }

    private void updateCaseStatus(List<String> caseIdList) {
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
        caseReviewFunctionalCaseExample.setOrderByClause("update_time DESC");
        List<CaseReviewFunctionalCase> otherReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper mapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        if (CollectionUtils.isEmpty(otherReviewFunctionalCases)) {
            setUnReviewCase(caseIdList, mapper);
        } else {
            //如果不为空，那就把不为空的设置为上一次的结果，再取caseIdList的id,与map的key的差集，则为要置为未评审的
            Map<String, List<CaseReviewFunctionalCase>> collect = otherReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getCaseId));
            List<String>hasMoreReviewCaseIds = new ArrayList<>(collect.keySet());
            caseIdList.removeAll(hasMoreReviewCaseIds);
            if (CollectionUtils.isNotEmpty(caseIdList)) {
                setUnReviewCase(caseIdList, mapper);
            }
            collect.forEach((caseId, caseList) -> {
                FunctionalCase functionalCase = new FunctionalCase();
                functionalCase.setId(caseId);
                functionalCase.setReviewStatus(caseList.getFirst().getStatus());
                mapper.updateByPrimaryKeySelective(functionalCase);
            });
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private static void setUnReviewCase(List<String> caseIdList, FunctionalCaseMapper mapper) {
        for (String id : caseIdList) {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setId(id);
            functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
            mapper.updateByPrimaryKeySelective(functionalCase);
        }
    }

    /**
     * 3.功能用例的删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (CollectionUtils.isEmpty(caseIdList)) {
                return;
            }
            startUpdateCaseReview(paramMap, caseIdList);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DELETE_FUNCTIONAL_CASE + "事件更新失败：{}", e.getMessage());
        }
    }

    /**
     * 4.功能用例的回收站删除/批量删除重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByDeleteTrashFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            //要被删除的caseIds
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (CollectionUtils.isEmpty(caseIdList)) {
                return;
            }
            if (startUpdateCaseReview(paramMap, caseIdList)) return;
            //删除关联关系
            CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
            caseReviewFunctionalCaseUserExample.createCriteria().andCaseIdIn(caseIdList);
            caseReviewFunctionalCaseUserMapper.deleteByExample(caseReviewFunctionalCaseUserExample);
            //从回收站删除也删除关联关系
            CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
            caseReviewFunctionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
            caseReviewFunctionalCaseMapper.deleteByExample(caseReviewFunctionalCaseExample);
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE + "事件更新失败：{}", e.getMessage());
        }
    }

    /**
     * 5.功能用例的回收站恢复/批量恢复重新计算用例评审的通过率和用例数
     */
    private void updateCaseReviewByRecoverFunctionalCase(Map<String, Object> paramMap) {
        try {
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (CollectionUtils.isEmpty(caseIdList)) {
                return;
            }
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
                Map<String, Integer> caseCountMap = getCaseCountMap(caseReviewFunctionalCaseList);
                updateCaseReview(reviewId, caseReviewFunctionalCaseList.size(), caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());
            });
        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.RECOVER_FUNCTIONAL_CASE + "事件更新失败：{}", e.getMessage());
        }

    }

    /**
     * 6.评审用例/批量评审用例重新计算用例评审的通过率和用例评审状态和发送通知
     */
    private void updateCaseReviewByReviewFunctionalCase(Map<String, Object> paramMap) {
        try {
            String reviewId = paramMap.get(CaseEvent.Param.REVIEW_ID).toString();
            CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
            if (caseReview == null) {
                return;
            }
            Object caseIds = paramMap.get(CaseEvent.Param.CASE_IDS);
            List<String> caseIdList = JSON.parseArray(JSON.toJSONString(caseIds), String.class);
            if (CollectionUtils.isEmpty(caseIdList)) {
                return;
            }
            Object statusObjMap = paramMap.get(CaseEvent.Param.STATUS_MAP);
            Map<String, String> statusMap = JSON.parseMap(JSON.toJSONString(statusObjMap));
            updateFunctionalCase(statusMap);
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(List.of(reviewId), caseIdList, false);
            Map<String, Integer> caseCountMap = getCaseCountMap(caseReviewFunctionalCases);
            Object mapCount = paramMap.get(CaseEvent.Param.COUNT_MAP);
            Map<String, Integer> map = JSON.parseMap(JSON.toJSONString(mapCount));
            updateMapCount(map, caseCountMap);
            updateCaseReview(reviewId, caseReviewFunctionalCases.size() + caseIdList.size(), caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());

        } catch (Exception e) {
            LogUtils.error(CaseEvent.Event.REVIEW_FUNCTIONAL_CASE + "事件更新失败：{}", e.getMessage());
        }
    }

    private void updateFunctionalCase(Map<String, String> statusMap) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper mapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        statusMap.forEach((castId, status) -> {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setId(castId);
            functionalCase.setReviewStatus(status);
            mapper.updateByPrimaryKeySelective(functionalCase);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }


    private static void updateMapCount(Map<String, Integer> map, Map<String, Integer> caseCountMap) {
        if (map.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString()) != null) {
            Integer i = caseCountMap.get(UN_REVIEWED_COUNT);
            caseCountMap.put(UN_REVIEWED_COUNT, map.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString()) + i);
        }
        if (map.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) != null) {
            Integer i = caseCountMap.get(UNDER_REVIEWED_COUNT);
            caseCountMap.put(UNDER_REVIEWED_COUNT, map.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) + i);
        }
        if (map.get(FunctionalCaseReviewStatus.PASS.toString()) != null) {
            Integer i = caseCountMap.get(PASS_COUNT);
            caseCountMap.put(PASS_COUNT, map.get(FunctionalCaseReviewStatus.PASS.toString()) + i);
        }
        if (map.get(FunctionalCaseReviewStatus.UN_PASS.toString()) != null) {
            Integer i = caseCountMap.get(UN_PASS_COUNT);
            caseCountMap.put(UN_PASS_COUNT, map.get(FunctionalCaseReviewStatus.UN_PASS.toString()) + i);
        }
        if (map.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString()) != null) {
            Integer i = caseCountMap.get(RE_REVIEWED_COUNT);
            caseCountMap.put(RE_REVIEWED_COUNT, map.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString()) + i);
        }
    }

    /**
     * 准备更新caseReview
     */
    private boolean startUpdateCaseReview(Map<String, Object> paramMap, List<String> caseIdList) {
        CaseReviewFunctionalCaseExample functionalCaseExample = new CaseReviewFunctionalCaseExample();
        functionalCaseExample.createCriteria().andCaseIdIn(caseIdList);
        List<CaseReviewFunctionalCase> deletedCaseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(deletedCaseReviewFunctionalCases)) {
            return true;
        }
        List<String> reviewIds = deletedCaseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getReviewId).distinct().toList();
        //获取与选中case无关的关联关系
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = extCaseReviewFunctionalCaseMapper.getListExcludes(reviewIds, caseIdList, false);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCases)) {
            for (String reviewId : reviewIds) {
                Map<String, Integer> caseCountMap = getCaseCountMap(caseReviewFunctionalCases);
                updateCaseReview(reviewId, caseReviewFunctionalCases.size(), caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());
            }
        } else {
            Map<String, List<CaseReviewFunctionalCase>> reviewIdMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
            reviewIdMap.forEach((reviewId, caseReviewFunctionalCaseList) -> {
                Map<String, Integer> caseCountMap = getCaseCountMap(caseReviewFunctionalCaseList);
                updateCaseReview(reviewId, caseReviewFunctionalCaseList.size(), caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());
            });
            Set<String> otherReviewIds = reviewIdMap.keySet();
            List<String> leastReviewIds = reviewIds.stream().filter(t -> !otherReviewIds.contains(t)).toList();
            if (CollectionUtils.isNotEmpty(leastReviewIds)) {
                for (String reviewId : leastReviewIds) {
                    Map<String, Integer> caseCountMap = getCaseCountMap(new ArrayList<>());
                    updateCaseReview(reviewId, 0, caseCountMap, paramMap.get(CaseEvent.Param.USER_ID).toString());
                }
            }
        }

        return false;
    }


    /**
     * 处理数据库还存在关联关系获取用例各种状态的数据
     */
    private static Map<String, Integer> getCaseCountMap(List<CaseReviewFunctionalCase> caseReviewFunctionalCases) {
        Map<String, Integer> mapCount = new HashMap<>();
        Map<String, List<CaseReviewFunctionalCase>> caseMap = caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));
        if (caseMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString()) != null) {
            mapCount.put(UN_REVIEWED_COUNT, caseMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString()).size());
        } else {
            mapCount.put(UN_REVIEWED_COUNT, 0);
        }

        if (caseMap.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) != null) {
            mapCount.put(UNDER_REVIEWED_COUNT, caseMap.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()).size());
        } else {
            mapCount.put(UNDER_REVIEWED_COUNT, 0);
        }

        if (caseMap.get(FunctionalCaseReviewStatus.PASS.toString()) != null) {
            mapCount.put(PASS_COUNT, caseMap.get(FunctionalCaseReviewStatus.PASS.toString()).size());
        } else {
            mapCount.put(PASS_COUNT, 0);
        }

        if (caseMap.get(FunctionalCaseReviewStatus.UN_PASS.toString()) != null) {
            mapCount.put(UN_PASS_COUNT, caseMap.get(FunctionalCaseReviewStatus.UN_PASS.toString()).size());
        } else {
            mapCount.put(UN_PASS_COUNT, 0);
        }

        if (caseMap.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString()) != null) {
            mapCount.put(RE_REVIEWED_COUNT, caseMap.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString()).size());
        } else {
            mapCount.put(RE_REVIEWED_COUNT, 0);
        }

        return mapCount;

    }

    /**
     * 重新计算用例评审的通过率和用例数以及评审状态
     *
     * @param reviewId  当前用例评审的ID
     * @param caseCount 用例评审上显示的用例数量总和
     * @param mapCount  功能用例各种状态的数量map
     * @param userId    操作人
     */
    private void updateCaseReview(String reviewId, int caseCount, Map<String, Integer> mapCount, String userId) {
        CaseReview caseReview = new CaseReview();
        caseReview.setId(reviewId);
        //更新用例数量
        caseReview.setCaseCount(caseCount);
        //通过率
        BigDecimal passCount = BigDecimal.valueOf(mapCount.get(PASS_COUNT));
        BigDecimal totalCount = BigDecimal.valueOf(caseReview.getCaseCount());
        if (totalCount.compareTo(BigDecimal.ZERO) == 0) {
            caseReview.setPassRate(BigDecimal.ZERO);
        } else {
            BigDecimal passRate = passCount.divide(totalCount, 2, RoundingMode.HALF_UP);
            caseReview.setPassRate(passRate.multiply(BigDecimal.valueOf(100)));
        }
        boolean completed = false;
        //1.如果都是未评审，则用例评审状态为未开始
        //2.如果都是已完成状态（通过/不通过），则用例评审状态为已完成
        //3.如果有未评审/重新提审状态/评审中数量>1，则是评审中
        Integer unPassCount = mapCount.get(UN_PASS_COUNT);
        Integer unReviewedCount = mapCount.get(UN_REVIEWED_COUNT);
        Integer underReviewedCount = mapCount.get(UNDER_REVIEWED_COUNT);
        Integer reReviewedCount = mapCount.get(RE_REVIEWED_COUNT);
        if (Objects.equals(unReviewedCount, caseReview.getCaseCount())) {
            caseReview.setStatus(CaseReviewStatus.PREPARED.toString());
        } else if ((unReviewedCount + underReviewedCount + reReviewedCount) > 0) {
            caseReview.setStatus(CaseReviewStatus.UNDERWAY.toString());
        } else if ((mapCount.get(PASS_COUNT) + unPassCount) == caseReview.getCaseCount()) {
            caseReview.setStatus(CaseReviewStatus.COMPLETED.toString());
            completed = true;
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
