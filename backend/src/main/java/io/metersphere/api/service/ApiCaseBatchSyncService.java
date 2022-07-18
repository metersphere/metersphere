package io.metersphere.api.service;

import io.metersphere.api.dto.definition.ApiSyncCaseRequest;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;

public interface ApiCaseBatchSyncService {
    void oneClickSyncCase(ApiSyncCaseRequest apiSyncCaseRequest, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs);
}
