package io.metersphere.commons.config;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.KafkaProperties;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    public static final String DEBUG_TOPICS_KEY = "MS-API-DEBUG-KEY";

    @Resource
    private KafkaProperties kafkaProperties;

    @Bean
    public NewTopic apiExecTopic() {
        return TopicBuilder.name(KafkaTopicConstants.API_REPORT_TOPIC)
                .build();
    }

    @Bean
    public NewTopic testPlanMessageTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC)
                .build();
    }

    @Bean
    public NewTopic debugTopic() {
        return TopicBuilder.name(KafkaTopicConstants.DEBUG_TOPICS)
                .build();
    }

    private static ProducerFactory<String, String> producerFactory;

    @Autowired
    public KafkaConfig(org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        producerFactory = new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    public static void addDefaultConfig(Map<String, Object> producerProps) {
        Map<String, Object> defaultMap = producerFactory.getConfigurationProperties();
        if (MapUtils.isNotEmpty(defaultMap)) {
            defaultMap.forEach((k, v) -> producerProps.putIfAbsent(k, v));
        }
    }

    public static Map<String, Object> getKafka() {
        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaProperties.getMaxRequestSize());
        producerProps.put(DEBUG_TOPICS_KEY, KafkaTopicConstants.DEBUG_TOPICS);

        // 加入默认配置
        addDefaultConfig(producerProps);
        return producerProps;
    }

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaProperties.getMaxRequestSize());
        // 加入默认配置
        addDefaultConfig(producerProps);
        // 批量一次最大拉取数据量
        producerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        // 消费者每次去kafka拉取数据最大间隔，服务端会认为消费者已离线。触发reBalance
        producerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000);
        // 心跳检查
        producerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000);
        // 手动提交 配置 false
        producerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        producerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        producerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);

        producerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        producerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return producerProps;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs()));
        //并发数量
        factory.setConcurrency(1);
        //开启批量监听
        factory.setBatchListener(true);

        factory.getContainerProperties().setPollTimeout(5000L);

        //设置提交偏移量的方式，
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}
