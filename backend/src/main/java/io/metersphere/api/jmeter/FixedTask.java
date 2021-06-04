package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FixedTask {
    @Scheduled(cron = "*/3 * * * * ?")
    public void execute() {
        ApiScenarioReportService scenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
        if (MessageCache.cache != null && MessageCache.cache.size() > 0) {
            for (String key : MessageCache.cache.keySet()) {
                ReportCounter counter = MessageCache.cache.get(key);
                // 合并
                if (counter.getNumber() == counter.getReportIds().size()) {
                    scenarioReportService.margeReport(key, counter.getReportIds());
                    MessageCache.cache.remove(key);
                }
            }
        }
    }

}
