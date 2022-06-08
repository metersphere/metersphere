package io.metersphere.track.request.issues;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformIssueTypeRequest {
    private String projectId;
    private String workspaceId;
    private String platformKey;
}
