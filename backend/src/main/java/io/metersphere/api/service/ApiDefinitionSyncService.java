package io.metersphere.api.service;

import io.metersphere.api.dto.definition.SaveApiDefinitionRequest;

public interface ApiDefinitionSyncService {

    void syncApi(SaveApiDefinitionRequest request);

}
