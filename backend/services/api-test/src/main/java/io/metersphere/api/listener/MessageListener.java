package io.metersphere.api.listener;

import io.metersphere.api.event.ApiEventSource;
import io.metersphere.api.service.ApiReportSendNoticeService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.sdk.constants.ApplicationScope;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class MessageListener {
    public static final String MESSAGE_CONSUME_ID = "MS-API-MESSAGE-CONSUME";
    @Resource
    private ApiReportSendNoticeService apiReportSendNoticeService;

    @Resource
    private ApiEventSource apiEventSource;

    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;

    @KafkaListener(id = MESSAGE_CONSUME_ID, topics = KafkaTopicConstants.API_REPORT_TASK_TOPIC, groupId = MESSAGE_CONSUME_ID)
    public void messageConsume(ConsumerRecord<?, String> record) {
        try {
            LogUtils.info("接收到发送通知信息：", record.key());
            if (ObjectUtils.isNotEmpty(record.value())) {
                ApiNoticeDTO dto = JSON.parseObject(record.value(), ApiNoticeDTO.class);
                apiReportSendNoticeService.sendNotice(dto);

                // TODO 通知测试计划处理后续
                LogUtils.info("发送通知给测试计划：", record.key());
                apiEventSource.fireEvent(ApplicationScope.API_TEST, record.value());

                // TODO 串行触发下次执行
                ExecutionQueueDetail detail = apiExecutionQueueService.getNextDetail(dto.getQueueId());
                // TODO 调用执行方法

            }
        } catch (Exception e) {
            LogUtils.error("接收到发送通知信息：", e);
        }
    }
}
