package io.metersphere.workstation.service.api;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiService extends RemoteService {

    public ApiService() {
        super(MicroServiceName.API_TEST);
    }
}
