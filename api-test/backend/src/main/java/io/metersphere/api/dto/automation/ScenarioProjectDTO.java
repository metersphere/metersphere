package io.metersphere.api.dto.automation;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ScenarioProjectDTO {
    private List<String> projectIdList;
    private Map<String, List<String>> scenarioProjectIdMap;

    public ScenarioProjectDTO() {
        this.projectIdList = new ArrayList<>();
        this.scenarioProjectIdMap = new HashMap<>();
    }

    public void merge(ScenarioProjectDTO mergeDTO) {
        if (CollectionUtils.isNotEmpty(mergeDTO.getProjectIdList())) {
            for (String projectId : mergeDTO.getProjectIdList()) {
                if (!this.projectIdList.contains(projectId)) {
                    projectIdList.add(projectId);
                }
            }
        }
        if (MapUtils.isNotEmpty(mergeDTO.getScenarioProjectIdMap())) {
            this.scenarioProjectIdMap.putAll(mergeDTO.getScenarioProjectIdMap());
        }
    }
}
