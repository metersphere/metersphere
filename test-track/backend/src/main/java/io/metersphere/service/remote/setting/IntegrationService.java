package io.metersphere.service.remote.setting;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.request.IntegrationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class IntegrationService extends SettingService {

    public ServiceIntegration get(IntegrationRequest request) {
        return microService.postForData(serviceName, "/service/integration/type", request,
                ServiceIntegration.class);
    }
}
