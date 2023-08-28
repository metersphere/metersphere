package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.job.CleanUpReportJob;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.sechedule.BaseScheduleService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.Schedule;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Resource
    private BaseScheduleService baseScheduleService;

    public ProjectApplication update(ProjectApplication application) {
        //定时任务配置，检查是否存在定时任务配置，存在则更新，不存在则新增
        this.doBeforeUpdate(application);
        //配置信息入库
        this.createOrUpdateConfig(application);
        return application;
    }

    private void createOrUpdateConfig(ProjectApplication application) {
        String type = application.getType();
        String projectId = application.getProjectId();
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
            projectApplicationMapper.updateByExample(application, example);
        } else {
            projectApplicationMapper.insertSelective(application);
        }
    }

    private void doBeforeUpdate(ProjectApplication application) {
        String type = application.getType();
        //TODO 自定义id配置 &其他配置
        if (StringUtils.equals(type, ProjectApplicationType.APPLICATION_CLEAN_TEST_PLAN_REPORT.name())) {
            //处理测试计划报告 定时任务
            this.doHandleSchedule(application);
        }
    }

    private void doHandleSchedule(ProjectApplication application) {
        String typeValue = application.getTypeValue();
        String projectId = application.getProjectId();
        Boolean enable = BooleanUtils.isTrue(Boolean.valueOf(typeValue));
        Schedule schedule = baseScheduleService.getScheduleByResource(application.getProjectId(), CleanUpReportJob.class.getName());
        Optional<Schedule> optional = Optional.ofNullable(schedule);
        optional.ifPresentOrElse(s -> {
            s.setEnable(enable);
            baseScheduleService.editSchedule(s);
            baseScheduleService.addOrUpdateCronJob(s,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        },() -> {
            Schedule request = new Schedule();
            request.setName("Clean Report Job");
            request.setResourceId(projectId);
            request.setKey(projectId);
            request.setProjectId(projectId);
            request.setEnable(enable);
            request.setCreateUser(SessionUtils.getUserId());
            request.setType(ScheduleType.CRON.name());
            // 每天凌晨2点执行清理任务
            request.setValue("0 0 2 * * ?");
            request.setJob(CleanUpReportJob.class.getName());
            baseScheduleService.addSchedule(request);
            baseScheduleService.addOrUpdateCronJob(request,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        });

    }


    /**
     * 更新接口日志
     *
     * @param application
     * @return
     */
    public LogDTO updateLog(ProjectApplication application) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andTypeEqualTo(application.getType()).andProjectIdEqualTo(application.getProjectId());
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_PROJECT_MANAGER,
                "测试计划配置");
        dto.setOriginalValue(JSON.toJSONBytes(list));
        return dto;
    }
}
