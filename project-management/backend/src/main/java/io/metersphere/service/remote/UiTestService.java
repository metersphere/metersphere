package io.metersphere.service.remote;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class UiTestService extends RemoteService {

    public UiTestService() {
        super(MicroServiceName.UI_TEST);
    }
}
