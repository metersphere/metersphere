
//TODO 后续清理（合并完没问题再清理）=====清理报告定时任务只有一个，cron表达式凌晨定时扫描 创建项目不再创建定时任务
/*
package io.metersphere.project.service;

import io.metersphere.project.job.CleanUpReportJob;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.CreateProjectResourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

*/
/**
 * @author wx
 *//*

@Component
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
        request.setResourceType(ScheduleResourceType.CLEAN_REPORT.name());
        scheduleService.addSchedule(request);
        scheduleService.addOrUpdateCronJob(request,
                CleanUpReportJob.getJobKey(projectId),
                CleanUpReportJob.getTriggerKey(projectId),
                CleanUpReportJob.class);
    }
}
*/
