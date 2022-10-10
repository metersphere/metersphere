package io.metersphere.job.schedule;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.sechedule.MsScheduleJob;
import io.metersphere.service.ProjectService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;

/**
 * @author lyh
 */
public class CleanUpReportJob extends MsScheduleJob {

    private final ProjectService projectService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    LocalDate localDate;

    public CleanUpReportJob() {
        projectService = CommonBeanFactory.getBean(ProjectService.class);
        kafkaTemplate = CommonBeanFactory.getBean(KafkaTemplate.class);
        localDate = LocalDate.now();
    }

    @Override
    public void businessExecute(JobExecutionContext context) {
        Project project = projectService.getProjectById(resourceId);
        if (project == null) {
            LogUtil.error("clean up report fail. project id is not exist. ");
            return;
        }

        LogUtil.info("send clean up message, project id: " + project.getId());
        kafkaTemplate.send(KafkaTopicConstants.CLEAN_UP_REPORT_SCHEDULE, project.getId());
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
    }
}
