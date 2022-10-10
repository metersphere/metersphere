package io.metersphere.service.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JiraIssueProject {
    private String id;
    private String name;
    private String key;
    private List<JiraIssueType> issueTypes;
}
