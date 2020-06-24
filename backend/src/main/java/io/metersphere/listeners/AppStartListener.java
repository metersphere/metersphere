package io.metersphere.listeners;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.Schedule;
import io.metersphere.base.mapper.ScheduleMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.job.QuartzManager;
import io.metersphere.job.sechedule.ApiTestJob;
import io.metersphere.service.ScheduleService;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务启动完成后打印日志
 */
@Component
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {

//    private static boolean started = false;

    @Resource
    private ScheduleService scheduleService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        System.out.println("================= 应用启动 =================");

        try {
            Thread.sleep(5*60*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Schedule> Schedules = scheduleService.getEnableSchedule();

        Schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    LogUtil.error("初始化任务：" + JSON.toJSONString(schedule));
                    QuartzManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()),
                            new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(),
                            QuartzManager.getDefaultJobDataMap(schedule.getResourceId(), schedule.getValue(), schedule.getUserId()));
                }
                Thread.sleep(1*60*1000);
            } catch (Exception e) {
                LogUtil.error("应用启动，初始化任务失败", e);
                e.printStackTrace();
            }
        });
    }
}
