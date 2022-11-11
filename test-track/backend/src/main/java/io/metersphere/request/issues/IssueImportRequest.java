package io.metersphere.request.issues;

import lombok.Data;

@Data
public class IssueImportRequest {
    private String projectId;
    private String workspaceId;
    private String userId;
    private String importType;
}
