package io.metersphere.commons.consumer;

import com.alibaba.fastjson.JSON;
import io.metersphere.Application;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.reflections8.Reflections;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestConsumer {
    public static final String CONSUME_ID = "load-test-data";

    @Resource
    ApiExecutionQueueService apiExecutionQueueService;

    @KafkaListener(id = CONSUME_ID, topics = "${kafka.test.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LoadTestReport loadTestReport = JSON.parseObject(record.value(), LoadTestReport.class);
        Reflections reflections = new Reflections(Application.class);
        Set<Class<? extends LoadTestFinishEvent>> subTypes = reflections.getSubTypesOf(LoadTestFinishEvent.class);
        subTypes.forEach(s -> {
            try {
                CommonBeanFactory.getBean(s).execute(loadTestReport);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
        //删除性能测试在执行队列中的数据 （在测试计划执行中会将性能测试执行添加到执行队列，用于判断整个测试计划到执行进度）
        apiExecutionQueueService.checkExecutionQueneByLoadTest(loadTestReport);
    }
}
