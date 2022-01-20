package io.metersphere.track.issue.domain.jira;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JiraUser {
    private String accountId;
    private String key;
    private String name;
    private String displayName;
    private String emailAddress;
    private Boolean active;
}
