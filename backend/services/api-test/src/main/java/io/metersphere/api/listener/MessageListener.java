package io.metersphere.api.listener;

import io.metersphere.api.service.ApiReportSendNoticeService;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.util.CommonBeanFactory;
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

    @KafkaListener(id = MESSAGE_CONSUME_ID, topics = KafkaTopicConstants.API_REPORT_TASK_TOPIC, groupId = MESSAGE_CONSUME_ID)
    public void messageConsume(ConsumerRecord<?, String> record) {
        try {
            if (apiReportSendNoticeService == null) {
                apiReportSendNoticeService = CommonBeanFactory.getBean(ApiReportSendNoticeService.class);
            }

            LogUtils.info("接收到发送通知信息：", record.key());
            if (ObjectUtils.isNotEmpty(record.value())) {
                ApiNoticeDTO dto = JSON.parseObject(record.value(), ApiNoticeDTO.class);
                apiReportSendNoticeService.sendNotice(dto);
            }
        } catch (Exception e) {
            LogUtils.error("接收到发送通知信息：", e);
        }
    }
}
