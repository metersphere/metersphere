package io.metersphere.xpack.api.service;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;

import java.util.Map;

public interface ApiCaseBatchSyncService {
    void oneClickSyncCase(String apiUpdateRule, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, ApiTestCaseWithBLOBs testCases);

    void sendApiNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, Map<String, Object> paramMap);

    void sendCaseNotice(ApiTestCase apiTestCase);
}
