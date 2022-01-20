package io.metersphere.track.issue.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectIssueConfig {
    private String jiraIssueType;
    private String jiraStoryType;
    private String projectKey;
}
