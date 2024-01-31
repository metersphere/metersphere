package io.metersphere.system.job;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CommonProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class CleanProjectJob {

    @Resource
    private ProjectMapper projectMapper;

    /**
     * 清理状态为删除的项目  每天凌晨三点执行
     */
    @QuartzScheduled(cron = "0 0 3 * * ?")
    public void cleanupProject() {
        LogUtils.info("clean up project start.");
        LocalDateTime dateTime = LocalDateTime.now().minusDays(30);
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.doCleanupProject(timestamp);
        LogUtils.info("clean up project end.");
    }

    private void doCleanupProject(long timestamp) {
        //获取所有删除状态的项目总量
        ProjectExample example = new ProjectExample();
        example.createCriteria().andDeletedEqualTo(true).andDeleteTimeLessThanOrEqualTo(timestamp);
        long count = projectMapper.countByExample(example);
        CommonProjectService commonProjectService = CommonBeanFactory.getBean(CommonProjectService.class);
        while (count > 0) {
            PageHelper.startPage(1, 100);
            List<Project> projects = projectMapper.selectByExample(example);
            assert commonProjectService != null;
            commonProjectService.deleteProject(projects);
            count = projectMapper.countByExample(example);
            LogUtils.info("剩余项目数量为===================" + count);
        }
    }
}
