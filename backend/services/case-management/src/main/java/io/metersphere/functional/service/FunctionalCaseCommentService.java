package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.domain.FunctionalCaseCommentExample;
import io.metersphere.functional.dto.CommentEnum;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
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

import java.util.*;

/**
 * @author guoyuqi
 */

@Service
public class FunctionalCaseCommentService {

    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FunctionalCaseNoticeService functionalCaseNoticeService;

    @Resource
    private NoticeSendService noticeSendService;

    /**
     * 新增评论
     *
     * @param functionalCaseCommentRequest functionalCaseCommentDTO
     * @param userId                       当前操作用户
     * @return FunctionalCaseComment
     */
    public FunctionalCaseComment saveComment(FunctionalCaseCommentRequest functionalCaseCommentRequest, String userId) {
        checkCase(functionalCaseCommentRequest);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest, userId);
        if (StringUtils.equals(functionalCaseCommentRequest.getEvent(), NoticeConstants.Event.REPLY)) {
            return saveCommentWidthNotice(functionalCaseCommentRequest, functionalCaseComment, userId);
        } else {
            return saveCommentWidthOutNotice(functionalCaseCommentRequest, functionalCaseComment, userId);
        }
    }



    /**
     * 组装除通知人，被回复的id外的其他用例评论属性
     *
     * @param functionalCaseCommentRequest 页面参数
     * @param userId                       当前操作人
     * @return FunctionalCaseComment
     */
    private static FunctionalCaseComment getFunctionalCaseComment(FunctionalCaseCommentRequest functionalCaseCommentRequest, String userId) {
        FunctionalCaseComment functionalCaseComment = new FunctionalCaseComment();
        functionalCaseComment.setId(IDGenerator.nextStr());
        functionalCaseComment.setCaseId(functionalCaseCommentRequest.getCaseId());
        functionalCaseComment.setContent(functionalCaseCommentRequest.getContent());
        functionalCaseComment.setCreateUser(userId);
        functionalCaseComment.setCreateTime(System.currentTimeMillis());
        functionalCaseComment.setUpdateTime(System.currentTimeMillis());
        functionalCaseComment.setType(CommentEnum.CASE.toString());
        return functionalCaseComment;
    }

    private void checkCase(FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        if (functionalCase == null) {
            throw new MSException(Translator.get("case_comment.case_is_null"));
        }
    }

    /**
     * 非REPLAY事件，保存
     * @param functionalCaseCommentRequest 页面参数
     * @param functionalCaseComment 被组装的半份数据
     * @return FunctionalCaseComment
     */
    public FunctionalCaseComment saveCommentWidthOutNotice(FunctionalCaseCommentRequest functionalCaseCommentRequest, FunctionalCaseComment functionalCaseComment, String userId) {
        if (StringUtils.isNotBlank(functionalCaseCommentRequest.getNotifier())) {
            functionalCaseComment.setNotifier(functionalCaseCommentRequest.getNotifier());
        }
        functionalCaseCommentMapper.insert(functionalCaseComment);
        FunctionalCaseDTO functionalCaseDTO = functionalCaseNoticeService.getFunctionalCaseDTO(functionalCaseCommentRequest);
        sendNotice(functionalCaseCommentRequest, userId, functionalCaseDTO);
        return functionalCaseComment;
    }

    /**
     * 如果是REPLAY事件，需要再次发送回复被@的通知
     * @param functionalCaseCommentRequest 页面参数
     * @param functionalCaseComment 被组装的半份数据
     * @return FunctionalCaseComment
     */
    public FunctionalCaseComment saveCommentWidthNotice(FunctionalCaseCommentRequest functionalCaseCommentRequest, FunctionalCaseComment functionalCaseComment, String userId) {
        checkParentId(functionalCaseCommentRequest, functionalCaseComment);
        if (StringUtils.isBlank(functionalCaseCommentRequest.getReplyUser())) {
            throw new MSException(Translator.get("case_comment.reply_user_is_null"));
        }
        functionalCaseCommentRequest.setReplyUser(functionalCaseCommentRequest.getReplyUser());
        if (StringUtils.isNotBlank(functionalCaseCommentRequest.getNotifier())) {
            functionalCaseComment.setNotifier(functionalCaseCommentRequest.getNotifier());
        }
        functionalCaseCommentMapper.insert(functionalCaseComment);
        FunctionalCaseDTO functionalCaseDTOReply = functionalCaseNoticeService.getFunctionalCaseDTO(functionalCaseCommentRequest);
        sendNotice(functionalCaseCommentRequest, userId, functionalCaseDTOReply);
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        FunctionalCaseDTO functionalCaseDTO = functionalCaseNoticeService.getFunctionalCaseDTO(functionalCaseCommentRequest);
        //发通知
        sendNotice(functionalCaseCommentRequest, userId, functionalCaseDTO);
        return functionalCaseComment;
    }

    @Async
    public void sendNotice(FunctionalCaseCommentRequest functionalCaseCommentRequest, String userId, FunctionalCaseDTO functionalCaseDTO) {
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK + "_" + functionalCaseCommentRequest.getEvent());
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK + "_" + functionalCaseCommentRequest.getEvent());
        List<String> relatedUsers = getRelatedUsers(functionalCaseDTO.getRelatedUsers());
        User user = userMapper.selectByPrimaryKey(userId);
        BeanMap beanMap = new BeanMap(functionalCaseDTO);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(userId)
                .context(template)
                .subject(subject)
                .paramMap(paramMap)
                .event(functionalCaseCommentRequest.getEvent())
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(relatedUsers)
                .build();
        noticeSendService.send(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, noticeModel);
    }

    /**
     * 检查被回复的评论是否为空
     * @param functionalCaseCommentRequest 页面参数
     * @param functionalCaseComment 被组装的半份数据
     */
    private void checkParentId(FunctionalCaseCommentRequest functionalCaseCommentRequest, FunctionalCaseComment functionalCaseComment) {
        String parentId = functionalCaseCommentRequest.getParentId();
        if (StringUtils.isBlank(parentId)) {
            throw new MSException(Translator.get("case_comment.parent_id_is_null"));
        }
        FunctionalCaseComment parentComment = functionalCaseCommentMapper.selectByPrimaryKey(parentId);
        if (parentComment==null) {
            throw new MSException(Translator.get("case_comment.parent_case_is_null"));
        }
        functionalCaseComment.setParentId(parentId);
    }

    private List<String> getRelatedUsers(Object relatedUsers) {
        String relatedUser = (String) relatedUsers;
        List<String> relatedUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(relatedUser)) {
            relatedUserList = Arrays.asList(relatedUser.split(";"));
        }
        return relatedUserList;
    }

    public void deleteComment(String commentId) {
        FunctionalCaseComment functionalCaseComment = functionalCaseCommentMapper.selectByPrimaryKey(commentId);
        if (functionalCaseComment == null) {
            return;
        }
        //删除选中的评论下的所有回复
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andParentIdEqualTo(commentId);
        List<FunctionalCaseComment> functionalCaseComments = functionalCaseCommentMapper.selectByExample(functionalCaseCommentExample);
        List<String> commentIds = new ArrayList<>(functionalCaseComments.stream().map(FunctionalCaseComment::getId).toList());
        commentIds.add(commentId);
        functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andIdIn(commentIds);
        functionalCaseCommentMapper.deleteByExample(functionalCaseCommentExample);
    }
}
