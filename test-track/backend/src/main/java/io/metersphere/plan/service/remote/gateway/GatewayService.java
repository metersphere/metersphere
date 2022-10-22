package io.metersphere.plan.service.remote.gateway;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.dto.ServiceDTO;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatewayService extends RemoteService {
    public GatewayService() {
        super(MicroServiceName.GATEWAY);
    }

    public List<ServiceDTO> getMicroServices() {
        return microService.getForDataArray(serviceName, "/services", ServiceDTO.class);
    }
}
