package io.metersphere.service;

import io.metersphere.base.domain.Schedule;
import io.metersphere.base.domain.ScheduleExample;
import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserExample;
import io.metersphere.base.mapper.ScheduleMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.BaseScheduleMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.schedule.ScheduleReference;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.QueryScheduleRequest;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.sechedule.ScheduleManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class)
public class BaseScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private BaseScheduleMapper baseScheduleMapper;
    @Resource
    private UserMapper userMapper;

    private final String API_SCENARIO_JOB = "io.metersphere.sechedule.ApiScenarioTestJob";
    private final String API_SWAGGER_IMPORT_JOB = "io.metersphere.sechedule.SwaggerUrlImportJob";
    private final String TEST_PLAN_JOB = "io.metersphere.plan.job.TestPlanTestJob";

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
        schedule.setCreateTime(null);
        schedule.setUserId(null);
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

    public List<Schedule> getScheduleByResourceIds(List<String> resourceIds, String group) {
        ScheduleExample example = new ScheduleExample();
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        example.createCriteria().andResourceIdIn(resourceIds).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules;
        }
        return new ArrayList<>();
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


    public int deleteByWorkspaceId(String workspaceId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        schedules.forEach(item -> {
            removeJob(item.getResourceId(), item.getGroup());
        });
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    public int deleteByProjectId(String projectId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andProjectIdEqualTo(projectId);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        schedules.forEach(item -> {
            removeJob(item.getKey(), item.getGroup());
        });
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    private void removeJob(String key, String group) {
        scheduleManager.removeJob(new JobKey(key, group), new TriggerKey(key, group));
    }

    public List<Schedule> getScheduleByGroup(ScheduleGroup group) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria()
                .andGroupEqualTo(group.name());
        return scheduleMapper.selectByExample(example);
    }

    private void jobConvert(Schedule schedule, ScheduleGroup group) {
        switch (group) {
            case API_SCENARIO_TEST -> schedule.setJob(API_SCENARIO_JOB);
            case SWAGGER_IMPORT -> schedule.setJob(API_SWAGGER_IMPORT_JOB);
            case TEST_PLAN_TEST -> schedule.setJob(TEST_PLAN_JOB);
        }
    }

    public void startEnableSchedules(ScheduleGroup group) {
        List<Schedule> Schedules = getScheduleByGroup(group);
        Schedules.forEach(schedule -> {
            try {
                // 兼容历史数据
                jobConvert(schedule, group);
                if (schedule.getEnable()) {
                    LogUtil.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()),
                            new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getUserId()));
                } else {
                    // 删除关闭的job
                    removeJob(schedule.getKey(), group.toString());
                }
            } catch (Exception e) {
                LogUtil.error("初始化任务失败", e);
            }
        });
    }

    public Schedule buildApiTestSchedule(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setResourceId(request.getResourceId());
        schedule.setEnable(request.getEnable());
        schedule.setValue(request.getValue().trim());
        schedule.setKey(request.getResourceId());
        schedule.setUserId(SessionUtils.getUser().getId());
        schedule.setProjectId(request.getProjectId());
        schedule.setWorkspaceId(request.getWorkspaceId());
        schedule.setConfig(request.getConfig());
        return schedule;
    }

    public void resetJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        try {
            scheduleManager.removeJob(jobKey, triggerKey);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("重置定时任务-删除旧定时任务时出现异常");
        }
        if (!request.getEnable()) {
            return;
        }
        try {
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, request.getValue(),
                    scheduleManager.getDefaultJobDataMap(request, request.getValue(), SessionUtils.getUser().getId()));
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("重置定时任务-启动新定时任务出现异常");
        }
    }

    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (enable != null && enable && StringUtils.isNotBlank(cronExpression)) {
            try {
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, SessionUtils.getUser().getId()));
            } catch (SchedulerException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException("定时任务开启异常");
            }
        } else {
            try {
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                MSException.throwException("定时任务关闭异常");
            }
        }
    }

    public List<ScheduleDao> list(QueryScheduleRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        request.setOrders(orderList);
        return baseScheduleMapper.list(request);
    }

    public void build(Map<String, String> resourceNameMap, List<ScheduleDao> schedules) {
        List<String> userIds = schedules.stream()
                .map(Schedule::getUserId)
                .collect(Collectors.toList());
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        Map<String, String> userMap = userMapper.selectByExample(example).stream().collect(Collectors.toMap(User::getId, User::getName));
        schedules.forEach(schedule -> {
            schedule.setResourceName(resourceNameMap.get(schedule.getResourceId()));
            schedule.setUserName(userMap.get(schedule.getUserId()));
        });
    }

    public List<Schedule> selectScenarioTaskByProjectId(String projectId, String versionId) {
        return baseScheduleMapper.selectScenarioTaskByProjectId(projectId, versionId);
    }


    public long countTaskByProjectIdInThisWeek(String projectId, String versionId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return baseScheduleMapper.countTaskByProjectIdAndCreateTimeRange(projectId, versionId, firstTime.getTime(), lastTime.getTime());
        }
    }


    public Object getCurrentlyExecutingJobs() {
        return scheduleManager.getCurrentlyExecutingJobs();
    }

    public String getScheduleInfo(String id) {
        ScheduleExample schedule = new ScheduleExample();
        schedule.createCriteria().andResourceIdEqualTo(id);
        List<Schedule> list = scheduleMapper.selectByExample(schedule);
        if (list.size() > 0) {
            return list.get(0).getKey();
        } else {
            return StringUtils.EMPTY;
        }

    }

    public String getLogDetails(String id) {
        Schedule bloB = scheduleMapper.selectByPrimaryKey(id);
        if (bloB != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloB, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloB.getId()), bloB.getProjectId(), bloB.getName(), bloB.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ScheduleRequest request) {
        Schedule bloBs = this.getScheduleByResource(request.getResourceId(), request.getGroup());
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void updateNameByResourceID(String id, String name) {
        baseScheduleMapper.updateNameByResourceID(id, name);
    }
}
