package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugComment;
import io.metersphere.bug.domain.BugCommentExample;
import io.metersphere.bug.dto.BugCommentDTO;
import io.metersphere.bug.dto.BugCommentNoticeDTO;
import io.metersphere.bug.dto.request.BugCommentEditRequest;
import io.metersphere.bug.mapper.BugCommentMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.CommentUserInfo;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugCommentService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private BugCommentMapper bugCommentMapper;
    @Resource
    private BugCommentNoticeService bugCommentNoticeService;

    /**
     * 获取缺陷评论
     * @param bugId 缺陷ID
     * @return 缺陷评论
     */
    public List<BugCommentDTO> getComments(String bugId) {
        BugCommentExample example = new BugCommentExample();
        example.createCriteria().andBugIdEqualTo(bugId);
        List<BugComment> bugComments = bugCommentMapper.selectByExample(example);
        return this.generateCommentDTOs(bugComments);
    }

    /**
     * 生成缺陷评论DTO
     *
     * @param bugComments 缺陷评论集合
     * @return 缺陷评论DTO
     */
    private List<BugCommentDTO> generateCommentDTOs(List<BugComment> bugComments) {
        if (CollectionUtils.isEmpty(bugComments)) {
            return new ArrayList<>();
        }

        // BugComment -> BugCommentDTO
        Map<String, CommentUserInfo> userMap = getUserMap(bugComments);
        List<BugCommentDTO> bugCommentDTOList = bugComments.stream().map(bugComment -> {
            BugCommentDTO commentDTO = new BugCommentDTO();
            BeanUtils.copyBean(commentDTO, bugComment);
            commentDTO.setReplyUserName(StringUtils.isNotEmpty(bugComment.getReplyUser()) ?
                    userMap.get(bugComment.getReplyUser()).getName() : null);
            commentDTO.setCommentUserInfo(userMap.get(bugComment.getCreateUser()));
            commentDTO.setNotifierOption(getNotifyUserOption(bugComment.getNotifier(), userMap));
            return commentDTO;
        }).toList();

        // 父级评论
        List<BugCommentDTO> parentComments = bugCommentDTOList.stream().filter(bugCommentDTO -> StringUtils.isEmpty(bugCommentDTO.getParentId()))
                .sorted(Comparator.comparing(BugCommentDTO::getCreateTime, Comparator.reverseOrder())).toList();
        parentComments.forEach(parentComment -> {
            // 子级评论
            List<BugCommentDTO> childComments = bugCommentDTOList.stream().filter(bugCommentDTO -> StringUtils.equals(bugCommentDTO.getParentId(), parentComment.getId()))
                    .sorted(Comparator.comparing(BugCommentDTO::getCreateTime, Comparator.reverseOrder())).toList();
            parentComment.setChildComments(childComments);
        });
        return parentComments;
    }


    /**
     * 批量获取缺陷ID
     */
    public Map<String, List<BugCommentDTO>> getComments(@NotEmpty List<String> bugIds) {
        BugCommentExample example = new BugCommentExample();
        example.createCriteria().andBugIdIn(bugIds);
        List<BugComment> bugComments = bugCommentMapper.selectByExample(example);
        Map<String, List<BugComment>> bugCommentByBugId = bugComments.stream().collect(Collectors.groupingBy(BugComment::getBugId));

        Map<String, List<BugCommentDTO>> returnMap = new HashMap<>();
        for (Map.Entry<String, List<BugComment>> entry : bugCommentByBugId.entrySet()) {
            returnMap.put(entry.getKey(), generateCommentDTOs(entry.getValue()));
        }
        return returnMap;
    }

    /**
     * 添加评论
     * @param request 评论请求参数
     * @param currentUser 当前用户ID
     * @return 缺陷评论
     */
    public BugComment addComment(BugCommentEditRequest request, String currentUser) {
        checkBug(request.getBugId());
        BugComment bugComment = getBugComment(request, currentUser, false);
        if (StringUtils.equals(request.getEvent(), NoticeConstants.Event.REPLY)) {
            return addBugCommentAndReplyNotice(request, bugComment, currentUser);
        } else {
            return addBugCommentAndCommentNotice(request, bugComment, currentUser);
        }
    }

    /**
     * 修改评论
     * @param request 评论请求参数
     * @param currentUser 当前用户ID
     * @return 缺陷评论
     */
    public BugComment updateComment(BugCommentEditRequest request, String currentUser) {
        checkComment(request.getId());
        BugComment bugComment = getBugComment(request, currentUser, true);
        return updateBugCommentAndNotice(request, bugComment, currentUser);
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     */
    public void deleteComment(String commentId) {
        checkComment(commentId);
        BugComment bugComment = bugCommentMapper.selectByPrimaryKey(commentId);
        if (StringUtils.isEmpty(bugComment.getParentId())) {
            // 如果是父评论, 先删除子评论
            BugCommentExample example = new BugCommentExample();
            example.createCriteria().andParentIdEqualTo(commentId);
            bugCommentMapper.deleteByExample(example);
        }
        // 删除当条评论
        bugCommentMapper.deleteByPrimaryKey(commentId);
    }

    /**
     * 新建评论并发送REPLY通知(需处理@提醒人)
     * @param request 缺陷评论请求参数
     * @param bugComment 缺陷评论
     * @return 缺陷评论
     */
    public BugComment addBugCommentAndReplyNotice(BugCommentEditRequest request, BugComment bugComment, String currentUser) {
        checkAndSetReplyComment(request, bugComment);
        bugCommentMapper.insertSelective(bugComment);
        // 回复通知
        BugCommentNoticeDTO bugCommentNotice = bugCommentNoticeService.getBugCommentNotice(request);
        bugCommentNoticeService.sendNotice(request, bugCommentNotice, currentUser);
        // @通知
        request.setEvent(NoticeConstants.Event.AT);
        bugCommentNotice = bugCommentNoticeService.getBugCommentNotice(request);
        bugCommentNoticeService.sendNotice(request, bugCommentNotice, currentUser);
        return bugComment;
    }

    /**
     * 新建评论并发送COMMENT通知(需处理@提醒人)
     * @param request 缺陷评论请求参数
     * @param bugComment 缺陷评论
     * @return 缺陷评论
     */
    public BugComment addBugCommentAndCommentNotice(BugCommentEditRequest request, BugComment bugComment, String currentUser) {
        bugComment.setNotifier(request.getNotifier());
        bugCommentMapper.insertSelective(bugComment);
        /*
         * 通知;
         * 如果通知@人不为空, 先发送@通知, 再发送评论通知.
         * 如果通知@人为空, 只发送评论通知.
         */
        BugCommentNoticeDTO bugCommentNotice = bugCommentNoticeService.getBugCommentNotice(request);
        bugCommentNoticeService.sendNotice(request, bugCommentNotice, currentUser);
        if (StringUtils.equals(request.getEvent(), NoticeConstants.Event.AT)) {
            request.setEvent(NoticeConstants.Event.COMMENT);
            bugCommentNoticeService.sendNotice(request, bugCommentNotice, currentUser);
        }
        return bugComment;
    }

    /**
     * 更新评论并发送通知(只需处理@提醒人)
     * @param request 请求参数
     * @param bugComment 缺陷评论
     * @param currentUser 当前用户ID
     * @return 缺陷评论
     */
    public BugComment updateBugCommentAndNotice(BugCommentEditRequest request, BugComment bugComment, String currentUser) {
        bugComment.setNotifier(request.getNotifier());
        bugCommentMapper.updateByPrimaryKeySelective(bugComment);
        if (StringUtils.equals(request.getEvent(), NoticeConstants.Event.COMMENT)) {
            // COMMENT事件, 无需通知
            return bugComment;
        }
        // REPLY及AT通知, 都只处理@通知人
        if (StringUtils.equals(request.getEvent(), NoticeConstants.Event.REPLY)) {
            // REPLY事件需保留回复人
            request.setReplyUser(null);
            request.setEvent(NoticeConstants.Event.AT);
        }
        BugCommentNoticeDTO bugCommentNotice = bugCommentNoticeService.getBugCommentNotice(request);
        bugCommentNoticeService.sendNotice(request, bugCommentNotice, currentUser);
        return bugComment;
    }

    /**
     * 获取评论
     * @param request 缺陷评论请求参数
     * @param currentUser 当前用户ID
     * @return 缺陷评论
     */
    public static BugComment getBugComment(BugCommentEditRequest request, String currentUser, boolean isUpdate) {
        BugComment bugComment = new BugComment();
        bugComment.setId(isUpdate ? request.getId() : IDGenerator.nextStr());
        bugComment.setBugId(request.getBugId());
        bugComment.setContent(request.getContent());
        if (!isUpdate) {
            bugComment.setCreateUser(currentUser);
            bugComment.setCreateTime(System.currentTimeMillis());
        }
        bugComment.setUpdateUser(currentUser);
        bugComment.setUpdateTime(System.currentTimeMillis());
        return bugComment;
    }

    /**
     * 校验并设置评论信息
     * @param request 缺陷评论请求参数
     * @param bugComment 缺陷评论
     */
    private void checkAndSetReplyComment(BugCommentEditRequest request, BugComment bugComment) {
        if (StringUtils.isEmpty(request.getParentId())) {
            throw new MSException(Translator.get("bug_comment.parent_id.not_blank"));
        }
        BugComment parentComment = bugCommentMapper.selectByPrimaryKey(request.getParentId());
        if (parentComment == null) {
            throw new MSException(Translator.get("bug_comment.parent.not_exist"));
        }
        bugComment.setParentId(request.getParentId());
        if (StringUtils.isEmpty(request.getReplyUser())) {
            throw new MSException(Translator.get("bug_comment.reply_user.not_blank"));
        }
        bugComment.setReplyUser(request.getReplyUser());
        bugComment.setNotifier(request.getNotifier());
    }

    /**
     * 校验缺陷是否存在
     * @param bugId 缺陷ID
     */
    private void checkBug(String bugId) {
        Bug bug = bugMapper.selectByPrimaryKey(bugId);
        if (bug == null) {
            throw new IllegalArgumentException(Translator.get("bug_not_exist"));
        }
    }

    /**
     * 校验评论是否存在
     * @param commentId 评论ID
     */
    private void checkComment(String commentId) {
        BugComment bugComment = bugCommentMapper.selectByPrimaryKey(commentId);
        if (bugComment == null) {
            throw new IllegalArgumentException(Translator.get("bug_comment_not_exist"));
        }
    }

    /**
     * 获取评论@通知人选项
     * @param notifier 通知人ID串
     * @param userMap 用户信息Map
     * @return 通知人选项
     */
    private List<OptionDTO> getNotifyUserOption(String notifier, Map<String, CommentUserInfo> userMap) {
        if (StringUtils.isBlank(notifier)) {
            return new ArrayList<>();
        }
        List<String> notifyUserIds = Arrays.asList(notifier.split(";"));
        return userMap.values().stream().filter(user -> notifyUserIds.contains(user.getId())).map(user -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(user.getId());
            optionDTO.setName(user.getName());
            return optionDTO;
        }).toList();
    }

    /**
     * 获取用户Map
     * @param bugComments 缺陷评论集合
     * @return 用户信息Map
     */
    private Map<String, CommentUserInfo> getUserMap(List<BugComment> bugComments) {
        List<String> userIds = new ArrayList<>();
        userIds.addAll(bugComments.stream().map(BugComment::getCreateUser).toList());
        userIds.addAll(bugComments.stream().map(BugComment::getReplyUser).toList());
        bugComments.forEach(bugComment -> {
            if (StringUtils.isNotBlank(bugComment.getNotifier())) {
                userIds.addAll(Arrays.asList(bugComment.getNotifier().split(";")));
            }
        });
        List<String> distinctIds = userIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
        List<CommentUserInfo> commentUserInfos = baseUserMapper.getCommentUserInfoByIds(distinctIds);
        return commentUserInfos.stream().collect(Collectors.toMap(CommentUserInfo::getId, v -> v));
    }
}
