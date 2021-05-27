package io.metersphere.api.dto.automation;


import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiScenarioBatchRequest extends ApiScenarioWithBLOBs {
    private List<String> ids;
    private String id;
    private String name;
    private String projectId;
    private String environmentId;

    private ApiScenarioRequest condition;

    /**
     * 环境和项目对应关系
     */
    private Map<String, String> envMap;

    /**
     * 场景用例跨项目的关系
     */
    private Map<String, List<String>> mapping;
}
