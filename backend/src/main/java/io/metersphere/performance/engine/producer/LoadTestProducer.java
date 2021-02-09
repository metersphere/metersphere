package io.metersphere.performance.engine.producer;

import com.alibaba.fastjson.JSON;
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

    public void sendMessage(String reportId) {
        Metric metric = new Metric();
        metric.setReportId(reportId);
        metric.setThreadName("tearDown Thread Group"); // 发送停止消息
        this.kafkaTemplate.send(topic, JSON.toJSONString(metric));
    }
}
