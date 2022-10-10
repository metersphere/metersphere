package io.metersphere.listener;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.BaseScheduleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AppStartListener implements ApplicationRunner {

    @Resource
    private BaseScheduleService baseScheduleService;

    @Value("${jmeter.home}")
    private String jmeterHome;
    @Value("${quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock}")
    private String acquireTriggersWithinLock;
    @Value("${quartz.enabled}")
    private boolean quartzEnable;
    @Value("${quartz.scheduler-name}")
    private String quartzScheduleName;
    @Value("${quartz.thread-count}")
    private int quartzThreadCount;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("================= 应用启动 =================");

        System.setProperty("jmeter.home", jmeterHome);

        LogUtil.info("开始启动定时任务。 相关设置：" +
                "quartz.acquireTriggersWithinLock :" + acquireTriggersWithinLock + "\r\n" +
                "quartz.enabled :" + quartzEnable + "\r\n" +
                "quartz.scheduler-name :" + quartzScheduleName + "\r\n" +
                "quartz.thread-count :" + quartzThreadCount + "\r\n"
        );
        baseScheduleService.startEnableSchedules(ScheduleGroup.PERFORMANCE_TEST);
    }
}
