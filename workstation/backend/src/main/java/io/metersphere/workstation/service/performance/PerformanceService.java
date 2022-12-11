package io.metersphere.workstation.service.performance;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceService extends RemoteService {

    public PerformanceService() {
        super(MicroServiceName.PERFORMANCE_TEST);
    }
}
