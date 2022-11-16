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
    public NewTopic projectDeletedTopic() {
        return TopicBuilder.name(KafkaTopicConstants.PROJECT_DELETED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic projectCreatedTopic() {
        return TopicBuilder.name(KafkaTopicConstants.PROJECT_CREATED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic checkMockEnvTopic() {
        return TopicBuilder.name(KafkaTopicConstants.CHECK_MOCK_ENV_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }


    @Bean
    public NewTopic platformPluginAddTopic() {
        return TopicBuilder.name(KafkaTopicConstants.PLATFORM_PLUGIN_ADD)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic platformPluginDeleteTopic() {
        return TopicBuilder.name(KafkaTopicConstants.PLATFORM_PLUGIN_DELETED)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
