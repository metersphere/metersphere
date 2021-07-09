package io.metersphere.dto;

import lombok.Data;

@Data
public class RelatedSource {
    private String organizationId;
    private String workspaceId;
    private String projectId;
}
