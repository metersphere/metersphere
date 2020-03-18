package io.metersphere.job;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.ZaleniumService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

@Configuration
@EnableScheduling
public class ZaleniumJob {


    @Resource
    ZaleniumService zaleniumService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncFucTestReport(){
        LogUtil.info("============= start sync FucTestReport =============");
        zaleniumService.syncTestResult();
        LogUtil.info("============= end sync FucTestReport =============");
    }
}
