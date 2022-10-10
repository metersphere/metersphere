package io.metersphere.service.remote.project;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class TrackProjectSettingService extends RemoteService {

    public TrackProjectSettingService() {
        super(MicroServiceName.PROJECT_MANAGEMENT);
    }
}
