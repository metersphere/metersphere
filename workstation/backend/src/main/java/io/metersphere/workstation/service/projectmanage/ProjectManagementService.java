package io.metersphere.workstation.service.projectmanage;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectManagementService extends RemoteService {
    public ProjectManagementService() {
        super(MicroServiceName.PROJECT_MANAGEMENT);
    }
}
