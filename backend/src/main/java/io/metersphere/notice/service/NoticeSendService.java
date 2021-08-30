package io.metersphere.notice.service;

import com.alibaba.nacos.client.utils.StringUtils;
import io.metersphere.base.domain.Organization;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    public void send(String taskType, NoticeModel noticeModel) {
        try {
            List<MessageDetail> messageDetails;
            switch (taskType) {
                case NoticeConstants.Mode.API:
                    String loadReportId = (String) noticeModel.getParamMap().get("id");
                    messageDetails = noticeService.searchMessageByTypeBySend(NoticeConstants.TaskType.JENKINS_TASK, loadReportId);
                    break;
                case NoticeConstants.Mode.SCHEDULE:
                    messageDetails = noticeService.searchMessageByTestId(noticeModel.getTestId());
                    break;
                default:
                    messageDetails = noticeService.searchMessageByType(taskType);
                    break;
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

    public void send(Organization organization, String taskType, NoticeModel noticeModel) {
        try {
            List<MessageDetail> messageDetails = noticeService.searchMessageByTypeAndOrganizationId(taskType, organization.getId());

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
