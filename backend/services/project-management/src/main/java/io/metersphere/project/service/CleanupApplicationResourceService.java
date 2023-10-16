package io.metersphere.project.service;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.sechedule.ScheduleService;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author wx
 */
@Component
public class CleanupApplicationResourceService implements CleanupProjectResourceService {

    @Resource
    private ScheduleService scheduleService;

    @Override
    public void deleteResources(String projectId) {
        scheduleService.deleteByProjectId(projectId);
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关报告资源");
    }
}
