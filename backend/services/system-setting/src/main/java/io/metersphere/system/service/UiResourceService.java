package io.metersphere.system.service;


import io.metersphere.system.dto.pool.TestResourceDTO;

public interface UiResourceService {

    boolean validate(TestResourceDTO testResourceDTO);

}
