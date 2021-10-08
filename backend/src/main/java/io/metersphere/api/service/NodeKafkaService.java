package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.JvmInfoDTO;
import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.KafkaProperties;
import io.metersphere.dto.NodeDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class NodeKafkaService {
    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private RestTemplate restTemplate;

    @Value("${spring.kafka.producer.properties.max.request.size}")
    private String maxRequestSize;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private static final String BASE_URL = "http://%s:%d";

    public String createKafkaProducer(RunModeConfig config) {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        try {
            for (JvmInfoDTO jvmInfoDTO : config.getTestResources()) {
                TestResource testResource = jvmInfoDTO.getTestResource();
                String configuration = testResource.getConfiguration();
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                String nodeIp = node.getIp();
                Integer port = node.getPort();

                String uri = String.format(BASE_URL + "/producer/create", nodeIp, port);
                ResponseEntity<String> result = restTemplate.postForEntity(uri, producerProps, String.class);
                if (result.getBody() == null || !"SUCCESS".equals(result.getBody())) {
                    config.getTestResources().remove(jvmInfoDTO);
                }

                //String cUri = String.format(BASE_URL + "/consumer/create", nodeIp, port);
                //restTemplate.postForEntity(cUri, consumerProps, void.class);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return "ERROR";
        }
        return "SUCCESS";
    }
}
