package io.metersphere.service.remote;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class BaseSystemSettingService extends RemoteService {
    public BaseSystemSettingService() {
        super(MicroServiceName.SYSTEM_SETTING);
    }
}
