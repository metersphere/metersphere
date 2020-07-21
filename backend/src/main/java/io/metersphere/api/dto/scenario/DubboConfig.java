package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.scenario.request.dubbo.ConfigCenter;
import io.metersphere.api.dto.scenario.request.dubbo.ConsumerAndService;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import lombok.Data;

@Data
public class DubboConfig {
    private ConfigCenter configCenter;
    private RegistryCenter registryCenter;
    private ConsumerAndService consumerAndService;
}
