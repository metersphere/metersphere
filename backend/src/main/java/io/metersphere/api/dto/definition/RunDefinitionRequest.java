package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RunDefinitionRequest {

    private String id;

    private String reportId;

    private String type;

    private String projectId;

    private String environmentId;

    private MsTestElement testElement;

    private String executeType;

    private Response response;

    private List<String> bodyUploadIds;
}
