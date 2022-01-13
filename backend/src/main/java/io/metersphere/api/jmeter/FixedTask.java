package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.utils.LoggerUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FixedTask {
    private ApiExecutionQueueService queueService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void execute() {
        if (queueService == null) {
            queueService = CommonBeanFactory.getBean(ApiExecutionQueueService.class);
        }
        LoggerUtil.info("进入超时处理");
        queueService.timeOut();
    }
}
