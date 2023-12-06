package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.mapper.*;
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

import java.nio.charset.StandardCharsets;
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
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private ExtCaseReviewHistoryMapper extCaseReviewHistoryMapper;



    /**
     * 评审功能用例
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void saveReview(ReviewFunctionalCaseRequest request, String userId) {
        //保存评审历史
        CaseReviewHistory caseReviewHistory = buildReviewHistory(request, userId);
        caseReviewHistoryMapper.insert(caseReviewHistory);
        //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算
        String functionalCaseStatus = getFunctionalCaseStatus(request);
        extCaseReviewFunctionalCaseMapper.updateStatus(request.getCaseId(), request.getReviewId(), functionalCaseStatus);
        //更新用例评审状态(判断所有用例是否结束，false：进行中，true：已完成)
        boolean completed = updateCaseReviewStatus(request.getCaseId(), request.getReviewId());
        //检查是否有@，发送@通知
        if (StringUtils.isNotBlank(request.getNotifier())) {
            List<String> relatedUsers = Arrays.asList(request.getNotifier().split(";"));
            sendNotice(relatedUsers, userId, request,NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_AT);
        }
        //发送评审通过不通过通知（评审中不发）
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            sendNotice(new ArrayList<>(), userId, request,NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_FAIL);
        }
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
            sendNotice(new ArrayList<>(), userId, request,NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_PASSED);
        }
        //检查用例评审是否结束发送通知
        if (completed) {
            sendNotice(new ArrayList<>(), userId, request,NoticeConstants.TaskType.CASE_REVIEW_TASK, NoticeConstants.Event.REVIEW_COMPLETED);
        }
    }

    @Async
    public void sendNotice(List<String> relatedUsers, String userId,ReviewFunctionalCaseRequest request, String task, String event) {
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(request.getReviewId());
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(request.getCaseId());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(task + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(task + "_" + event);
        User user = userMapper.selectByPrimaryKey(userId);
        Map paramMap;
        if (StringUtils.equalsIgnoreCase(task,NoticeConstants.TaskType.CASE_REVIEW_TASK)) {
            BeanMap beanMap = new BeanMap(caseReview);
            paramMap = new HashMap<>(beanMap);
        } else {
            BeanMap beanMap = new BeanMap(functionalCase);
            paramMap = new HashMap<>(beanMap);
            paramMap.put("reviewName", caseReview.getName());
        }
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
        noticeSendService.send(task, noticeModel);
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
     * @param request 评审规则
     * @return 功能用例的评审结果
     */
    private String getFunctionalCaseStatus(ReviewFunctionalCaseRequest request) {
        String functionalCaseStatus;
        if (StringUtils.equals(request.getReviewPassRule(), CaseReviewPassRule.SINGLE.toString())) {
            functionalCaseStatus = request.getStatus();
        } else {
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
            caseReviewHistoryExample.createCriteria().andCaseIdEqualTo(request.getCaseId()).andReviewIdEqualTo(request.getReviewId());
            List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
            Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistories.stream().sorted(Comparator.comparingLong(CaseReviewHistory::getCreateTime).reversed()).collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser, Collectors.toList()));
            CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
            caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdEqualTo(request.getCaseId());
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
    private static CaseReviewHistory buildReviewHistory(ReviewFunctionalCaseRequest request, String userId) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setId(IDGenerator.nextStr());
        caseReviewHistory.setReviewId(request.getReviewId());
        caseReviewHistory.setCaseId(request.getCaseId());
        caseReviewHistory.setStatus(request.getStatus());
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
            caseReviewHistoryDTO.setContentText(new String(caseReviewHistoryDTO.getContent(),StandardCharsets.UTF_8));
        }
        return list;
    }
}
