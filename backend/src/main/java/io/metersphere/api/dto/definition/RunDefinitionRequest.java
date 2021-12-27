package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.definition.response.Response;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RunDefinitionRequest {
    private String id;

    private String reportId;

    private String runMode;

    private boolean isDebug;

    private boolean saved;

    private String requestId;

    private String name;

    private String type;

    private String projectId;

    private String scenarioId;

    private String scenarioName;

    private String environmentId;

    private MsTestElement testElement;

    private String executeType;

    private Response response;

    List<String> bodyFileRequestIds;

    List<String> scenarioFileIds;

    private RunModeConfigDTO config;

    private Map<String, String> environmentMap;

    private String environmentType;
    private String environmentJson;
    private String environmentGroupId;
}
