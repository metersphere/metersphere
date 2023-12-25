package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseUserExample;
import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.CaseReviewHistoryExample;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseUserMapper;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtCaseReviewHistoryMapper;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReviewFunctionalCaseService {

    @Resource
    private CaseReviewHistoryMapper caseReviewHistoryMapper;
    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    @Resource
    private ExtCaseReviewHistoryMapper extCaseReviewHistoryMapper;
    @Resource
    private ReviewSendNoticeService reviewSendNoticeService;
    @Resource
    private BaseCaseProvider provider;

    /**
     * 评审功能用例
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void saveReview(ReviewFunctionalCaseRequest request, String userId) {
        //保存评审历史
        String reviewId = request.getReviewId();
        String caseId = request.getCaseId();
        CaseReviewHistory caseReviewHistory = buildReviewHistory(request, userId);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdEqualTo(request.getCaseId()).andReviewIdEqualTo(request.getReviewId()).andDeletedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistories.stream().sorted(Comparator.comparingLong(CaseReviewHistory::getCreateTime).reversed()).collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser, Collectors.toList()));
        if (hasReviewedUserMap.get(userId) ==null) {
            List<CaseReviewHistory>caseReviewHistoryList = new ArrayList<>();
            caseReviewHistoryList.add(caseReviewHistory);
            hasReviewedUserMap.put(userId,caseReviewHistoryList);
        }
        //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算
        String functionalCaseStatus = getFunctionalCaseStatus(request, hasReviewedUserMap);
        extCaseReviewFunctionalCaseMapper.updateStatus(caseId, reviewId, functionalCaseStatus);
        caseReviewHistoryMapper.insert(caseReviewHistory);
        int passCount = 0;
        if (StringUtils.equalsIgnoreCase(FunctionalCaseReviewStatus.PASS.toString(),functionalCaseStatus)) {
            passCount=1;
        }

        //检查是否有@，发送@通知
        if (StringUtils.isNotBlank(request.getNotifier())) {
            List<String> relatedUsers = Arrays.asList(request.getNotifier().split(";"));
            reviewSendNoticeService.sendNoticeCase(relatedUsers, userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_AT, reviewId);
        }
        //发送评审通过不通过通知（评审中不发）
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_FAIL, reviewId);
        }
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
            reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_PASSED, reviewId);
        }
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
        param.put(CaseEvent.Param.PASS_COUNT,passCount);
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.STATUS, request.getStatus());
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);

    }

    /**
     * 计算当前评审的功能用例的评审结果
     *
     * @param request 评审规则
     * @return 功能用例的评审结果
     */
    private String getFunctionalCaseStatus(ReviewFunctionalCaseRequest request, Map<String, List<CaseReviewHistory>> hasReviewedUserMap) {
        String functionalCaseStatus;
        if (StringUtils.equals(request.getReviewPassRule(), CaseReviewPassRule.SINGLE.toString())) {
            functionalCaseStatus = request.getStatus();
        } else {
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
            caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdEqualTo(request.getCaseId());
            long reviewerNum = caseReviewFunctionalCaseUserMapper.countByExample(caseReviewFunctionalCaseUserExample);
            AtomicInteger passCount = new AtomicInteger();
            AtomicInteger unPassCount = new AtomicInteger();
            hasReviewedUserMap.forEach((k, v) -> {
                if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                    passCount.set(passCount.get() + 1);
                }
                if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                    unPassCount.set(unPassCount.get() + 1);
                }
            });
            if (unPassCount.get()>0) {
                functionalCaseStatus = FunctionalCaseReviewStatus.UN_PASS.toString();
            } else if ((int) reviewerNum > hasReviewedUserMap.size()) {
                functionalCaseStatus = FunctionalCaseReviewStatus.UNDER_REVIEWED.toString();
            } else {
                //检查是否全部是通过，全是才是PASS,否则是评审中
                if (passCount.get() == hasReviewedUserMap.size()) {
                    functionalCaseStatus = FunctionalCaseReviewStatus.PASS.toString();
                } else {
                    functionalCaseStatus = FunctionalCaseReviewStatus.UNDER_REVIEWED.toString();
                }
            }
        }
        return functionalCaseStatus;
    }

    /**
     * 构建评审历史表
     *
     * @param request request
     * @param userId  当前操作人
     * @return CaseReviewHistory
     */
    private static CaseReviewHistory buildReviewHistory(ReviewFunctionalCaseRequest request, String userId) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setReviewId(request.getReviewId());
        caseReviewHistory.setCaseId(request.getCaseId());
        caseReviewHistory.setStatus(request.getStatus());
        caseReviewHistory.setDeleted(false);
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            if (StringUtils.isBlank(request.getContent())) {
                throw new MSException(Translator.get("case_review_content.not.exist"));
            }
        } else {
            if (StringUtils.isNotBlank(request.getContent())) {
                caseReviewHistory.setContent(request.getContent().getBytes());
            }
        }
        caseReviewHistory.setNotifier(request.getNotifier());
        caseReviewHistory.setCreateUser(userId);
        caseReviewHistory.setCreateTime(System.currentTimeMillis());
        return caseReviewHistory;
    }

    public List<CaseReviewHistoryDTO> getCaseReviewHistoryList(String reviewId, String caseId) {
        List<CaseReviewHistoryDTO> list = extCaseReviewHistoryMapper.list(caseId, reviewId);
        for (CaseReviewHistoryDTO caseReviewHistoryDTO : list) {
            if (StringUtils.equalsIgnoreCase(caseReviewHistoryDTO.getCreateUser(), "system")) {
                caseReviewHistoryDTO.setUserName(Translator.get("case_review_history.system"));
            }
            if (caseReviewHistoryDTO.getContent() != null) {
                caseReviewHistoryDTO.setContentText(new String(caseReviewHistoryDTO.getContent(), StandardCharsets.UTF_8));
            }
        }
        return list;
    }
}
