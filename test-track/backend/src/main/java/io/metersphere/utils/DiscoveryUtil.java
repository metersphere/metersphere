package io.metersphere.utils;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ServiceDTO;
import io.metersphere.plan.service.remote.gateway.GatewayService;

import java.util.Set;
import java.util.stream.Collectors;

public class DiscoveryUtil {

    public static boolean hasService(String serviceId) {
        return getServiceIdSet().contains(serviceId);
    }

    public static Set<String> getServiceIdSet() {
        GatewayService gatewayService = CommonBeanFactory.getBean(GatewayService.class);
        return gatewayService.getMicroServices()
                .stream()
                .map(ServiceDTO::getServiceId)
                .collect(Collectors.toSet());
    }
}
