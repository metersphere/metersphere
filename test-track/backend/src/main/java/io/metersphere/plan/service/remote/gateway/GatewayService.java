package io.metersphere.plan.service.remote.gateway;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
public class GatewayService extends RemoteService {
    public GatewayService() {
        super(MicroServiceName.GATEWAY);
    }

    public Object getMicroServices() {
        return microService.getForData(serviceName, "/services");
    }
}
