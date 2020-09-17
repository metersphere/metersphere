package com.chendb.chatbot;

import com.chendb.commons.wx.chatbot.SendResult;
import com.chendb.commons.wx.chatbot.WxChatbotClient;
import com.chendb.commons.wx.chatbot.message.NewsArticle;
import com.chendb.commons.wx.chatbot.message.NewsMessage;
import org.junit.Test;

/**
 * 
 */
public class NewsMessageTest {

    private WxChatbotClient client = new WxChatbotClient();

    @Test
    public void testSendNewsMessage() throws Exception {

    	NewsArticle article=new NewsArticle();
    	article.setTitle("测试推送消息--忽略");
    	article.setDescription("测试推送消息--忽略");
    	article.setUrl("https://work.weixin.qq.com/api/doc#90000/90135/91760");
    	article.setPicurl("http://res.mail.qq.com/node/ww/wwopenmng/images/independent/doc/test_pic_msg1.png");
    	
    	NewsMessage message=new NewsMessage();
    	message.addNewsArticle(article);
    	SendResult result = client.send(TestConfig.CHATBOT_WEBHOOK, message);
        System.out.println(result);
    }

}