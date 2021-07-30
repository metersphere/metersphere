package io.metersphere.websocket.c.to.c;

import com.alibaba.fastjson.JSON;
import io.metersphere.websocket.c.to.c.util.MsgDto;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class MsWebSocketClient extends WebSocketClient{
    public MsWebSocketClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        System.out.println("握手...");
        for(Iterator<String> it=shake.iterateHttpFields();it.hasNext();) {
            String key = it.next();
            System.out.println(key+":"+shake.getFieldValue(key));
        }
    }

    @Override
    public void onMessage(String paramString) {
        System.out.println("接收到消息："+paramString);
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
        System.out.println("关闭...");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("异常"+e);
    }
    public static void main(String[] args) {
        try {
            MsWebSocketClient client = new MsWebSocketClient("ws://127.0.0.1:8081/ws/22222");
            client.connect();
            System.out.println("建立websocket连接");
            MsgDto dto = new MsgDto();
            dto.setContent("099991023123123");
            dto.setReportId("123123123");
            dto.setToReport("3933abd9");
            client.send(JSON.toJSONString(dto));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
