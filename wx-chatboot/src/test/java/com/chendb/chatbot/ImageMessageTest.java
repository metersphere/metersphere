package com.chendb.chatbot;

import com.chendb.commons.wx.chatbot.SendResult;
import com.chendb.commons.wx.chatbot.WxChatbotClient;
import com.chendb.commons.wx.chatbot.message.ImageMessage;
import com.chendb.commons.wx.chatbot.utils.Base64Utils;
import com.chendb.commons.wx.chatbot.utils.dto.ImageBase64Md5;
import org.junit.Test;

/**
 */
public class ImageMessageTest {


    @Test
    public void imageMessageTest() throws Exception {
    	
    	String string = "https://t8.baidu.com/it/u=3775602298,2697846462&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1595834379&t=f2fc08916bdec3f7b3b8c0e018d04ae6";
		ImageBase64Md5  image = Base64Utils.ImageToBase64ByOnline(string);
		ImageMessage imageMessage = new ImageMessage(image.getBase64(),image.getMd5());
        SendResult result = WxChatbotClient.send(TestConfig.CHATBOT_WEBHOOK, imageMessage);
        System.out.println(result);
    }
}