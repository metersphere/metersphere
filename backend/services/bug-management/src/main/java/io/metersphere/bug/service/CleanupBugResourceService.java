package io.metersphere.bug.service;

import io.metersphere.sdk.service.CleanupProjectResourceService;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class CleanupBugResourceService implements CleanupProjectResourceService {

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关缺陷资源");
    }
}
