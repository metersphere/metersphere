package io.metersphere.reportstatistics.service.remote.track;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestTrackBaseService extends RemoteService {

    public TestTrackBaseService() {
        super(MicroServiceName.TEST_TRACK);
    }
}
