package io.metersphere.system.mock;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CreateProjectResourceService;
import org.springframework.stereotype.Component;

@Component
public class CreateTestResourceService implements CreateProjectResourceService {

    @Override
    public void createResources(String projectId) {
        LogUtils.info("默认增加当前项目[" + projectId + "]TEST资源");
    }

}
