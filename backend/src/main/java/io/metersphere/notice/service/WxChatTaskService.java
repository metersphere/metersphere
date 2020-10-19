package io.metersphere.notice.service;

import io.metersphere.notice.message.TextMessage;
import io.metersphere.notice.util.SendResult;
import io.metersphere.notice.util.WxChatbotClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WxChatTaskService {
    public void enterpriseWechatTask() {
        TextMessage message = new TextMessage("脸给你打歪");
        List<String> mentionedMobileList = new ArrayList<String>();
        mentionedMobileList.add("15135125273");//@群内成员  手机号
        message.setMentionedMobileList(mentionedMobileList);
        message.setIsAtAll(true);//@所有人
        try {
            SendResult result = WxChatbotClient.send("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=a03acc34-4988-4200-a9c7-7c9b30c5601e", message);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
