package io.metersphere.api.dto.automation.parse;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioImport {
    private String projectId;
    private List<ApiScenarioWithBLOBs> data;
}
