package io.metersphere.system.service;

import io.metersphere.system.dto.TestResourceDTO;

public interface LoadResourceService {

    boolean validate(TestResourceDTO testResourceDTO, String type);

}
