package io.metersphere.service.remote.setting;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SettingService extends RemoteService {

    public SettingService() {
        super(MicroServiceName.SYSTEM_SETTING);
    }
}
