package io.metersphere.notice.service;

import com.alibaba.nacos.client.utils.StringUtils;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.*;
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
    public void send(String taskType, NoticeModel noticeModel) {
        try {
            List<MessageDetail> messageDetails = noticeService.searchMessageByType(taskType);

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        this.getNoticeSender(messageDetail).send(messageDetail, noticeModel);
                    });

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * jenkins 和定时任务触发的发送
     */
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
                messageDetails = noticeService.searchMessageByTypeBySend(NoticeConstants.TaskType.JENKINS_TASK, projectId);
            }

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        this.getNoticeSender(messageDetail).send(messageDetail, noticeModel);
                    });

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 后台触发的发送，没有session
     */
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
            messageDetails = noticeService.searchMessageByTypeAndWorkspaceId(taskType, project.getWorkspaceId());

            // 异步发送通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> {
                        this.getNoticeSender(messageDetail).send(messageDetail, noticeModel);
                    });

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }
}
