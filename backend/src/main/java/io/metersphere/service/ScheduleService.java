package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.Schedule;
import io.metersphere.base.domain.ScheduleExample;
import io.metersphere.base.mapper.ScheduleMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.job.QuartzManager;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {
    
    @Resource
    private ScheduleMapper scheduleMapper;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        scheduleMapper.insert(schedule);
    }

    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    public Schedule getScheduleByResource(String resourceId, String group) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules.get(0);
        }
        return null;
    }

    public int deleteSchedule(String scheduleId) {
        return scheduleMapper.deleteByPrimaryKey(scheduleId);
    }

    public List<Schedule> listSchedule() {
        ScheduleExample example = new ScheduleExample();
        return scheduleMapper.selectByExample(example);
    }

    public List<Schedule> getEnableSchedule() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andEnableEqualTo(true);
        return scheduleMapper.selectByExample(example);
    }

    public void startEnableSchedules() {
        List<Schedule> Schedules = getEnableSchedule();
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
                LogUtil.error("初始化任务失败", e);
                e.printStackTrace();
            }
        });
    }
}
