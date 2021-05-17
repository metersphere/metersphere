package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RunDefinitionRequest {

    private String id;

    private String reportId;

    private String name;

    private String type;

    private String projectId;

    private String scenarioId;

    private String scenarioName;

    private String environmentId;

    private MsTestElement testElement;

    private String executeType;

    private Response response;

    private RunModeConfig config;

    private List<String> bodyUploadIds;

    private Map<String, String> environmentMap;
}
