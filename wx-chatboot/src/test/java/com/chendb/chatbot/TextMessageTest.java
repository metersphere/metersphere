package com.chendb.chatbot;

import com.chendb.commons.wx.chatbot.SendResult;
import com.chendb.commons.wx.chatbot.WxChatbotClient;
import com.chendb.commons.wx.chatbot.message.TextMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TextMessageTest {


    @Test
    public void testSendTextMessageWithAtAndAtAll() throws Exception {
        TextMessage message = new TextMessage("测试机器人推送");
        List<String> mentionedMobileList=new ArrayList<String>();
        mentionedMobileList.add("18600967174");//@群内成员  手机号
        message.setMentionedMobileList(mentionedMobileList);
//        message.setIsAtAll(true);//@所有人
        SendResult result = WxChatbotClient.send(TestConfig.CHATBOT_WEBHOOK, message);
        System.out.println(result);
    }
}