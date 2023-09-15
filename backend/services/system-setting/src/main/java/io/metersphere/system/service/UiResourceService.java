package io.metersphere.system.service;


import io.metersphere.sdk.dto.TestResourceDTO;

public interface UiResourceService {

    boolean validate(TestResourceDTO testResourceDTO);

}
