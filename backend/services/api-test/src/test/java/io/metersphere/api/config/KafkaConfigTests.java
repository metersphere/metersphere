package io.metersphere.api.config;

import io.metersphere.sdk.util.LogUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class KafkaConfigTests {

    @Test
    @Order(1)
    public void testProduceConfig() throws Exception {
        Map<String, Object> config = KafkaConfig.getKafkaConfig();
        LogUtils.info(config.get("spring.kafka.producer.properties.compression.type"));
    }

    @Test
    @Order(1)
    public void testConsumerConfig() throws Exception {
        Map<String, Object> config = KafkaConfig.consumerConfig();
        LogUtils.info(config.get("spring.kafka.consumer.properties.decompression.type"));
    }
}
