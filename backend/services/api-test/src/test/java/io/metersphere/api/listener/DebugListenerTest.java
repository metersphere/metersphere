package io.metersphere.api.listener;

import io.metersphere.api.socket.WebSocketHandler;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.constants.MsgType;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.util.WebSocketUtils;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class DebugListenerTest {

    @InjectMocks
    private DebugListener debugListener;
    @Mock
    private Session session;
    @InjectMocks
    private WebSocketHandler webSocketHandler;

    @Mock
    private RemoteEndpoint.Async async;

    @Test
    void testDebugConsume() {
        testOpenSession();
        // 模拟参数
        ConsumerRecord<Object, String> record = new ConsumerRecord<>(KafkaTopicConstants.API_REPORT_DEBUG_TOPIC, 0, 0, "123", "value");
        // 调用被测试方法
        debugListener.debugConsume(record);
        String reportId = "123";
        WebSocketUtils.sendMessageSingle(new SocketMsgDTO(reportId, "2", MsgType.CONNECT.name(), "test"));
    }

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
}
