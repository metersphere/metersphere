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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DingTaskService {
    @Resource
    private UserService userService;

    public void sendNailRobot(MessageDetail messageDetail, List<String> userIds, String context, String eventType) {
        List<String> addresseeIdList = new ArrayList<>();
            if (StringUtils.equals(eventType, messageDetail.getEvent())) {
                messageDetail.getUserIds().forEach(u -> {
                    if (!StringUtils.equals(NoticeConstants.EXECUTOR, u) && !StringUtils.equals(NoticeConstants.FOUNDER, u) && !StringUtils.equals(NoticeConstants.MAINTAINER, u)) {
                        addresseeIdList.add(u);
                    }else{
                        addresseeIdList.addAll(userIds);
                    }

                });
                sendDingTask(context, addresseeIdList, messageDetail.getWebhook());
            }
    }

    public void sendDingTask(String context, List<String> userIds, String Webhook) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
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
