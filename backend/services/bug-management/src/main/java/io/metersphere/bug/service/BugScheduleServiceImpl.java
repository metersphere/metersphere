package io.metersphere.bug.service;

import io.metersphere.bug.job.BugSyncJob;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.BaseBugScheduleService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugScheduleServiceImpl implements BaseBugScheduleService {

	@Resource
	private ScheduleService scheduleService;

	@Override
	public void updateBugSyncScheduleConfig(List<ProjectApplication> bugSyncConfigs, String projectId, String currentUser) {
		List<ProjectApplication> syncCron = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.CRON_EXPRESSION.name())).toList();
		List<ProjectApplication> syncEnable = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name())).toList();
		if (CollectionUtils.isNotEmpty(syncCron)) {
			Boolean enable = Boolean.valueOf(syncEnable.get(0).getTypeValue());
			String typeValue = syncCron.get(0).getTypeValue();
			Schedule schedule = scheduleService.getScheduleByResource(projectId, BugSyncJob.class.getName());
			Optional<Schedule> optional = Optional.ofNullable(schedule);
			optional.ifPresentOrElse(s -> {
				s.setEnable(enable);
				s.setValue(typeValue);
				scheduleService.editSchedule(s);
				scheduleService.addOrUpdateCronJob(s, BugSyncJob.getJobKey(projectId), BugSyncJob.getTriggerKey(projectId), BugSyncJob.class);
			}, () -> {
				Schedule request = new Schedule();
				request.setName("Bug Sync Job");
				request.setResourceId(projectId);
				request.setKey(IDGenerator.nextStr());
				request.setProjectId(projectId);
				request.setEnable(enable);
				request.setCreateUser(currentUser);
				request.setType(ScheduleType.CRON.name());
				request.setValue(typeValue);
				request.setJob(BugSyncJob.class.getName());
				request.setResourceType(ScheduleResourceType.BUG_SYNC.name());
				scheduleService.addSchedule(request);
				scheduleService.addOrUpdateCronJob(request, BugSyncJob.getJobKey(projectId), BugSyncJob.getTriggerKey(projectId), BugSyncJob.class);
			});
		}
	}

	@Override
	public void enableOrNotBugSyncSchedule(String projectId, String currentUser, Boolean enable) {
		Schedule schedule = scheduleService.getScheduleByResource(projectId, BugSyncJob.class.getName());
		if (schedule != null) {
			schedule.setEnable(enable);
			scheduleService.editSchedule(schedule);
		}
	}
}
