package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiTestImportRequest {
    private String name;
    private String environmentId;
    private String projectId;
    private String platform;
    private Boolean useEnvironment;
    private String swaggerUrl;
}
