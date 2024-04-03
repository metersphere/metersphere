package io.metersphere.system.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.service.BaseCleanUpReport;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wx
 */
@Component
public class CleanUpReportJob {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Autowired
    private ApplicationContext applicationContext;
    @Resource
    private BaseProjectMapper baseProjectMapper;

    /**
     * 清理报告定时任务（所有项目共用一个）
     */
    @QuartzScheduled(cron = "0 0 1 * * ?")
    public void cleanReport() {

        long count = getProjectCount();

        long pages = Double.valueOf(Math.ceil(count / 100.0)).longValue();

        for (int i = 0; i < pages; i++) {
            int start = i * 100;
            List<Project> projects = baseProjectMapper.selectProjectByLimit(start, 100);
            projects.forEach(project -> {
                ProjectApplicationExample applicationExample = new ProjectApplicationExample();
                //test_plan
                applicationExample.createCriteria().andProjectIdEqualTo(project.getId()).andTypeEqualTo(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name());
                List<ProjectApplication> testPlan = projectApplicationMapper.selectByExample(applicationExample);
                Map<String, String> map = new HashMap<>();
                if (CollectionUtils.isNotEmpty(testPlan)) {
                    map.put(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name(), testPlan.get(0).getTypeValue());
                } else {
                    map.put(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name(), "3M");
                }

                //ui
                applicationExample.clear();
                applicationExample.createCriteria().andProjectIdEqualTo(project.getId()).andTypeEqualTo(ProjectApplicationType.UI.UI_CLEAN_REPORT.name());
                List<ProjectApplication> ui = projectApplicationMapper.selectByExample(applicationExample);
                if (CollectionUtils.isNotEmpty(ui)) {
                    map.put(ProjectApplicationType.UI.UI_CLEAN_REPORT.name(), ui.get(0).getTypeValue());
                } else {
                    map.put(ProjectApplicationType.UI.UI_CLEAN_REPORT.name(), "3M");
                }

                //load_test
                applicationExample.clear();
                applicationExample.createCriteria().andProjectIdEqualTo(project.getId()).andTypeEqualTo(ProjectApplicationType.LOAD_TEST.LOAD_TEST_CLEAN_REPORT.name());
                List<ProjectApplication> loadTest = projectApplicationMapper.selectByExample(applicationExample);
                if (CollectionUtils.isNotEmpty(loadTest)) {
                    map.put(ProjectApplicationType.LOAD_TEST.LOAD_TEST_CLEAN_REPORT.name(), loadTest.get(0).getTypeValue());
                } else {
                    map.put(ProjectApplicationType.LOAD_TEST.LOAD_TEST_CLEAN_REPORT.name(), "3M");
                }

                //api
                applicationExample.clear();
                applicationExample.createCriteria().andProjectIdEqualTo(project.getId()).andTypeEqualTo(ProjectApplicationType.API.API_CLEAN_REPORT.name());
                List<ProjectApplication> api = projectApplicationMapper.selectByExample(applicationExample);
                if (CollectionUtils.isNotEmpty(api)) {
                    map.put(ProjectApplicationType.API.API_CLEAN_REPORT.name(), api.get(0).getTypeValue());
                } else {
                    map.put(ProjectApplicationType.API.API_CLEAN_REPORT.name(), "3M");
                }

                Map<String, BaseCleanUpReport> beansOfType = applicationContext.getBeansOfType(BaseCleanUpReport.class);
                beansOfType.forEach((k, v) -> {
                    v.cleanReport(map, project.getId());
                });

            });

        }
    }

    private long getProjectCount() {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andDeletedEqualTo(false);
        return projectMapper.countByExample(example);
    }
}
