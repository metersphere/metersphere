package io.metersphere.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    // 执行内容监听
    public final static String EXEC_TOPIC = "ms-automation-exec-topic";
    //执行结果回传
    public static final String TOPICS = "ms-api-exec-topic";

    @Bean
    public NewTopic apiExecTopic() {
        return TopicBuilder.name(TOPICS)
                .build();
    }

    @Bean
    public NewTopic automationTopic() {
        return TopicBuilder.name(EXEC_TOPIC)
                .build();
    }
}
