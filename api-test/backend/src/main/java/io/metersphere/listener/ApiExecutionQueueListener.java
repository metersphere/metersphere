package io.metersphere.listener;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.service.ApiExecutionQueueService;
import org.springframework.stereotype.Component;

@Component
public class ApiExecutionQueueListener {
    private ApiExecutionQueueService queueService;

    @QuartzScheduled(cron = "0 0/30 0/1 * * ?")
    public void execute() {
        if (queueService == null) {
            queueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
        }
        queueService.defendQueue();
    }
}
