package io.metersphere.performance.engine.producer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoadTestProducer {
    private static final String SEPARATOR = " ";

    @Value("${kafka.log.topic}")
    private String topic;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String reportId) {
        String[] contents = new String[]{reportId, "none", "0", "Notifying test listeners of end of test"};
        String log = StringUtils.join(contents, SEPARATOR);
        this.kafkaTemplate.send(topic, log);
    }
}
