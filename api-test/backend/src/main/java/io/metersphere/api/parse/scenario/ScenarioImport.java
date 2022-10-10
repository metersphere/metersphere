package io.metersphere.api.parse.scenario;

import io.metersphere.api.parse.api.ms.NodeTree;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioImport {
    private String projectId;
    private List<ApiScenarioWithBLOBs> data;
    private List<NodeTree> nodeTree;
}
