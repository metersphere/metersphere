package io.metersphere.project.service;

import io.metersphere.sdk.service.CleanupProjectResourceService;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class CleanupRobotResourceService implements CleanupProjectResourceService {

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关接口测试资源");
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告资源");
    }
}
