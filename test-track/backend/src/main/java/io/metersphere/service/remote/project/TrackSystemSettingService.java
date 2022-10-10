package io.metersphere.service.remote.project;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class TrackSystemSettingService extends RemoteService {

    public TrackSystemSettingService() {
        super(MicroServiceName.SYSTEM_SETTING);
    }
}
