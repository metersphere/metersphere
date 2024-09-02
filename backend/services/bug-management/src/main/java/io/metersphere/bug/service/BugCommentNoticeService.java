package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugCommentEditRequest;
import io.metersphere.bug.dto.response.BugCommentNoticeDTO;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.bug.mapper.ExtBugCustomFieldMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugCommentNoticeService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ExtBugCustomFieldMapper extBugCustomFieldMapper;

    /**
     * 获取缺陷通知参数
     * @param request 页面请求参数
     * @return 缺陷通知参数
     */
    public BugCommentNoticeDTO getBugCommentNotice(BugCommentEditRequest request) {
        Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
        BugCommentNoticeDTO bugCommentNoticeDTO = new BugCommentNoticeDTO();
        BeanUtils.copyBean(bugCommentNoticeDTO, bug);
        setNotifier(request, bugCommentNoticeDTO);
        setCustomField(bugCommentNoticeDTO);
        return bugCommentNoticeDTO;
    }

    /**
     * 发送缺陷通知
     */
    @Async
    public void sendNotice(String event, BugCommentNoticeDTO noticeDTO, String currentUser) {
        User user = userMapper.selectByPrimaryKey(currentUser);
        setLanguage(user.getLanguage());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TaskType.BUG_TASK + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TaskType.BUG_TASK + "_" + event);
        BeanMap beanMap = new BeanMap(noticeDTO);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(currentUser)
                .context(template)
                .subject(subject)
                .paramMap(paramMap)
                .event(event)
                .status((String) paramMap.get("status"))
                .excludeSelf(true)
                .relatedUsers(getRelateUser(noticeDTO.getNotifier()))
                .build();
        noticeSendService.send(NoticeConstants.TaskType.BUG_TASK, noticeModel);
    }

    /**
     * 设置本地语言
     * @param language 语言
     */
    private static void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase(language, "US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language, "TW")){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }

    /**
     * 设置缺陷通知的自定义字段值
     * @param bugCommentNoticeDTO 缺陷通知参数
     */
    private void setCustomField(BugCommentNoticeDTO bugCommentNoticeDTO) {
        List<BugCustomFieldDTO> bugCustomFields = extBugCustomFieldMapper.getBugExistCustomFields(List.of(bugCommentNoticeDTO.getId()), bugCommentNoticeDTO.getProjectId());
        if (CollectionUtils.isNotEmpty(bugCustomFields)) {
            List<OptionDTO> fields = bugCustomFields.stream().map(field -> new OptionDTO(field.getName(), field.getValue())).toList();
            bugCommentNoticeDTO.setFields(fields);
        }
    }

    /**
     * 评论通知@用户处理与功能用例保持一致即可, 根据事件类型设置通知人
     * 如果是REPLAY事件, 需要判断有无@的人, 如果有@的人且和当前被回复的人不是同一人, 这里只通知被回复的人; 如果是同一人, 这里通知人为空, 走AT事件
     * 如果不是REPLAY事件，需要判断有无被回复的人，如果被回复的人不在被@人里，则用页面参数传递的通知人，如果在，则排除这个人,如果没有被回复的人，用页面数据
     *
     * @param request 页面请求参数
     * @param bugCommentNoticeDTO 缺陷通知参数
     */
    private void setNotifier(BugCommentEditRequest request, BugCommentNoticeDTO bugCommentNoticeDTO) {
        String notifier = request.getNotifier();
        String replyUser = request.getReplyUser();
        if (StringUtils.equals(request.getEvent(), NoticeConstants.Event.REPLY)) {
            // REPLAY事件, 只通知回复人(且不为空);
            bugCommentNoticeDTO.setNotifier(replyUser);
        } else {
            // AT事件, 如果有回复人, 直接排除;
            if (StringUtils.isNotBlank(replyUser) && StringUtils.isNotBlank(notifier)) {
                List<String> notifierList = new ArrayList<>(Arrays.asList(notifier.split(";")));
                if (notifierList.contains(replyUser)) {
                    notifierList.remove(replyUser);
                    bugCommentNoticeDTO.setNotifier(String.join(";", notifierList));
                } else {
                    bugCommentNoticeDTO.setNotifier(notifier);
                }
            } else {
                bugCommentNoticeDTO.setNotifier(notifier);
            }
        }
    }

    /**
     * 根据通知人分隔的字符串获取参数
     * @return 通知人列表
     */
    private List<String> getRelateUser(String notifier) {
        if (StringUtils.isBlank(notifier)) {
            return null;
        }
        return Arrays.asList(notifier.split(";"));
    }
}
