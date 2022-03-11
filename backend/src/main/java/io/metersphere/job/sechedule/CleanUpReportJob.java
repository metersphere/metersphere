package io.metersphere.job.sechedule;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.service.ProjectService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.BiConsumer;

/**
 * @author lyh
 */
public class CleanUpReportJob extends MsScheduleJob {

    private final ProjectService projectService;
    private final ProjectApplicationService projectApplicationService;
    private static final String UNIT_DAY = "D";
    private static final String UNIT_MONTH = "M";
    private static final String UNIT_YEAR = "Y";
    LocalDate localDate;

    public CleanUpReportJob() {
        projectService = CommonBeanFactory.getBean(ProjectService.class);
        projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
        localDate = LocalDate.now();
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        LogUtil.info("clean up report start.");
        Project project = projectService.getProjectById(resourceId);
        if (project == null) {
            return;
        }
        ProjectConfig config = projectApplicationService.getProjectConfig(project.getId());
        try {
            if (BooleanUtils.isTrue(config.getCleanTrackReport())) {
                this.doCleanUp(projectService::cleanUpTrackReport, config.getCleanTrackReportExpr());
            }
            if (BooleanUtils.isTrue(config.getCleanApiReport())) {
                this.doCleanUp(projectService::cleanUpApiReport, config.getCleanApiReportExpr());
            }
            if (BooleanUtils.isTrue(config.getCleanLoadReport())) {
                this.doCleanUp(projectService::cleanUpLoadReport, config.getCleanLoadReportExpr());
            }
        } catch (Exception e) {
            LogUtil.error("clean up report error.");
            LogUtil.error(e.getMessage(), e);
        }
        LogUtil.info("clean up report end.");
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
    }

    private void doCleanUp(BiConsumer<Long, String> func, String expr) {
        long time = getCleanDate(expr);
        if (time == 0) {
            return;
        }
        func.accept(time, resourceId);
    }

    private long getCleanDate(String expr) {
        LocalDate date = null;
        long timeMills = 0;
        if (StringUtils.isNotBlank(expr)) {
            try {
                String unit = expr.substring(expr.length() - 1);
                int quantity = Integer.parseInt(expr.substring(0, expr.length() - 1));
                if (StringUtils.equals(unit, UNIT_DAY)) {
                    date = localDate.minusDays(quantity);
                } else if (StringUtils.equals(unit, UNIT_MONTH)) {
                    date = localDate.minusMonths(quantity);
                } else if (StringUtils.equals(unit, UNIT_YEAR)) {
                    date = localDate.minusYears(quantity);
                } else {
                    LogUtil.error("clean up expr parse error. expr : " + expr);
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                LogUtil.error("clean up job. get clean date error. project : " + resourceId);
            }
        }
        if (date != null) {
            timeMills = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return timeMills;
    }
}
