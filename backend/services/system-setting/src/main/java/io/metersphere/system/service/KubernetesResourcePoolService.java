package io.metersphere.system.service;


import io.metersphere.system.dto.TestResourceDTO;

public interface KubernetesResourcePoolService {

    boolean validate(TestResourceDTO testResourceDT);
}
