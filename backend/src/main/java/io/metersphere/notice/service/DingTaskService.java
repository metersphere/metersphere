package io.metersphere.notice.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.UserDetail;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DingTaskService {
    @Resource
    private UserService userService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendNailRobot(MessageDetail messageDetail, List<String> userIds, String context, String eventType) {
        List<String> addresseeIdList = new ArrayList<>();
        messageDetail.getEvents().forEach(e -> {
            if (StringUtils.equals(eventType, e)) {
                messageDetail.getUserIds().forEach(u -> {
                    if (!StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                        addresseeIdList.add(u);
                    }
                    if (StringUtils.equals(NoticeConstants.CREATE, eventType) && StringUtils.equals(NoticeConstants.EXECUTOR, u)) {
                        addresseeIdList.addAll(userIds);
                    }
                    if (StringUtils.equals(NoticeConstants.UPDATE, eventType) && StringUtils.equals(NoticeConstants.FOUNDER, u)) {
                        addresseeIdList.addAll(userIds);
                    }
                    if (StringUtils.equals(NoticeConstants.DELETE, eventType) && StringUtils.equals(NoticeConstants.FOUNDER, u)) {
                        addresseeIdList.addAll(userIds);
                    }
                    if (StringUtils.equals(NoticeConstants.COMMENT, eventType) && StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                        addresseeIdList.addAll(userIds);
                    }


                });
                sendDingTask(context, addresseeIdList, messageDetail.getWebhook());
            }
        });
    }

    public void sendDingTask(String context, List<String> userIds, String Webhook) {
        DingTalkClient client = new DefaultDingTalkClient(Webhook);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(context);
        request.setText(text);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        List<UserDetail> list = userService.queryTypeByIds(userIds);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> {
            phoneList.add(u.getPhone());
        });
        at.setAtMobiles(phoneList);
        request.setAt(at);
        OapiRobotSendResponse response = null;
        try {
            response = client.execute(request);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        System.out.println(response.getErrcode());
    }

}
