package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.datacount.response.TaskInfoResult;
import io.metersphere.api.dto.definition.ApiSwaggerUrlDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtScheduleMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.job.sechedule.*;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

    @Resource
    private TestPlanMapper testPlanMapper;
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
        swaggerUrlProjectMapper.updateByPrimaryKeySelective(swaggerUrlProject);
    }

    public ApiSwaggerUrlDTO selectApiSwaggerUrlDTO(String id){
        return extScheduleMapper.select(id);
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

    public int deleteByResourceId(String resourceId, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        removeJob(resourceId, group);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    private void removeJob(String resourceId, String group) {
        if(StringUtils.equals(ScheduleGroup.API_SCENARIO_TEST.name(), group)){
            scheduleManager.removeJob(ApiScenarioTestJob.getJobKey(resourceId), ApiScenarioTestJob.getTriggerKey(resourceId));
        } else if(StringUtils.equals(ScheduleGroup.TEST_PLAN_TEST.name(), group)){
            scheduleManager.removeJob(TestPlanTestJob.getJobKey(resourceId), TestPlanTestJob.getTriggerKey(resourceId));
        } else if(StringUtils.equals(ScheduleGroup.SWAGGER_IMPORT.name(), group)){
            scheduleManager.removeJob(SwaggerUrlImportJob.getJobKey(resourceId), SwaggerUrlImportJob.getTriggerKey(resourceId));
        } else if(StringUtils.equals(ScheduleGroup.PERFORMANCE_TEST.name(), group)){
            scheduleManager.removeJob(PerformanceTestJob.getJobKey(resourceId), PerformanceTestJob.getTriggerKey(resourceId));
        } else {
            scheduleManager.removeJob(ApiTestJob.getJobKey(resourceId), ApiTestJob.getTriggerKey(resourceId));
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
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()),
                            new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getUserId()));
                }
            } catch (Exception e) {
                LogUtil.error("初始化任务失败", e);
                e.printStackTrace();
            }
        });
    }

    public Schedule buildApiTestSchedule(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setResourceId(request.getResourceId());
        schedule.setEnable(true);
        schedule.setValue(request.getValue().trim());
        schedule.setKey(request.getResourceId());
        schedule.setUserId(SessionUtils.getUser().getId());
        schedule.setProjectId(request.getProjectId());
        schedule.setWorkspaceId(request.getWorkspaceId());
        return schedule;
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
        return extScheduleMapper.list(request);
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

    public long countTaskByProjectId(String projectId) {
        return  extScheduleMapper.countTaskByProjectId(projectId);
    }

    public long countTaskByProjectIdInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if(firstTime==null || lastTime == null){
            return  0;
        }else {
            return extScheduleMapper.countTaskByProjectIdAndCreateTimeRange(projectId,firstTime.getTime(),lastTime.getTime());
        }
    }

    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID, BaseQueryRequest request) {
        List<TaskInfoResult> runningTaskInfoList = extScheduleMapper.findRunningTaskInfoByProjectID(projectID, request);
        return  runningTaskInfoList;
    }

    public void createSchedule(ScheduleRequest request) {
        Schedule schedule = this.buildApiTestSchedule(request);


        JobKey jobKey = null;
        TriggerKey triggerKey = null;
        Class clazz = null;
        if("testPlan".equals(request.getScheduleFrom())){
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getResourceId());
            schedule.setName(testPlan.getName());
            schedule.setProjectId(testPlan.getProjectId());
            schedule.setGroup(ScheduleGroup.TEST_PLAN_TEST.name());
            schedule.setType(ScheduleType.CRON.name());
            jobKey = TestPlanTestJob.getJobKey(request.getResourceId());
            triggerKey = TestPlanTestJob.getTriggerKey(request.getResourceId());
            clazz = TestPlanTestJob.class;
            schedule.setJob(TestPlanTestJob.class.getName());
        }else {
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

        this.addOrUpdateCronJob(request,jobKey ,triggerKey , clazz);
    }

    public void updateSchedule(Schedule request) {


        JobKey jobKey = null;
        TriggerKey triggerKey = null;
        Class clazz = null;
        if(ScheduleGroup.TEST_PLAN_TEST.name().equals(request.getGroup())){
            jobKey = TestPlanTestJob.getJobKey(request.getResourceId());
            triggerKey = TestPlanTestJob.getTriggerKey(request.getResourceId());
            clazz = TestPlanTestJob.class;
            request.setJob(TestPlanTestJob.class.getName());
        }else {
            //默认为情景
            jobKey = ApiScenarioTestJob.getJobKey(request.getResourceId());
            triggerKey = ApiScenarioTestJob.getTriggerKey(request.getResourceId());
            clazz = ApiScenarioTestJob.class;
            request.setJob(ApiScenarioTestJob.class.getName());
        }
        this.editSchedule(request);

        this.addOrUpdateCronJob(request,jobKey ,triggerKey , clazz);
    }

    public Object getCurrentlyExecutingJobs() {
        return scheduleManager.getCurrentlyExecutingJobs();
    }
}
