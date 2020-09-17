package com.chendb.chatbot;

import com.chendb.commons.wx.chatbot.SendResult;
import com.chendb.commons.wx.chatbot.WxChatbotClient;
import com.chendb.commons.wx.chatbot.message.MarkdownMessage;
import org.junit.Test;

import java.util.ArrayList;

/**
 */
public class MarkdownMessageTest {


    @Test
    public void testSendMarkdownMessage() throws Exception {
        MarkdownMessage message = new MarkdownMessage();
        message.add("This is a markdown message");
        message.add(MarkdownMessage.getHeaderText(1, "header 1"));
        message.add(MarkdownMessage.getHeaderText(2, "header 2"));
        message.add(MarkdownMessage.getHeaderText(3, "header 3"));
        message.add(MarkdownMessage.getHeaderText(4, "header 4"));
        message.add(MarkdownMessage.getHeaderText(5, "header 5"));
        message.add(MarkdownMessage.getHeaderText(6, "header 6"));

        message.add(MarkdownMessage.getReferenceText("reference text"));
        message.add("\n\n");

        message.add("normal text");
        message.add("\n\n");

        message.add(MarkdownMessage.getBoldText("Bold Text"));
        message.add("\n\n");

        message.add(MarkdownMessage.getItalicText("Italic Text"));
        message.add("\n\n");

        ArrayList<String> orderList = new ArrayList<String>();
        orderList.add("order item1");
        orderList.add("order item2");
        message.add(MarkdownMessage.getOrderListText(orderList));
        message.add("\n\n");

        ArrayList<String> unorderList = new ArrayList<String>();
        unorderList.add("unorder item1");
        unorderList.add("unorder item2");
        message.add(MarkdownMessage.getUnorderListText(unorderList));
        message.add("\n\n");

        message.add(MarkdownMessage.getImageText("http://img01.taobaocdn.com/top/i1/LB1GCdYQXXXXXXtaFXXXXXXXXXX"));
        message.add(MarkdownMessage.getLinkText("This is a link", "https://work.weixin.qq.com/api/doc#90000/90135/91760"));

        SendResult result = WxChatbotClient.send(TestConfig.CHATBOT_WEBHOOK, message);
        System.out.println(result);
    }
    
    @Test
    public void testSendMarkdownMessage1() throws Exception {
    	 MarkdownMessage message = new MarkdownMessage();
			 message.add(MarkdownMessage.getHeaderText(3, "有新用户注册啦"));
			 message.add(MarkdownMessage.getReferenceText("手机号： "+"183XXXXXXXX"));
			 message.add("\n\n");
			 message.add(MarkdownMessage.getReferenceText("用户名： "+"唐三"));
			 message.add("\n\n");
        SendResult result = WxChatbotClient.send(TestConfig.CHATBOT_WEBHOOK, message);
        System.out.println(result);
    }
}