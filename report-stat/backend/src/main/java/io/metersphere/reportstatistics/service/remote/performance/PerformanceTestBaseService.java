package io.metersphere.reportstatistics.service.remote.performance;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceTestBaseService extends RemoteService {

    public PerformanceTestBaseService() {
        super(MicroServiceName.PERFORMANCE_TEST);
    }
}
