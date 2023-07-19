package io.metersphere.system.mock;

import io.metersphere.sdk.service.CleanupProjectResourceService;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class CleanupTestResourceService implements CleanupProjectResourceService {

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]TEST资源");
    }
}
