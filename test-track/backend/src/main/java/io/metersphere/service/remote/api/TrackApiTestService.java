package io.metersphere.service.remote.api;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class TrackApiTestService extends RemoteService {

    public TrackApiTestService() {
        super(MicroServiceName.API_TEST);
    }
}
