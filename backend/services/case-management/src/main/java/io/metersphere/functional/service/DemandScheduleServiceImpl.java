package io.metersphere.functional.service;

import io.metersphere.functional.job.DemandSyncJob;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.BaseDemandScheduleService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DemandScheduleServiceImpl implements BaseDemandScheduleService {

    @Resource
    private ScheduleService scheduleService;

    @Override
    public void updateDemandSyncScheduleConfig(List<ProjectApplication> bugSyncConfigs, String projectId, String currentUser) {
        List<ProjectApplication> syncCron = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CRON_EXPRESSION.name())).toList();
        List<ProjectApplication> caseEnable = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name())).toList();
        if (CollectionUtils.isNotEmpty(syncCron)) {
            Boolean enable = Boolean.valueOf(caseEnable.getFirst().getTypeValue());
            String typeValue = syncCron.getFirst().getTypeValue();
            Schedule schedule = scheduleService.getScheduleByResource(projectId, DemandSyncJob.class.getName());
            Optional<Schedule> optional = Optional.ofNullable(schedule);
            optional.ifPresentOrElse(s -> {
                s.setValue(typeValue);
                scheduleService.editSchedule(s);
                scheduleService.addOrUpdateCronJob(s, DemandSyncJob.getJobKey(projectId), DemandSyncJob.getTriggerKey(projectId), DemandSyncJob.class);
            }, () -> {
                Schedule request = new Schedule();
                request.setName(Translator.get("demand.sync.job"));
                request.setResourceId(projectId);
                request.setKey(IDGenerator.nextStr());
                request.setProjectId(projectId);
                request.setEnable(enable);
                request.setCreateUser(currentUser);
                request.setType(ScheduleType.CRON.name());
                request.setValue(typeValue);
                request.setJob(DemandSyncJob.class.getName());
                request.setResourceType(ScheduleResourceType.DEMAND_SYNC.name());
                scheduleService.addSchedule(request);
                scheduleService.addOrUpdateCronJob(request, DemandSyncJob.getJobKey(projectId), DemandSyncJob.getTriggerKey(projectId), DemandSyncJob.class);
            });
        }
    }

    @Override
    public void enableOrNotDemandSyncSchedule(String projectId, String currentUser, Boolean enable) {
        Schedule schedule = scheduleService.getScheduleByResource(projectId, DemandSyncJob.class.getName());
        if (schedule != null) {
            schedule.setEnable(enable);
            scheduleService.editSchedule(schedule);
        }
    }
}
