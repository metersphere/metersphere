package io.metersphere.api.dto.automation;

import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SaveApiScenarioRequest {
    private String id;

    private String projectId;

    private String tags;

    private String userId;

    private String apiScenarioModuleId;

    private String environmentId;

    private String modulePath;

    private String name;

    private String level;

    private String status;

    private String principal;

    private Integer stepTotal;

    private String followPeople;

    private String schedule;

    private String description;

    private Integer version;

    private MsTestElement scenarioDefinition;

    List<String> bodyFileRequestIds;

    List<String> scenarioFileIds;

    private List<String> scenarioIds;

    private boolean isSelectAllDate;

    private Map<String, List<String>> filters;

    private List<String> moduleIds;

    private List<String> unSelectIds;

    private String customNum;

}
