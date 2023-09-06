package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectRobotExample;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.service.CleanupProjectResourceService;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CleanupRobotResourceService implements CleanupProjectResourceService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Override
    public void deleteResources(String projectId) {
        ProjectRobotExample projectExample = new ProjectRobotExample();
        projectExample.createCriteria().andProjectIdEqualTo(projectId);
        robotMapper.deleteByExample(projectExample);
        LogUtils.info("删除当前项目[" + projectId + "]相关接口测试资源");
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告资源");
    }
}
