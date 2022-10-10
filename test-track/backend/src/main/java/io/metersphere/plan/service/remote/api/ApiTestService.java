package io.metersphere.plan.service.remote.api;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiTestService extends RemoteService {
    public ApiTestService() {
        super(MicroServiceName.API_TEST);
    }
}
