package io.metersphere.sdk.service;


import io.metersphere.sdk.dto.TestResourceDTO;

public interface LoadResourceService {

    boolean validate(TestResourceDTO testResourceDTO, String type);

}
