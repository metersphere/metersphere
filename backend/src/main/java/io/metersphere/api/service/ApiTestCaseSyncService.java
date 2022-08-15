package io.metersphere.api.service;

import io.metersphere.base.domain.ApiTestCaseWithBLOBs;

import java.util.List;


public interface ApiTestCaseSyncService {

    void setApiCaseUpdate(ApiTestCaseWithBLOBs test);

    void setCaseUpdateValue(ApiTestCaseWithBLOBs test);

    List<String> getSyncRuleCaseStatus(String projectId);

}
