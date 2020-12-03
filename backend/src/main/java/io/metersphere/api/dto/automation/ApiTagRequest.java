package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiTagRequest {
    private String id;
    private String projectId;
    private String name;
    private String userId;
    private String workspaceId;
}
