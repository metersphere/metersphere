package io.metersphere.api.service;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;

public interface ApiAutomationRelationshipEdgeService {
    // 初始化引用关系
    void initRelationshipEdge(ApiScenarioWithBLOBs before, ApiScenarioWithBLOBs now);
}
