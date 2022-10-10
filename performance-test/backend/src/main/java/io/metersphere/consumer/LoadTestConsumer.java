package io.metersphere.consumer;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestConsumer {
    public static final String CONSUME_ID = "load-test-data";


    @KafkaListener(id = CONSUME_ID, topics = "${kafka.test.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LoadTestReport loadTestReport = JSON.parseObject(record.value(), LoadTestReport.class);

        Map<String, LoadTestFinishEvent> subTypes = CommonBeanFactory.getBeansOfType(LoadTestFinishEvent.class);
        subTypes.forEach((k, t) -> {
            try {
                t.execute(loadTestReport);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }
}
