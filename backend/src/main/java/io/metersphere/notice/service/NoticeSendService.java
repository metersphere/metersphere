package io.metersphere.notice.service;

import com.alibaba.nacos.client.utils.StringUtils;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.DingNoticeSender;
import io.metersphere.notice.sender.impl.LarkNoticeSender;
import io.metersphere.notice.sender.impl.MailNoticeSender;
import io.metersphere.notice.sender.impl.WeComNoticeSender;
import io.metersphere.service.UserService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    private NotificationService notificationService;
    @Resource
    private UserService userService;

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
            QueryOrgMemberRequest request = new QueryOrgMemberRequest();
            request.setOrganizationId(SessionUtils.getCurrentOrganizationId());
            List<User> orgAllMember = userService.getOrgAllMember(request);


            // 异步发送实体通知
            messageDetails.stream()
                    .filter(messageDetail -> StringUtils.equals(messageDetail.getEvent(), noticeModel.getEvent()))
                    .forEach(messageDetail -> this.getNoticeSender(messageDetail).send(messageDetail, noticeModel));

            // 异步发送站内通知
            sendAnnouncement(noticeModel, orgAllMember);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Async
    public void sendAnnouncement(NoticeModel noticeModel, List<User> orgAllMember) {
        // 替换变量
        noticeModel.setContext(getContent(noticeModel));
        orgAllMember.forEach(receiver -> {
            String context = noticeModel.getContext();
            LogUtil.debug("发送站内通知: {}, 内容: {}", receiver.getName(), context);
            notificationService.sendAnnouncement(noticeModel.getSubject(), context, receiver.getId());
        });
    }

    private String getContent(NoticeModel noticeModel) {
        String template = noticeModel.getContext();
        Map<String, Object> paramMap = noticeModel.getParamMap();
        if (MapUtils.isNotEmpty(paramMap)) {
            for (String k : paramMap.keySet()) {
                if (paramMap.get(k) != null) {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", paramMap.get(k).toString());
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "");
                }
            }
        }
        return template;
    }
}
