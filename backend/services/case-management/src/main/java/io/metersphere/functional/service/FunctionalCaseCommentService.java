package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.domain.FunctionalCaseCommentExample;
import io.metersphere.functional.dto.CommentEnum;
import io.metersphere.functional.dto.FunctionalCaseCommentDTO;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
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
import org.apache.commons.collections.CollectionUtils;



import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        checkCase(functionalCaseCommentRequest.getCaseId());
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

    private void checkCase(String caseId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
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

    public List<FunctionalCaseCommentDTO> getCommentList(String caseId) {
        checkCase(caseId);
        //查询出当前用例下的所有数据
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        List<FunctionalCaseComment> functionalCaseComments = functionalCaseCommentMapper.selectByExampleWithBLOBs(functionalCaseCommentExample);
        List<String> userIds = getUserIds(functionalCaseComments);
        Map<String, User> userMap = getUserMap(userIds);
        return buildData(functionalCaseComments, userMap);
    }

    /**
     * 组装需要返回前端的数据结构
     * @param functionalCaseComments 查出来的所有当前用例的评论信息
     * @param userMap 用户信息
     */
    private List<FunctionalCaseCommentDTO> buildData(List<FunctionalCaseComment> functionalCaseComments, Map<String, User> userMap) {
        List<FunctionalCaseCommentDTO>list = new ArrayList<>();
        List<FunctionalCaseComment> rootList = functionalCaseComments.stream().filter(t -> StringUtils.isBlank(t.getParentId())).toList();
        List<FunctionalCaseComment> replyList = functionalCaseComments.stream().filter(t -> StringUtils.isNotBlank(t.getParentId())).toList();
        Map<String, List<FunctionalCaseComment>> commentMap = replyList.stream().collect(Collectors.groupingBy(FunctionalCaseComment::getParentId));
        for (FunctionalCaseComment functionalCaseComment : rootList) {
            FunctionalCaseCommentDTO functionalCaseCommentDTO = new FunctionalCaseCommentDTO();
            BeanUtils.copyBean(functionalCaseCommentDTO,functionalCaseComment);
            functionalCaseCommentDTO.setUserName(userMap.get(functionalCaseComment.getCreateUser()).getName());
            List<FunctionalCaseComment> replyComments = commentMap.get(functionalCaseComment.getId());
            if (CollectionUtils.isNotEmpty(replyComments)) {
                List<FunctionalCaseCommentDTO> replies = getReplies(userMap, functionalCaseComment, replyComments);
                functionalCaseCommentDTO.setReplies(replies);
            }
            list.add(functionalCaseCommentDTO);
        }
        return list;
    }

    private List<FunctionalCaseCommentDTO> getReplies(Map<String, User> userMap, FunctionalCaseComment functionalCaseComment, List<FunctionalCaseComment> replyComments) {
        List<FunctionalCaseCommentDTO> replies = new ArrayList<>();
        for (FunctionalCaseComment replyComment : replyComments) {
            FunctionalCaseCommentDTO functionalCaseCommentDTOReply = new FunctionalCaseCommentDTO();
            BeanUtils.copyBean(functionalCaseCommentDTOReply,replyComment);
            functionalCaseCommentDTOReply.setUserName(userMap.get(replyComment.getCreateUser()).getName());
            if (StringUtils.isBlank(replyComment.getReplyUser())) {
                functionalCaseCommentDTOReply.setReplyUserName(userMap.get(functionalCaseComment.getCreateUser()).getName());
            } else {
                functionalCaseCommentDTOReply.setReplyUserName(userMap.get(replyComment.getReplyUser()).getName());
            }
            replies.add(functionalCaseCommentDTOReply);
        }
        return replies.stream().sorted(Comparator.comparing(FunctionalCaseComment::getCreateTime).reversed()).toList();
    }

    /**
     * 根据userIds 获取user信息
     * @param userIds userIds
     * @return Map<String, User>
     */
    private Map<String, User> getUserMap(List<String> userIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
       return users.stream().collect(Collectors.toMap(User::getId, item -> item));
    }

    /**
     * 获取评论里所有人员信息
     * @param functionalCaseComments 评论集合
     * @return List<String>userIds
     */
    private static List<String> getUserIds(List<FunctionalCaseComment> functionalCaseComments) {
        List<String> userIds = new ArrayList<>(functionalCaseComments.stream().flatMap(functionalCaseComment -> Stream.of(functionalCaseComment.getCreateUser(), functionalCaseComment.getReplyUser())).toList());
        List<String> notifierList = functionalCaseComments.stream().map(FunctionalCaseComment::getNotifier).filter(StringUtils::isNotBlank).toList();
        for (String notifierStr : notifierList) {
            List<String> notifiers = Arrays.asList(notifierStr.split(";"));
            userIds.addAll(notifiers);
        }
        userIds = userIds.stream().distinct().toList();
        return userIds;
    }

}
