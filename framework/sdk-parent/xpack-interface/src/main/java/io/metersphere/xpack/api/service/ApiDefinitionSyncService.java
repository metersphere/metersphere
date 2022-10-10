package io.metersphere.xpack.api.service;

import io.metersphere.request.ApiSyncCaseRequest;
import io.metersphere.request.SyncApiDefinitionRequest;

public interface ApiDefinitionSyncService {
    void syncApi(SyncApiDefinitionRequest request);

    ApiSyncCaseRequest getApiSyncCaseRequest(String projectId);

    Boolean getProjectApplications(String projectId);
}
