package io.metersphere.track.issue;

import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueFactory {
    public static AbstractIssuePlatform createPlatform(String platform, IssuesRequest addIssueRequest) {
        if (StringUtils.equals(IssuesManagePlatform.Tapd.toString(), platform)) {
            return new TapdPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Jira.toString(), platform)) {
            return new JiraPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Zentao.toString(), platform)) {
            return new ZentaoPlatform(addIssueRequest);
        } else if (StringUtils.equalsIgnoreCase(IssuesManagePlatform.Local.toString(), platform)) {
            return new LocalPlatform(addIssueRequest);
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

    public static Map<String, AbstractIssuePlatform> createPlatformsForMap(List<String> types, IssuesRequest addIssueRequest) {
        Map<String, AbstractIssuePlatform> platformMap = new HashMap<>();
        types.forEach(type -> {
            AbstractIssuePlatform abstractIssuePlatform = createPlatform(type, addIssueRequest);
            if (abstractIssuePlatform != null) {
                platformMap.put(type, abstractIssuePlatform);
            }
        });
        return platformMap;
    }
}
