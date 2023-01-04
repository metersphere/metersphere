package io.metersphere.websocket;

import io.metersphere.commons.utils.LogUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class MsWebSocketClient extends WebSocketClient {
    public MsWebSocketClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        LogUtil.info("握手...");
        for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext(); ) {
            String key = it.next();
            LogUtil.info(key + ":" + shake.getFieldValue(key));
        }
    }

    @Override
    public void onMessage(String paramString) {
        LogUtil.info("接收到消息：" + paramString);
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
        LogUtil.info("关闭...");
    }

    @Override
    public void onError(Exception e) {
        LogUtil.info("异常" + e);
    }
}
