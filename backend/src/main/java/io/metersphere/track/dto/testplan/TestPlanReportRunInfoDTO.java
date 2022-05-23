package io.metersphere.track.dto.testplan;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestPlanReportRunInfoDTO {

    private String envGroupId;

    private String runMode;
    // <测试计划场景关联表ID, <项目ID，环境ID>>
    private Map<String, Map<String,String>> scenarioRunInfo;
    // <测试计划用例关联表ID, <项目ID，环境ID>>
    private Map<String, Map<String,String>> apiCaseRunInfo;

    public TestPlanReportRunInfoDTO(){
        scenarioRunInfo = new HashMap<>();
        apiCaseRunInfo = new HashMap<>();
    }

    public void putScenarioRunInfo(String scenarioResourceId,String projectId,String environmentId){
        scenarioRunInfo.put(scenarioResourceId,new HashMap<>(){{
            this.put(projectId,environmentId);
        }});
    }

    public void putApiCaseRunInfo(String apiCaseResourceId,String projectId,String environmentId){
        apiCaseRunInfo.put(apiCaseResourceId,new HashMap<>(){{
            this.put(projectId,environmentId);
        }});
    }
}