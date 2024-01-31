package io.metersphere.api.listener;

import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.WebSocketUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 后端调试消息监听器
 */
@Component
public class DebugListener {
    public static final String DEBUG_CONSUME_ID = "MS-API-DEBUG-CONSUME";

    @KafkaListener(id = DEBUG_CONSUME_ID, topics = KafkaTopicConstants.API_REPORT_DEBUG_TOPIC, groupId = DEBUG_CONSUME_ID + "_" + "${random.uuid}")
    public void debugConsume(ConsumerRecord<?, String> record) {
        try {
            LogUtils.info("接收到执行结果：", record.key());
            if (ObjectUtils.isNotEmpty(record.value()) && WebSocketUtils.has(record.key().toString())) {
                SocketMsgDTO dto = JSON.parseObject(record.value(), SocketMsgDTO.class);
                WebSocketUtils.sendMessageSingle(dto);
            }
        } catch (Exception e) {
            LogUtils.error("调试消息推送失败：", e);
        }
    }
}
