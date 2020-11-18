package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.definition.response.Response;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiDefinitionRequest {

    private String id;

    private String reportId;

    private String projectId;

    private String name;

    private String url;

    private String moduleId;

    private String status;

    private String description;

    private String modulePath;

    private String method;

    private Scenario scenario;

    private Object request;

    private Response response;

    private String environmentId;

    private String userId;

    private Schedule schedule;

    private String triggerMode;

    private List<String> bodyUploadIds;
}
