package io.metersphere.api.service;

import io.metersphere.api.dto.definition.ApiSyncCaseRequest;
import io.metersphere.api.dto.definition.SaveApiDefinitionRequest;

public interface ApiDefinitionSyncService {

    void syncApi(SaveApiDefinitionRequest request);

    ApiSyncCaseRequest getApiSyncCaseRequest(String projectId);

    Boolean getProjectApplications(String projectId);


}
