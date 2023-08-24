package io.metersphere.sdk.sechedule;

import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = Exception.class)
public class BaseScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        scheduleMapper.insert(schedule);
    }


    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    public Schedule getScheduleByResource(String resourceId, String job) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andJobEqualTo(job);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(schedules)) {
            return schedules.get(0);
        }
        return null;
    }

    public int deleteByResourceId(String resourceId, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        removeJob(resourceId, group);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    public int deleteByResourceIds(List<String> resourceIds, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdIn(resourceIds);
        for (String resourceId : resourceIds) {
            removeJob(resourceId, group);
        }
        return scheduleMapper.deleteByExample(scheduleExample);
    }


    public int deleteByProjectId(String projectId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andProjectIdEqualTo(projectId);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        schedules.forEach(item -> {
            removeJob(item.getKey(), item.getJob());
        });
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    private void removeJob(String key, String job) {
        scheduleManager.removeJob(new JobKey(key, job), new TriggerKey(key, job));
    }
}
