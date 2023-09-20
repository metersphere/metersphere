package io.metersphere.system.sechedule;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import io.metersphere.system.uid.UUID;

@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

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

    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (Optional.ofNullable(enable).isPresent() && StringUtils.isNotBlank(cronExpression)) {
            try {
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, request.getCreateUser()));
            } catch (SchedulerException e) {
                throw new MSException("定时任务开启异常: " + e.getMessage());
            }
        } else {
            try {
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                throw new MSException("定时任务关闭异常: " + e.getMessage());
            }
        }
    }
}
