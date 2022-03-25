package io.metersphere.config;

import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    //执行结果回传
    public static final String TOPICS = "ms-api-exec-topic";
    @Resource
    private KafkaProperties kafkaProperties;

    @Bean
    public NewTopic apiExecTopic() {
        return TopicBuilder.name(TOPICS)
                .build();
    }

    public static Map<String, Object> getKafka() {
        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaProperties.getMaxRequestSize());
        return producerProps;
    }

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaProperties.getMaxRequestSize());
        // 批量一次最大拉取数据量
        producerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        // 消费者每次去kafka拉取数据最大间隔，服务端会认为消费者已离线。触发reBalance
        producerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000);
        // 心跳检查
        producerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000);
        // 手动提交 配置 false
        producerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        producerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        producerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 20000);

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

        factory.getContainerProperties().setPollTimeout(60000);

        //设置提交偏移量的方式，
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}
