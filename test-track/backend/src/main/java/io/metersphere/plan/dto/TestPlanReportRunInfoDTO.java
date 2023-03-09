package io.metersphere.plan.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanReportRunInfoDTO {

    private String envGroupId;

    private String runMode;
    private Map<String, List<String>> requestEnvMap;

    private List<String> resourcePools;

    // <测试计划场景关联表ID, <项目ID，环境ID>>
    private Map<String, Map<String, List<String>>> scenarioRunInfo;
    // <测试计划场景关联表ID, <项目ID，环境ID>>
    private Map<String, Map<String, List<String>>> uiScenarioRunInfo;
    // <测试计划用例关联表ID, <项目ID，环境ID>>
    private Map<String, Map<String, String>> apiCaseRunInfo;

    public TestPlanReportRunInfoDTO() {
        scenarioRunInfo = new HashMap<>();
        uiScenarioRunInfo = new HashMap<>();
        apiCaseRunInfo = new HashMap<>();
    }

    public void putScenarioRunInfo(String scenarioResourceId, String projectId, String environmentId) {
        if (scenarioRunInfo.containsKey(scenarioResourceId)) {
            if (scenarioRunInfo.get(scenarioResourceId).containsKey(projectId)) {
                if (!scenarioRunInfo.get(scenarioResourceId).get(projectId).contains(environmentId)) {
                    scenarioRunInfo.get(scenarioResourceId).get(projectId).add(environmentId);
                }
            } else {
                scenarioRunInfo.get(scenarioResourceId).put(projectId, new ArrayList<>() {{
                    this.add(environmentId);
                }});
            }
        } else {
            scenarioRunInfo.put(scenarioResourceId, new HashMap<>() {{
                this.put(projectId, new ArrayList<>() {{
                    this.add(environmentId);
                }});
            }});
        }
    }

    public void putUiScenarioRunInfo(String scenarioResourceId, String projectId, String environmentId) {
        if (uiScenarioRunInfo.containsKey(scenarioResourceId)) {
            if (uiScenarioRunInfo.get(scenarioResourceId).containsKey(projectId)) {
                if (!uiScenarioRunInfo.get(scenarioResourceId).get(projectId).contains(environmentId)) {
                    uiScenarioRunInfo.get(scenarioResourceId).get(projectId).add(environmentId);
                }
            } else {
                uiScenarioRunInfo.get(scenarioResourceId).put(projectId, new ArrayList<>() {{
                    this.add(environmentId);
                }});
            }
        } else {
            uiScenarioRunInfo.put(scenarioResourceId, new HashMap<>() {{
                this.put(projectId, new ArrayList<>() {{
                    this.add(environmentId);
                }});
            }});
        }
    }

    public void putApiCaseRunInfo(String apiCaseResourceId, String projectId, String environmentId) {
        apiCaseRunInfo.put(apiCaseResourceId, new HashMap<>() {{
            this.put(projectId, environmentId);
        }});
    }
}
