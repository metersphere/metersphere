package io.metersphere.reportstatistics.service.remote.apitest;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestService extends RemoteService {

    public ApiTestService() {
        super(MicroServiceName.API_TEST);
    }
}
