package io.metersphere.xpack.resourcepool.engine;


import io.metersphere.dto.TestResourcePoolDTO;

public interface KubernetesResourcePoolService {

    boolean validate(TestResourcePoolDTO testResourcePool);
}
