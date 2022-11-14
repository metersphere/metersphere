package io.metersphere.api.dto;

import lombok.Data;

@Data
public class ApiCaseBasicInfoDTO {
    private String id;
    private String apiId;
    private String name;
    private String projectId;
    private String versionId;
}
