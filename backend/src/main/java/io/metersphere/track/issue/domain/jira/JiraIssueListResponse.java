package io.metersphere.track.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JiraIssueListResponse {
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
    private List<JiraIssue> issues;
}
