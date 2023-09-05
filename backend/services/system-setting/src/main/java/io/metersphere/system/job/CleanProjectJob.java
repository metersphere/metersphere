package io.metersphere.system.job;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class CleanProjectJob {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private CommonProjectService commonProjectService;

    /**
     * 清理状态为删除的项目  每天凌晨三点执行
     */
    @QuartzScheduled(cron = "0 0 3 * * ?")
    public void cleanupProject() {
        LogUtils.info("clean up project start.");
        try {
            LocalDate date = LocalDate.now().minusMonths(1);
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.doCleanupProject(timestamp);
        } catch (Exception e) {
            LogUtils.error("clean up project error.", e);
        }
        LogUtils.info("clean up project end.");
    }

    private void doCleanupProject(long timestamp) {
        //获取所有删除状态的项目总量
        ProjectExample example = new ProjectExample();
        example.createCriteria().andDeletedEqualTo(true).andDeleteTimeLessThanOrEqualTo(timestamp);
        long count = projectMapper.countByExample(example);
        for (int i = 0; i < count; i++) {
            //对项目进行分批处理
            if (i % 100 == 0) {
                List<Project> deleteProjectIds = extSystemProjectMapper.getDeleteProjectIds(timestamp, i);
                commonProjectService.deleteProject(deleteProjectIds);
            }
        }
    }
}
