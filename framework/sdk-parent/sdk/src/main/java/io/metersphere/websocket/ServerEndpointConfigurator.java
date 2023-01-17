package io.metersphere.websocket;

import io.metersphere.commons.utils.SessionUtils;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class ServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 将用户信息存储到socket的配置里
        System.out.println(SessionUtils.getUser());
        sec.getUserProperties().put("user", SessionUtils.getUser());
        super.modifyHandshake(sec, request, response);
    }
}
