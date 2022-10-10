package io.metersphere.plan.service.remote.performance;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadTestService extends RemoteService {
    public LoadTestService() {
        super(MicroServiceName.PERFORMANCE_TEST);
    }
}
