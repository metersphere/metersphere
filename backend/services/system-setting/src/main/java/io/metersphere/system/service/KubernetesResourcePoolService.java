package io.metersphere.system.service;


import io.metersphere.system.dto.TestResourcePoolDTO;

public interface KubernetesResourcePoolService {

    boolean validate(TestResourcePoolDTO testResourcePool);
}
