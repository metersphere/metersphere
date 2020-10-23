package io.metersphere.notice.service;

import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.notice.message.TextMessage;
import io.metersphere.notice.util.SendResult;
import io.metersphere.notice.util.WxChatbotClient;
import io.metersphere.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class WxChatTaskService {
    @Resource
    private UserService userService;

    public void sendWechatRobot(MessageDetail messageDetail, List<String> userIds, String context, String eventType) {
        List<String> addresseeIdList = new ArrayList<>();
            if (StringUtils.equals(eventType, messageDetail.getEvent())) {
                messageDetail.getUserIds().forEach(u -> {
                    if (!StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.FOUNDER, u) && !StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                        addresseeIdList.add(u);
                    }else{
                        addresseeIdList.addAll(userIds);
                    }
                });
                enterpriseWechatTask(context, addresseeIdList, messageDetail.getWebhook());
            }
    }

    public void enterpriseWechatTask(String context, List<String> userIds, String Webhook) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        TextMessage message = new TextMessage(context);
        List<String> mentionedMobileList = new ArrayList<>();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> {
            phoneList.add(u.getPhone());
        });
        mentionedMobileList.addAll(phoneList);
        message.setMentionedMobileList(mentionedMobileList);
        try {
            SendResult result = WxChatbotClient.send(Webhook, message);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
