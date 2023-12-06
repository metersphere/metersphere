package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseUserExample;
import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.CaseReviewHistoryExample;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseUserMapper;
import io.metersphere.functional.mapper.CaseReviewHistoryMapper;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseMapper;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NoticeSendService noticeSendService;

    /**
     * 评审功能用例
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void saveReview(ReviewFunctionalCaseRequest request, String userId) {
        String caseId = request.getCaseId();
        String reviewId = request.getReviewId();
        String reviewPassRule = request.getReviewPassRule();
        String status = request.getStatus();
        //保存评审历史
        CaseReviewHistory caseReviewHistory = buildReviewHistory(request, status, caseId, reviewId, userId);
        caseReviewHistoryMapper.insert(caseReviewHistory);
        //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算
        String functionalCaseStatus = getFunctionalCaseStatus(reviewPassRule, status, caseId, reviewId);
        extCaseReviewFunctionalCaseMapper.updateStatus(caseId, reviewId, functionalCaseStatus);
        //更新用例评审状态(判断所有用例是否结束，false：进行中，true：已完成)
        boolean completed = updateCaseReviewStatus(caseId, reviewId);
        //检查是否有@，发送@通知
        if (StringUtils.isNotBlank(request.getNotifier())) {
            List<String> relatedUsers = Arrays.asList(request.getNotifier().split(";"));
            sendNotice(relatedUsers, userId, reviewId, NoticeConstants.Event.REVIEW_AT);
        }
        //发送评审通过不通过通知（评审中不发）
        if (StringUtils.equalsIgnoreCase(status, FunctionalCaseReviewStatus.UN_PASS.toString())) {
            sendNotice(new ArrayList<>(), userId, reviewId, NoticeConstants.Event.REVIEW_FAIL);
        }
        if (StringUtils.equalsIgnoreCase(status, FunctionalCaseReviewStatus.PASS.toString())) {
            sendNotice(new ArrayList<>(), userId, reviewId, NoticeConstants.Event.REVIEW_PASSED);
        }
        //检查用例评审是否结束发送通知
        if (completed) {
            sendNotice(new ArrayList<>(), userId, reviewId, NoticeConstants.Event.REVIEW_COMPLETED);
        }
    }

    @Async
    protected void sendNotice(List<String> relatedUsers, String userId, String reviewId, String event) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(reviewId);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK + "_" + event);
        User user = userMapper.selectByPrimaryKey(userId);
        BeanMap beanMap = new BeanMap(caseReview);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(userId)
                .context(template)
                .subject(subject)
                .paramMap(paramMap)
                .event(event)
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(relatedUsers)
                .build();
        noticeSendService.send(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, noticeModel);
    }

    /**
     * 更新用例评审自身的状态
     *
     * @param caseId   功能用例Id
     * @param reviewId 用例评审Id
     * @return completed
     */
    private boolean updateCaseReviewStatus(String caseId, String reviewId) {
        boolean completed = false;
        List<String> statusList = new ArrayList<>();
        statusList.add(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        statusList.add(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        statusList.add(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        Long unCompletedCaseCount = extCaseReviewFunctionalCaseMapper.getUnCompletedCaseCount(reviewId, statusList);
        CaseReview caseReview = new CaseReview();
        caseReview.setId(reviewId);
        if (unCompletedCaseCount > 0L) {
            caseReview.setStatus(CaseReviewStatus.UNDERWAY.toString());
        } else {
            completed = true;
            caseReview.setStatus(CaseReviewStatus.COMPLETED.toString());
        }
        caseReviewMapper.updateByPrimaryKeySelective(caseReview);
        return completed;
    }

    /**
     * 计算当前评审的功能用例的评审结果
     *
     * @param reviewPassRule 评审规则
     * @param status         评审状态
     * @param caseId         用例Id
     * @param reviewId       评审Id
     * @return 功能用例的评审结果
     */
    private String getFunctionalCaseStatus(String reviewPassRule, String status, String caseId, String reviewId) {
        String functionalCaseStatus;
        if (StringUtils.equals(reviewPassRule, CaseReviewPassRule.SINGLE.toString())) {
            functionalCaseStatus = status;
        } else {
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
            caseReviewHistoryExample.createCriteria().andCaseIdEqualTo(caseId).andReviewIdEqualTo(reviewId);
            List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
            Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistories.stream().sorted(Comparator.comparingLong(CaseReviewHistory::getCreateTime).reversed()).collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser, Collectors.toList()));
            CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
            caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo(caseId);
            long reviewerNum = caseReviewFunctionalCaseUserMapper.countByExample(caseReviewFunctionalCaseUserExample);
            if ((int) reviewerNum > hasReviewedUserMap.size()) {
                functionalCaseStatus = FunctionalCaseReviewStatus.UNDER_REVIEWED.toString();
            } else {
                AtomicBoolean hasUnPass = new AtomicBoolean(false);
                AtomicInteger passCount = new AtomicInteger();
                hasReviewedUserMap.forEach((k, v) -> {
                    if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                        hasUnPass.set(true);
                    }
                    if (StringUtils.equalsIgnoreCase(v.get(0).getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                        passCount.set(passCount.get() + 1);
                    }
                });
                if (hasUnPass.get()) {
                    functionalCaseStatus = FunctionalCaseReviewStatus.UN_PASS.toString();
                } else {
                    //检查是否全部是通过，全是才是PASS,否则是评审中
                    if (passCount.get() == hasReviewedUserMap.size()) {
                        functionalCaseStatus = FunctionalCaseReviewStatus.PASS.toString();
                    } else {
                        functionalCaseStatus = FunctionalCaseReviewStatus.UNDER_REVIEWED.toString();
                    }
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
    private static CaseReviewHistory buildReviewHistory(ReviewFunctionalCaseRequest request, String status, String caseId, String reviewId, String userId) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setReviewId(reviewId);
        caseReviewHistory.setCaseId(caseId);
        caseReviewHistory.setStatus(status);
        if (StringUtils.equalsIgnoreCase(status, FunctionalCaseReviewStatus.UN_PASS.toString())) {
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
}
