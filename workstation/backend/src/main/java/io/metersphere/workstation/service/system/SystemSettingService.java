package io.metersphere.workstation.service.system;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemSettingService extends RemoteService {

    public SystemSettingService() {
        super(MicroServiceName.SYSTEM_SETTING);
    }

}

