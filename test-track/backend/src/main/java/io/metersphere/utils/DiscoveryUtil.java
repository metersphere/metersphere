package io.metersphere.utils;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ServiceDTO;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscoveryUtil {

    public static boolean hasService(String serviceId) {
        return getServiceIdSet().contains(serviceId);
    }

    public static Set<String> getServiceIdSet() {
        DiscoveryClient discoveryClient = CommonBeanFactory.getBean(DiscoveryClient.class);
        return discoveryClient.getServices()
                .stream()
                .collect(Collectors.toSet());
    }

    public static List<ServiceDTO> getServices() {
        DiscoveryClient discoveryClient = CommonBeanFactory.getBean(DiscoveryClient.class);
        return discoveryClient.getServices().stream()
                .map(service -> new ServiceDTO(service, discoveryClient.getInstances(service).get(0).getPort()))
                .collect(Collectors.toList());
    }
}
