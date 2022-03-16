package io.metersphere.notice.service;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.*;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class NoticeSendService {
    @Resource
    private MailNoticeSender mailNoticeSender;
    @Resource
    private WeComNoticeSender weComNoticeSender;
    @Resource
    private DingNoticeSender dingNoticeSender;
    @Resource
    private LarkNoticeSender larkNoticeSender;
    @Resource
    private NoticeService noticeService;
    @Resource
    private InSiteNoticeSender inSiteNoticeSender;


    private AbstractNoticeSender getNoticeSender(MessageDetail messageDetail) {
        AbstractNoticeSender noticeSender = null;
        switch (messageDetail.getType()) {
            case NoticeConstants.Type.EMAIL:
                noticeSender = mailNoticeSender;
                break;
            case NoticeConstants.Type.WECHAT_ROBOT:
                noticeSender = weComNoticeSender;
                break;
            case NoticeConstants.Type.NAIL_ROBOT:
                noticeSender = dingNoticeSender;
                break;
            case NoticeConstants.Type.LARK:
                noticeSender = larkNoticeSender;
                break;
            case NoticeConstants.Type.IN_SITE:
                noticeSender = inSiteNoticeSender;
                break;
            default:
                break;
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
            List<MessageDetail> messageDetails = noticeService.searchMessageByTypeAndProjectId(taskType, projectId);

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        this.getNoticeSender(m).send(m, n);
                    });

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * jenkins 和定时任务触发的发送
     */
    @Async
    public void send(String triggerMode, String taskType, NoticeModel noticeModel) {
        // api和定时任务调用不排除自己
        noticeModel.setExcludeSelf(false);
        try {
            List<MessageDetail> messageDetails = new ArrayList<>();

            if (StringUtils.equals(triggerMode, NoticeConstants.Mode.SCHEDULE)) {
                switch (taskType) {
                    case NoticeConstants.TaskType.API_AUTOMATION_TASK:
                    case NoticeConstants.TaskType.PERFORMANCE_TEST_TASK:
                    default:
                        break;
                }
                messageDetails = noticeService.searchMessageByTestId(noticeModel.getTestId());
            }

            if (StringUtils.equals(triggerMode, NoticeConstants.Mode.API)) {
                String projectId = (String) noticeModel.getParamMap().get("projectId");
                messageDetails = noticeService.searchMessageByTypeAndProjectId(NoticeConstants.TaskType.JENKINS_TASK, projectId);
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
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 后台触发的发送，没有session
     */
    @Async
    public void send(Project project, String taskType, NoticeModel noticeModel) {
        try {
            List<MessageDetail> messageDetails;
//            switch (taskType) {
//                case NoticeConstants.Mode.API:
//                    String projectId = (String) noticeModel.getParamMap().get("projectId");
//                    messageDetails = noticeService.searchMessageByTypeBySend(NoticeConstants.TaskType.JENKINS_TASK, projectId);
//                    break;
//                case NoticeConstants.Mode.SCHEDULE:
//                    messageDetails = noticeService.searchMessageByTestId(noticeModel.getTestId());
//                    break;
//                default:
//                    break;
//            }
            messageDetails = noticeService.searchMessageByTypeAndProjectId(taskType, project.getId());

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        MessageDetail m = SerializationUtils.clone(messageDetail);
                        NoticeModel n = SerializationUtils.clone(noticeModel);
                        this.getNoticeSender(m).send(m, n);
                    });

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }
}
