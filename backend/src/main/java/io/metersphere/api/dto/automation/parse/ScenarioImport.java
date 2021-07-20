package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioImport {
    private String projectId;
    private List<ApiScenarioWithBLOBs> data;
    private List<NodeTree> nodeTree;
}
