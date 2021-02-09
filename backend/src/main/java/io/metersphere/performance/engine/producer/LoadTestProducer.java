package io.metersphere.performance.engine.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.commons.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoadTestProducer {

    @Value("${kafka.topic}")
    private String topic;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private ObjectMapper objectMapper;

    public void sendMessage(String reportId) {
        Metric metric = new Metric();
        metric.setReportId(reportId);
        metric.setThreadName("tearDown Thread Group"); // 发送停止消息
        try {
            this.kafkaTemplate.send(topic, objectMapper.writeValueAsString(metric));
        } catch (JsonProcessingException e) {
            LogUtil.error("发送停止消息失败", e);
        }
    }
}
