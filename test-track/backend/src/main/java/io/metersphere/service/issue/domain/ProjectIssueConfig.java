package io.metersphere.service.issue.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectIssueConfig {
    private String jiraIssueTypeId;
    private String jiraStoryTypeId;
    private String projectKey;
}
