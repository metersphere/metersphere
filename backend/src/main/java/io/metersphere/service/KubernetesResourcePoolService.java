package io.metersphere.service;

import io.metersphere.dto.TestResourcePoolDTO;

public interface KubernetesResourcePoolService {
    boolean validate(TestResourcePoolDTO testResourcePool);
}
