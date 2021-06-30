package io.metersphere.track.issue.domain.Jira;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraIssue {
    private String expand;
    private String id;
    private String self;
    private String key;
    private JSONObject fields;
}
