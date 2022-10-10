package io.metersphere.reportstatistics.service.remote.projectmanagement;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectSettingBaseService extends RemoteService {

    public ProjectSettingBaseService() {
        super(MicroServiceName.PROJECT_MANAGEMENT);
    }
}
