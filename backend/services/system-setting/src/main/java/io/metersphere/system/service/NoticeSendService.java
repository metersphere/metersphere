package io.metersphere.system.service;


import io.metersphere.project.domain.Project;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.notice.sender.impl.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import io.metersphere.system.notice.NoticeModel;


import java.util.ArrayList;
import java.util.List;

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
        AbstractNoticeSender noticeSender = null;
        switch (messageDetail.getType()) {
            case NoticeConstants.Type.MAIL -> noticeSender = mailNoticeSender;
            case NoticeConstants.Type.WECOM_ROBOT -> noticeSender = weComNoticeSender;
            case NoticeConstants.Type.DING_CUSTOM_ROBOT -> noticeSender = dingCustomNoticeSender;
            case NoticeConstants.Type.DING_ENTERPRISE_ROBOT -> noticeSender = dingEnterPriseNoticeSender;
            case NoticeConstants.Type.LARK_ROBOT -> noticeSender = larkNoticeSender;
            case NoticeConstants.Type.IN_SITE -> noticeSender = inSiteNoticeSender;
            case NoticeConstants.Type.CUSTOM_WEBHOOK_ROBOT -> noticeSender = webhookNoticeSender;
            default -> {
            }
        }

        return noticeSender;
    }

    /**
     * 在线操作发送通知
     */
    @Async
    public void send(String taskType, NoticeModel noticeModel) {
        try {
            String projectId = (String) noticeModel.getParamMap().get("projectId");
            List<MessageDetail> messageDetails = messageDetailService.searchMessageByTypeAndProjectId(taskType, projectId);
            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        this.getNoticeSender(m).send(m, n);
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    /**
     * jenkins 和定时任务触发的发送
     */
    @Async
    public void sendJenkins(String triggerMode, NoticeModel noticeModel) {
        // api和定时任务调用不排除自己
        noticeModel.setExcludeSelf(false);
        try {
            List<MessageDetail> messageDetails = new ArrayList<>();

            if (StringUtils.equals(triggerMode, NoticeConstants.Mode.SCHEDULE)) {
                messageDetails = messageDetailService.searchMessageByTestId(noticeModel.getTestId());
            }

            if (StringUtils.equals(triggerMode, NoticeConstants.Mode.API)) {
                String projectId = (String) noticeModel.getParamMap().get("projectId");
                messageDetails = messageDetailService.searchMessageByTypeAndProjectId(NoticeConstants.TaskType.JENKINS_TASK, projectId);
            }

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        this.getNoticeSender(m).send(m, n);
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    /**
     * 后台触发的发送，没有session
     */
    @Async
    public void send(Project project, String taskType, NoticeModel noticeModel) {
        try {
            List<MessageDetail> messageDetails  = messageDetailService.searchMessageByTypeAndProjectId(taskType, project.getId());
            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        this.getNoticeSender(m).send(m, n);
                    });

        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }
}
