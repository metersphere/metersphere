package io.metersphere.plan.service;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import org.springframework.stereotype.Component;

@Component
public class CleanupPlanResourceService implements CleanupProjectResourceService {

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关测试计划资源");
    }

}
