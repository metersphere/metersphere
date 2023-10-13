package io.metersphere.project.service;

import io.metersphere.project.job.CleanUpReportJob;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.sechedule.ScheduleService;
import io.metersphere.system.service.CreateProjectResourceService;
import jakarta.annotation.Resource;

/**
 * @author wx
 */
public class CreateApplicationResourceService implements CreateProjectResourceService {


    @Resource
    private ScheduleService scheduleService;

    @Override
    public void createResources(String projectId) {
        //初始化清理报告定时任务
        Schedule request = new Schedule();
        request.setName("Clean Report Job");
        request.setResourceId(projectId);
        request.setKey(projectId);
        request.setProjectId(projectId);
        request.setEnable(true);
        request.setCreateUser("admin");
        request.setType(ScheduleType.CRON.name());
        // 每天凌晨2点执行清理任务
        request.setValue("0 0 2 * * ?");
        request.setJob(CleanUpReportJob.class.getName());
        scheduleService.addSchedule(request);
        scheduleService.addOrUpdateCronJob(request,
                CleanUpReportJob.getJobKey(projectId),
                CleanUpReportJob.getTriggerKey(projectId),
                CleanUpReportJob.class);
    }
}
