package io.metersphere.system.service;


import io.metersphere.project.domain.Project;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import io.metersphere.system.notice.sender.impl.*;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class NoticeSendService {
    @Resource
    private MailNoticeSender mailNoticeSender;
    @Resource
    private WeComNoticeSender weComNoticeSender;
    @Resource
    private DingCustomNoticeSender dingCustomNoticeSender;
    @Resource
    private DingEnterPriseNoticeSender dingEnterPriseNoticeSender;
    @Resource
    private LarkNoticeSender larkNoticeSender;
    @Resource
    private InSiteNoticeSender inSiteNoticeSender;
    @Resource
    private WebhookNoticeSender webhookNoticeSender;
    @Resource
    private MessageDetailService messageDetailService;


    private AbstractNoticeSender getNoticeSender(MessageDetail messageDetail) {
        AbstractNoticeSender noticeSender;
        switch (messageDetail.getType()) {
            case NoticeConstants.Type.MAIL -> noticeSender = mailNoticeSender;
            case NoticeConstants.Type.WE_COM -> noticeSender = weComNoticeSender;
            case NoticeConstants.Type.DING_TALK -> noticeSender = getDingSender(messageDetail.getDingType());
            case NoticeConstants.Type.LARK -> noticeSender = larkNoticeSender;
            case NoticeConstants.Type.CUSTOM -> noticeSender = webhookNoticeSender;
            default -> noticeSender = inSiteNoticeSender;
        }
        return noticeSender;
    }

    private AbstractNoticeSender getDingSender(String dingType) {
        if (StringUtils.equalsIgnoreCase(dingType, NoticeConstants.DingType.ENTERPRISE)) {
            return dingEnterPriseNoticeSender;
        }else{
            return dingCustomNoticeSender;
        }
    }

    /**
     * 在线操作发送通知
     */
    @Async("threadPoolTaskExecutor")
    public void send(String taskType, NoticeModel noticeModel) {
        setLanguage(noticeModel);
        try {
            String projectId = (String) noticeModel.getParamMap().get("projectId");
            List<MessageDetail> messageDetails = messageDetailService.searchMessageByTypeAndProjectId(taskType, projectId);
            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        try {
                            this.getNoticeSender(m).send(m, n);
                        } catch (Exception e) {
                            LogUtils.error(e);
                        }
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    private static void setLanguage(NoticeModel noticeModel) {
        String language = (String) noticeModel.getParamMap().get("Language");
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase(language,"US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language,"TW")){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }

    public void setLanguage(String language) {
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (StringUtils.containsIgnoreCase(language,"US")) {
            locale = Locale.US;
        } else if (StringUtils.containsIgnoreCase(language,"TW")){
            locale = Locale.TAIWAN;
        }
        LocaleContextHolder.setLocale(locale);
    }

    /**
     * jenkins 和定时任务触发的发送
     */
    @Async("threadPoolTaskExecutor")
    public void sendJenkins(String triggerMode, NoticeModel noticeModel) {
        // api和定时任务调用不排除自己
        noticeModel.setExcludeSelf(false);
        try {
            List<MessageDetail> messageDetails;

            if (StringUtils.equals(triggerMode, NoticeConstants.Mode.SCHEDULE)) {
                messageDetails = messageDetailService.searchMessageByTestId(noticeModel.getTestId());
            } else {
                String projectId = (String) noticeModel.getParamMap().get("projectId");
                messageDetails = messageDetailService.searchMessageByTypeAndProjectId(triggerMode, projectId);
            }

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        try {
                            this.getNoticeSender(m).send(m, n);
                        } catch (Exception e) {
                            LogUtils.error(e);
                        }
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    /**
     * 后台触发的发送，没有session
     */
    @Async("threadPoolTaskExecutor")
    public void send(Project project, String taskType, NoticeModel noticeModel) {
        setLanguage(noticeModel);
        try {
            List<MessageDetail> messageDetails = messageDetailService.searchMessageByTypeAndProjectId(taskType, project.getId());
            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        try {
                            this.getNoticeSender(m).send(m, n);
                        } catch (Exception e) {
                            LogUtils.error(e);
                        }
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    /**
     * 其他类型
     */
    @Async("threadPoolTaskExecutor")
    public void sendOther(String taskType, NoticeModel noticeModel, List<String> users, boolean excludeSelf) {
        //如果在线需要排除自己，也需要选定当前环境选择的语言
        if (excludeSelf) {
            setLanguage(noticeModel);
        }
        // 定时任务调用不排除自己
        noticeModel.setExcludeSelf(excludeSelf);
        try {
            String projectId = (String) noticeModel.getParamMap().get("projectId");
            List<MessageDetail> messageDetails = messageDetailService.searchMessageByTypeAndProjectId(taskType, projectId)
                    .stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .toList();
            if (CollectionUtils.isEmpty(messageDetails)) {
                NoticeModel n = SerializationUtils.clone(noticeModel);
                MessageDetail m = buildMessageTails(taskType, noticeModel, users, projectId);
                inSiteNoticeSender.send(m, n);
            } else {
                // 异步发送通知
                messageDetails.stream()
                        .forEach(messageDetail -> {
                            MessageDetail m = SerializationUtils.clone(messageDetail);
                            if (CollectionUtils.isNotEmpty(users)) {
                                m.getReceiverIds().addAll(users);
                            }
                            NoticeModel n = SerializationUtils.clone(noticeModel);
                            try {
                                this.getNoticeSender(m).send(m, n);
                            } catch (Exception e) {
                                LogUtils.error(e);
                            }
                        });
            }

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    private static MessageDetail buildMessageTails(String taskType, NoticeModel noticeModel, List<String> users, String projectId) {
        MessageDetail m = new MessageDetail();
        Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String defaultSubject = defaultTemplateTitleMap.get(taskType + "_" + noticeModel.getEvent());
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String defaultTemplate = defaultTemplateMap.get(taskType + "_" + noticeModel.getEvent());
        m.setReceiverIds(users);
        m.setProjectId(projectId);
        m.setEvent(noticeModel.getEvent());
        m.setTaskType(taskType);
        m.setTemplate(defaultTemplate);
        m.setSubject(defaultSubject);
        m.setType(NoticeConstants.Type.IN_SITE);
        return m;
    }

}
