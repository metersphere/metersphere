package io.metersphere.listener;

import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApiExecutionQueueListener {
    private ApiExecutionQueueService queueService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void execute() {
        if (queueService == null) {
            queueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
        }
        queueService.defendQueue();
    }
}
