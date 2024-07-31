package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.constants.CaseReviewPassRule;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.mapper.*;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;


    /**
     * 评审功能用例
     *
     * @param request 页面参数
     * @param userId  当前操作人
     */
    public void saveReview(ReviewFunctionalCaseRequest request, String userId) {
        //获取用例所有评审人
        String reviewId = request.getReviewId();
        String caseId = request.getCaseId();
        CaseReviewFunctionalCaseUserExample caseReviewFunctionalCaseUserExample = new CaseReviewFunctionalCaseUserExample();
        caseReviewFunctionalCaseUserExample.createCriteria().andReviewIdEqualTo(reviewId).andCaseIdEqualTo(caseId);
        List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsers = caseReviewFunctionalCaseUserMapper.selectByExample(caseReviewFunctionalCaseUserExample);
        List<String> users = new ArrayList<>(caseReviewFunctionalCaseUsers.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList());
        //系统管理员可以随意评论，不受约束
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andRoleIdEqualTo(InternalUserRole.ADMIN.getValue()).andSourceIdEqualTo(UserRoleScope.SYSTEM).andOrganizationIdEqualTo(UserRoleScope.SYSTEM);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> systemUsers = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        boolean isAdmin = systemUsers.contains(userId) && !users.contains(userId);
        users.addAll(systemUsers);
        if (!users.contains(userId)) {
            throw new MSException(Translator.get("case_review_user"));
        }
        //保存评审历史
        CaseReviewHistory caseReviewHistory = buildReviewHistory(request, userId);
        CaseReviewHistoryExample caseReviewHistoryExample = new CaseReviewHistoryExample();
        caseReviewHistoryExample.createCriteria().andCaseIdEqualTo(request.getCaseId()).andReviewIdEqualTo(request.getReviewId()).andDeletedEqualTo(false).andAbandonedEqualTo(false);
        List<CaseReviewHistory> caseReviewHistories = caseReviewHistoryMapper.selectByExample(caseReviewHistoryExample);
        Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistories.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser));
        if (hasReviewedUserMap.get(userId) == null) {
            List<CaseReviewHistory> caseReviewHistoryList = new ArrayList<>();
            caseReviewHistoryList.add(caseReviewHistory);
            hasReviewedUserMap.put(userId, caseReviewHistoryList);
        } else {
            hasReviewedUserMap.get(userId).add(caseReviewHistory);
        }
        //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算
        String functionalCaseStatus = getFunctionalCaseStatus(request, hasReviewedUserMap, isAdmin, caseReviewFunctionalCaseUsers.size());
        extCaseReviewFunctionalCaseMapper.updateStatus(caseId, reviewId, functionalCaseStatus);
        caseReviewHistoryMapper.insert(caseReviewHistory);

        //保存富文本评论附件
        functionalCaseAttachmentService.uploadMinioFile(caseId, request.getProjectId(), request.getReviewCommentFileIds(), userId, CaseFileSourceType.REVIEW_COMMENT.toString());

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
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put(functionalCaseStatus, 1);
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put(caseId, functionalCaseStatus);
        param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.STATUS_MAP, statusMap);
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.COUNT_MAP, countMap);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);

    }

    /**
     * 计算当前评审的功能用例的评审结果
     *
     * @param request 评审规则
     * @return 功能用例的评审结果
     */
    private String getFunctionalCaseStatus(ReviewFunctionalCaseRequest request, Map<String, List<CaseReviewHistory>> hasReviewedUserMap, boolean isAdmin, int reviewerNum) {
        String functionalCaseStatus;
        CaseReviewFunctionalCaseExample caseReviewFunctionalCaseExample = new CaseReviewFunctionalCaseExample();
        caseReviewFunctionalCaseExample.createCriteria().andReviewIdEqualTo(request.getReviewId()).andCaseIdEqualTo(request.getCaseId());
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(caseReviewFunctionalCaseExample);
        String status = caseReviewFunctionalCases.getFirst().getStatus();
        if (StringUtils.equals(request.getReviewPassRule(), CaseReviewPassRule.SINGLE.toString())) {
            if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) || isAdmin) {
                functionalCaseStatus = status;
            } else {
                functionalCaseStatus = request.getStatus();
            }
        } else {
            if (isAdmin) {
                return status;
            }
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            AtomicInteger passCount = new AtomicInteger();
            AtomicInteger unPassCount = new AtomicInteger();
            hasReviewedUserMap.forEach((k, v) -> {
                //过滤掉每个人的评审中状态，每个人的评审中为建议，建议不做评审结果，这里排除
                List<CaseReviewHistory> list = v.stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString())).sorted(Comparator.comparing(CaseReviewHistory::getCreateTime).reversed()).toList();
                if (CollectionUtils.isNotEmpty(list) && StringUtils.equalsIgnoreCase(list.getFirst().getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                    passCount.set(passCount.get() + 1);
                }
                if (CollectionUtils.isNotEmpty(list) && StringUtils.equalsIgnoreCase(list.getFirst().getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                    unPassCount.set(unPassCount.get() + 1);
                }
            });
            //检查是否全部是通过，全是才是PASS,否则是评审中(如果时自动重新提审，会有个system用户，这里需要排出一下) 建议不是评审中
            if (hasReviewedUserMap.get(UserRoleScope.SYSTEM) != null) {
                hasReviewedUserMap.remove(UserRoleScope.SYSTEM);
            }
            if (unPassCount.get() > 0) {
                functionalCaseStatus = FunctionalCaseReviewStatus.UN_PASS.toString();
            } else if (passCount.get() > 0 && passCount.get() < reviewerNum) {
                //通过> 0 但不是全部通过 为评审中
                functionalCaseStatus = FunctionalCaseReviewStatus.UNDER_REVIEWED.toString();
            } else if (passCount.get() == reviewerNum) {
                //检查是否全部是通过，全是才是PASS
                functionalCaseStatus = FunctionalCaseReviewStatus.PASS.toString();
            } else {
                functionalCaseStatus = FunctionalCaseReviewStatus.UN_REVIEWED.toString();
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
        caseReviewHistory.setAbandoned(false);
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            if (StringUtils.isBlank(request.getContent())) {
                throw new MSException(Translator.get("case_review_content.not.exist"));
            } else {
                caseReviewHistory.setContent(request.getContent().getBytes());
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
        List<CaseReviewHistoryDTO> list = extCaseReviewHistoryMapper.getHistoryListWidthAbandoned(caseId, reviewId);
        for (CaseReviewHistoryDTO caseReviewHistoryDTO : list) {
            if (StringUtils.equalsIgnoreCase(caseReviewHistoryDTO.getCreateUser(), UserRoleScope.SYSTEM)) {
                caseReviewHistoryDTO.setUserName(Translator.get("case_review_history.system"));
            }
            if (caseReviewHistoryDTO.getContent() != null) {
                caseReviewHistoryDTO.setContentText(new String(caseReviewHistoryDTO.getContent(), StandardCharsets.UTF_8));
            }
        }
        return list;
    }
}
