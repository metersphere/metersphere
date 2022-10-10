package io.metersphere.request.testcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesCountRequest {
    private String workspaceId;
    private String creator;
}
