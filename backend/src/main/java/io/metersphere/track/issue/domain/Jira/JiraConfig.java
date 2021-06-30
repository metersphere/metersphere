package io.metersphere.track.issue.domain.Jira;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraConfig {
    private String account;
    private String password;
    private String url;
    private String issuetype;
    private String storytype;
}
