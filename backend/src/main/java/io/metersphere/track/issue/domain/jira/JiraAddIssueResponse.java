package io.metersphere.track.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraAddIssueResponse {
    private String id;
    private String key;
    private String self;
}
