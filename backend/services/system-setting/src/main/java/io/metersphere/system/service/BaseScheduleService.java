package io.metersphere.system.service;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.schedule.ScheduleManager;
import jakarta.annotation.Resource;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;

    public void startEnableSchedules() {
        List<Schedule> Schedules = getSchedule();
        Schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    LogUtils.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey()),
                            new TriggerKey(schedule.getKey()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getCreateUser()));
                } else {
                    // 删除关闭的job
                    removeJob(schedule.getKey());
                }
            } catch (Exception e) {
                LogUtils.error("初始化任务失败", e);
            }
        });

    }

    private List<Schedule> getSchedule() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria();
        return scheduleMapper.selectByExample(example);
    }

    private void removeJob(String key) {
        scheduleManager.removeJob(new JobKey(key), new TriggerKey(key));
    }
}
