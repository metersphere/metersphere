package io.metersphere.plan.service.remote;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackService extends RemoteService {

    public TrackService() {
        super(MicroServiceName.TEST_TRACK);
    }
}
