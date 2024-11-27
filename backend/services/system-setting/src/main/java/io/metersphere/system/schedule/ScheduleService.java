package io.metersphere.system.schedule;

import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.request.ScheduleConfig;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private ApiScheduleNoticeService apiScheduleNoticeService;

    public void addSchedule(Schedule schedule) {
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        schedule.setNum(getNextNum(schedule.getProjectId()));
        scheduleMapper.insert(schedule);
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.TASK);
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
            return schedules.getFirst();
        }
        return null;
    }

    public int deleteByResourceId(String scenarioId, JobKey jobKey, TriggerKey triggerKey) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(scenarioId);

        scheduleManager.removeJob(jobKey, triggerKey);
        return scheduleMapper.deleteByExample(scheduleExample);
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
        if (BooleanUtils.isTrue(enable) && StringUtils.isNotBlank(cronExpression)) {
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

    public List<String> updateIfExist(String resourceId, boolean enable, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andJobEqualTo(clazz.getName());
        List<Schedule> scheduleList = scheduleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            Schedule schedule = scheduleList.getFirst();
            if (!schedule.getEnable().equals(enable)) {
                schedule.setEnable(enable);
                schedule.setUpdateTime(System.currentTimeMillis());
                scheduleMapper.updateByExampleSelective(schedule, example);
                apiScheduleNoticeService.sendScheduleNotice(schedule, operator);
                if (enable) {
                    scheduleManager.addCronJob(jobKey, triggerKey, clazz, schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getCreateUser()));
                } else {
                    scheduleManager.removeJob(jobKey, triggerKey);
                }
            }
        }
        return scheduleList.stream().map(Schedule::getResourceId).toList();
    }

    public String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        Schedule schedule;
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(scheduleConfig.getResourceId()).andJobEqualTo(clazz.getName());
        List<Schedule> scheduleList = scheduleMapper.selectByExample(example);
        boolean needSendNotice = false;
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            needSendNotice = !scheduleList.getFirst().getEnable().equals(scheduleConfig.getEnable());
            schedule = scheduleConfig.genCronSchedule(scheduleList.getFirst());
            schedule.setUpdateTime(System.currentTimeMillis());
            schedule.setJob(clazz.getName());
            scheduleMapper.updateByExampleSelective(schedule, example);
        } else {
            schedule = scheduleConfig.genCronSchedule(null);
            schedule.setJob(clazz.getName());
            schedule.setId(IDGenerator.nextStr());
            schedule.setCreateUser(operator);
            schedule.setCreateTime(System.currentTimeMillis());
            schedule.setUpdateTime(System.currentTimeMillis());
            schedule.setNum(getNextNum(scheduleConfig.getProjectId()));
            scheduleMapper.insert(schedule);
        }
        //通知
        if ((CollectionUtils.isEmpty(scheduleList) && BooleanUtils.isTrue(scheduleConfig.getEnable()))
                || needSendNotice) {
            apiScheduleNoticeService.sendScheduleNotice(schedule, operator);
        }


        JobDataMap jobDataMap = scheduleManager.getDefaultJobDataMap(schedule, scheduleConfig.getCron(), schedule.getCreateUser());

        /*
        scheduleManager.modifyCronJobTime方法如同它的方法名所说，只能修改定时任务的触发时间。
        如果定时任务的配置数据jobData发生了变化，上面方法是无法更新配置数据的。
        所以，如果配置数据发生了变化，做法就是先删除运行中的定时任务，再重新添加定时任务。

        以上的更新逻辑配合 enable 开关，可以简化为下列写法：
         */
        scheduleManager.removeJob(jobKey, triggerKey);
        if (BooleanUtils.isTrue(schedule.getEnable())) {
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, scheduleConfig.getCron(), jobDataMap);
        }
        return schedule.getId();
    }
}
