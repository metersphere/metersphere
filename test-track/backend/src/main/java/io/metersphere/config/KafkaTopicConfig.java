package io.metersphere.config;

import io.metersphere.commons.constants.KafkaTopicConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.partitions:3}")
    private Integer partitions;
    @Value("${kafka.replicas:1}")
    private Integer replicas;

    @Bean
    public NewTopic testPlanDeletedTopic() {
        return TopicBuilder.name(KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
