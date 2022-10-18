package io.metersphere.service.ext;

import io.metersphere.api.dto.ScheduleRequest;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ScheduleMapper;
import io.metersphere.base.mapper.SwaggerUrlProjectMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.ExtScheduleMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.schedule.ScheduleReference;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.QueryScheduleRequest;
import io.metersphere.sechedule.ApiScenarioTestJob;
import io.metersphere.sechedule.ScheduleManager;
import io.metersphere.sechedule.SwaggerUrlImportJob;
import io.metersphere.service.ServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExtApiScheduleService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private ExtScheduleMapper extScheduleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SwaggerUrlProjectMapper swaggerUrlProjectMapper;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        scheduleMapper.insert(schedule);
    }

    public void addSwaggerUrlSchedule(SwaggerUrlProject swaggerUrlProject) {
        swaggerUrlProjectMapper.insert(swaggerUrlProject);
    }

    public void updateSwaggerUrlSchedule(SwaggerUrlProject swaggerUrlProject) {
        swaggerUrlProjectMapper.updateByPrimaryKeyWithBLOBs(swaggerUrlProject);
    }

    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
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
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        ScheduleExample example = new ScheduleExample();
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

    public int deleteByProjectId(String projectId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andProjectIdEqualTo(projectId);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        schedules.forEach(item -> {
            removeJob(item.getKey(), item.getGroup());
            swaggerUrlProjectMapper.deleteByPrimaryKey(item.getResourceId());
        });
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

    private void removeJob(String resourceId, String group) {
        if (StringUtils.equals(ScheduleGroup.API_SCENARIO_TEST.name(), group)) {
            scheduleManager.removeJob(ApiScenarioTestJob.getJobKey(resourceId), ApiScenarioTestJob.getTriggerKey(resourceId));
        } else if (StringUtils.equals(ScheduleGroup.SWAGGER_IMPORT.name(), group)) {
            scheduleManager.removeJob(SwaggerUrlImportJob.getJobKey(resourceId), SwaggerUrlImportJob.getTriggerKey(resourceId));
        }
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
                    LogUtil.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()), new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(), scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getUserId()));
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
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, request.getValue(), scheduleManager.getDefaultJobDataMap(request, request.getValue(), SessionUtils.getUser().getId()));
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
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression, scheduleManager.getDefaultJobDataMap(request, cronExpression, SessionUtils.getUser().getId()));
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
        return extScheduleMapper.list(request);
    }

    public void build(Map<String, String> resourceNameMap, List<ScheduleDao> schedules) {
        List<String> userIds = schedules.stream().map(Schedule::getUserId).collect(Collectors.toList());
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        Map<String, String> userMap = userMapper.selectByExample(example).stream().collect(Collectors.toMap(User::getId, User::getName));
        schedules.forEach(schedule -> {
            schedule.setResourceName(resourceNameMap.get(schedule.getResourceId()));
            schedule.setUserName(userMap.get(schedule.getUserId()));
        });
    }

    public long countTaskByProjectId(String projectId) {
        return extScheduleMapper.countTaskByProjectId(projectId);
    }

    public long countTaskByProjectIdInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extScheduleMapper.countTaskByProjectIdAndCreateTimeRange(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID, BaseQueryRequest request) {
        List<TaskInfoResult> runningTaskInfoList = extScheduleMapper.findRunningTaskInfoByProjectID(projectID, request);
        return runningTaskInfoList;
    }

    public void createSchedule(ScheduleRequest request) {
        Schedule schedule = this.buildApiTestSchedule(request);
        JobKey jobKey = null;
        TriggerKey triggerKey = null;
        Class clazz = null;
        if ("testPlan".equals(request.getScheduleFrom())) {
            LogUtil.info("testPlan");
        } else {
            //默认为情景
            ApiScenarioWithBLOBs apiScene = apiScenarioMapper.selectByPrimaryKey(request.getResourceId());
            schedule.setName(apiScene.getName());
            schedule.setProjectId(apiScene.getProjectId());
            schedule.setGroup(ScheduleGroup.API_SCENARIO_TEST.name());
            schedule.setType(ScheduleType.CRON.name());
            jobKey = ApiScenarioTestJob.getJobKey(request.getResourceId());
            triggerKey = ApiScenarioTestJob.getTriggerKey(request.getResourceId());
            clazz = ApiScenarioTestJob.class;
            schedule.setJob(ApiScenarioTestJob.class.getName());
        }
        this.addSchedule(schedule);

        this.addOrUpdateCronJob(request, jobKey, triggerKey, clazz);
    }

    public void updateSchedule(Schedule request) {
        JobKey jobKey = null;
        TriggerKey triggerKey = null;
        Class clazz = null;

        //测试计划的定时任务修改会修改任务的配置信息，并不只是单纯的修改定时任务时间。需要重新配置这个定时任务
        boolean needResetJob = false;
        if (ScheduleGroup.SWAGGER_IMPORT.name().equals(request.getGroup())) {
            jobKey = SwaggerUrlImportJob.getJobKey(request.getResourceId());
            triggerKey = SwaggerUrlImportJob.getTriggerKey(request.getResourceId());
            clazz = SwaggerUrlImportJob.class;
            request.setJob(SwaggerUrlImportJob.class.getName());
            needResetJob = true;
        } else {
            //默认为情景
            jobKey = ApiScenarioTestJob.getJobKey(request.getResourceId());
            triggerKey = ApiScenarioTestJob.getTriggerKey(request.getResourceId());
            clazz = ApiScenarioTestJob.class;
            request.setJob(ApiScenarioTestJob.class.getName());
            needResetJob = true;
        }
        this.editSchedule(request);

        if (needResetJob) {
            this.resetJob(request, jobKey, triggerKey, clazz);
        } else {
            this.addOrUpdateCronJob(request, jobKey, triggerKey, clazz);
        }
    }


    public Object getCurrentlyExecutingJobs() {
        return scheduleManager.getCurrentlyExecutingJobs();
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
}
