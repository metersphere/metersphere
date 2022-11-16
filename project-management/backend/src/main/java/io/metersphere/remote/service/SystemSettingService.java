package io.metersphere.remote.service;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingService extends RemoteService {
    public SystemSettingService() {
        super(MicroServiceName.SYSTEM_SETTING);
    }
}
