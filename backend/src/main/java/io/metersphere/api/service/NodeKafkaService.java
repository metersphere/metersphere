package io.metersphere.api.service;

import io.metersphere.config.KafkaProperties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class NodeKafkaService {
    @Resource
    private KafkaProperties kafkaProperties;

    @Value("${spring.kafka.producer.properties.max.request.size}")
    private String maxRequestSize;

    public Map<String, Object> getKafka() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
        return producerProps;
    }
}
