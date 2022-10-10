package io.metersphere.notice.sender.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.taobao.api.ApiException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.notice.sender.AbstractNoticeSender;
import io.metersphere.notice.sender.NoticeModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DingNoticeSender extends AbstractNoticeSender {

    public void sendNailRobot(MessageDetail messageDetail, NoticeModel noticeModel, String context) {
        List<Receiver> receivers = noticeModel.getReceivers();

        DingTalkClient client = new DefaultDingTalkClient(messageDetail.getWebhook());
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("消息通知: \n" + context);
        request.setText(text);
        if (CollectionUtils.isNotEmpty(receivers)) {
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();

            List<String> userIds = receivers.stream()
                    .map(Receiver::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> phoneList = super.getUserDetails(userIds).stream()
                    .map(UserDetail::getPhone)
                    .distinct()
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(phoneList)) {
                LogUtil.info("钉钉收件人地址: {}", userIds);
                at.setAtMobiles(phoneList);
                request.setAt(at);
            }
        }
        try {
            client.execute(request);
        } catch (ApiException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendNailRobot(messageDetail, noticeModel, context);
    }
}
