package io.metersphere.api.socket;

import io.metersphere.sdk.constants.MsgType;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.util.JSON;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class WebSocketHandlerTest {
    @Mock
    private Session session;

    @Mock
    private RemoteEndpoint.Async async;

    @InjectMocks
    private WebSocketHandler webSocketHandler;

    @Test
    @Order(1)
    void testOpenSession() {
        // 模拟参数
        String reportId = "123";
        when(session.getAsyncRemote()).thenReturn(async);

        // 调用被测试方法
        webSocketHandler.openSession(reportId, session);

        // 验证行为
        verify(session).setMaxIdleTimeout(180000);
        // 这里可以添加更多的验证，例如检查 ONLINE_USER_SESSIONS 的状态等
    }

    @Test
    @Order(2)
    void testOnMessage() {
        // 模拟参数
        String reportId = "123";
        String message = JSON.toJSONString(new SocketMsgDTO(reportId, "2", MsgType.CONNECT.name(), "test"));
        when(session.getAsyncRemote()).thenReturn(async);

        // 调用被测试方法
        webSocketHandler.onMessage(reportId, message);
    }

    @Test
    @Order(3)
    void testOnHeartbeatCheck() {
        // 模拟参数
        when(session.getAsyncRemote()).thenReturn(async);

        // 调用被测试方法
        webSocketHandler.heartbeatCheck();
    }

    @Test
    @Order(4)
    void testOnClose() throws IOException {
        // 模拟参数
        String reportId = "123";

        // 调用被测试方法
        webSocketHandler.onClose(reportId, session);

        // 验证行为
        verify(session).close();
        // 这里可以添加更多的验证，例如检查 ONLINE_USER_SESSIONS 的状态等
    }

    @Test
    @Order(5)
    void testOnError() throws IOException {
        // 模拟参数
        when(session.getAsyncRemote()).thenReturn(async);

        // 调用被测试方法
        webSocketHandler.onError(session, new Throwable("Error"));

        // 验证行为
        verify(session).close();
    }

}
