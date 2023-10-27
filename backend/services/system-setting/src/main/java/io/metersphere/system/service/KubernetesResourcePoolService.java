package io.metersphere.system.service;


import io.metersphere.system.dto.pool.TestResourceDTO;

public interface KubernetesResourcePoolService {

    boolean validate(TestResourceDTO testResourceDTO, Boolean usedApiType);
}
