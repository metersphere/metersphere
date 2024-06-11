package io.metersphere.system.service;

import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.mapper.ExtScheduleMapper;
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
    @Resource
    private ExtScheduleMapper extScheduleMapper;

    public void startEnableSchedules() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria();
        long count = scheduleMapper.countByExample(example);
        long pages = Double.valueOf(Math.ceil(count / 100.0)).longValue();
        for (int i = 0; i < pages; i++) {
            int start = i * 100;
            List<Schedule> schedules = extScheduleMapper.getScheduleByLimit(start, 100);
            doHandleSchedule(schedules);
        }
    }

    private void doHandleSchedule(List<Schedule> schedules) {
        List<String> resourceTypes = List.of(ScheduleResourceType.API_IMPORT.name(), ScheduleResourceType.API_SCENARIO.name(), ScheduleResourceType.TEST_PLAN.name(), ScheduleResourceType.BUG_SYNC.name());
        schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    if (resourceTypes.contains(schedule.getResourceType())) {
                        // 删除关闭的job
                        removeJob(schedule);
                    }
                    LogUtils.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getJob()),
                            new TriggerKey(schedule.getKey(), schedule.getJob()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getCreateUser()));
                } else {
                    // 删除关闭的job
                    removeJob(schedule);
                }
            } catch (Exception e) {
                LogUtils.error("初始化任务失败", e);
            }
        });
    }

    private void removeJob(Schedule schedule) {
        scheduleManager.removeJob(new JobKey(schedule.getKey(), schedule.getJob()), new TriggerKey(schedule.getKey(), schedule.getJob()));
    }
}
