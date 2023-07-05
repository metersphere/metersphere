package io.metersphere.sdk.service;


import io.metersphere.sdk.dto.TestResourceDTO;

public interface KubernetesResourcePoolService {

    boolean validate(TestResourceDTO testResourceDTO, Boolean usedApiType);
}
