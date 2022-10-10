package io.metersphere.plan.service.remote.ui;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UiTestService extends RemoteService {
    public UiTestService() {
        super(MicroServiceName.UI_TEST);
    }
}
