package io.metersphere.api.service;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.mapper.ApiTestCaseMapper;

public interface ApiCaseBatchSyncService {
    void oneClickSyncCase(String apiUpdateRule, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, ApiTestCaseMapper apiTestCaseMapper, ApiTestCaseWithBLOBs testCases);

    void sendApiNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs);

    void sendCaseNotice(ApiTestCase apiTestCase);
}
