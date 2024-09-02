package io.metersphere.bug.service;

import com.google.common.collect.Maps;
import io.metersphere.bug.domain.Bug;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.constants.NotificationConstants;
import io.metersphere.system.notice.sender.impl.InSiteNoticeSender;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncNoticeService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private InSiteNoticeSender inSiteNoticeSender;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Async
    public void sendNotice(int total, String currentUser, String language, String triggerMode, String projectId) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        User user = userMapper.selectByPrimaryKey(currentUser);
        setLanguage(user.getLanguage());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TemplateText.BUG_SYNC_TASK_EXECUTE_COMPLETED);
        // ${OPERATOR}同步了${total}条缺陷
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        paramMap.put("total", total);
        paramMap.put("projectId", projectId);
        paramMap.put("Language", language);
        paramMap.put("platform", platformName);
        paramMap.put("triggerMode", triggerMode);
        NoticeModel noticeModel = NoticeModel.builder().operator(currentUser).excludeSelf(false)
                .context(template).subject(subject).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_COMPLETED).build();
        noticeSendService.send(NoticeConstants.TaskType.BUG_SYNC_TASK, noticeModel);
    }

    /**
     * 处理人通知为站内通知
     * @param bug 缺陷
     * @param currentUser 当前用户
     */
    @Async
    public void sendHandleUserNotice(Bug bug, String currentUser) {
        User user = userMapper.selectByPrimaryKey(currentUser);
        setLanguage(user.getLanguage());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String context = defaultTemplateMap.get(NoticeConstants.TemplateText.BUG_TASK_ASSIGN);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(NoticeConstants.TemplateText.BUG_TASK_ASSIGN);
        // ${OPERATOR}给你分配了一个缺陷: ${title}
        Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(8);
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
        paramMap.put("id", bug.getId());
        paramMap.put("title", bug.getTitle());
        paramMap.put("projectId", bug.getProjectId());
        MessageDetail messageDetail = new MessageDetail();
        messageDetail.setProjectId(bug.getProjectId());
        messageDetail.setTaskType(NoticeConstants.TaskType.BUG_TASK);
        NoticeModel noticeModel = NoticeModel.builder().operator(currentUser).excludeSelf(true).receivers(List.of(new Receiver(bug.getHandleUser(), NotificationConstants.Type.SYSTEM_NOTICE.name())))
                .context(context).subject(subject).paramMap(paramMap).event(NoticeConstants.Event.ASSIGN).build();
        inSiteNoticeSender.sendAnnouncement(messageDetail, noticeModel, MessageTemplateUtils.getContent(context, paramMap), subject);
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
}
