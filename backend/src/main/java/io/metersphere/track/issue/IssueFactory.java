package io.metersphere.track.issue;

import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class IssueFactory {
    public static AbstractIssuePlatform createPlatform(String platform, IssuesRequest addIssueRequest) {
        if (StringUtils.equals(IssuesManagePlatform.Tapd.toString(), platform)) {
            return new TapdIssue(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Jira.toString(), platform)) {
            return new JiraIssue(addIssueRequest);
        }  else if (StringUtils.equals("LOCAL", platform)) {
            return new LocalIssue(addIssueRequest);
        }
        return null;
    }

    public static List<AbstractIssuePlatform> createPlatforms(List<String> types, IssuesRequest addIssueRequest) {
        List<AbstractIssuePlatform> platforms = new ArrayList<>();
        types.forEach(type -> {
            AbstractIssuePlatform abstractIssuePlatform = createPlatform(type, addIssueRequest);
            if (abstractIssuePlatform != null) {
                platforms.add(abstractIssuePlatform);
            }
        });
        return platforms;
    }
}
