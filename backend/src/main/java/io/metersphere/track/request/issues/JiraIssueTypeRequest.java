package io.metersphere.track.request.issues;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraIssueTypeRequest {
    private String projectId;
    private String workspaceId;
    private String jiraKey;
}
