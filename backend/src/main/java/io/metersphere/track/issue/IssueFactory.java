package io.metersphere.track.issue;

import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;

public class IssueFactory {
    public static AbstractIssuePlatform createPlatform(String platform, IssuesRequest addIssueRequest) {
        if (StringUtils.equals(IssuesManagePlatform.Tapd.toString(), platform)) {
            return new TapdPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Jira.toString(), platform)) {
            return new JiraPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Zentao.toString(), platform)) {
            return new ZentaoPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.AzureDevops.toString(), platform)) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Class clazz = loader.loadClass("io.metersphere.xpack.issue.azuredevops.AzureDevopsPlatform");
                Constructor cons = clazz.getDeclaredConstructor(new Class[] { IssuesRequest.class });
                AbstractIssuePlatform azureDevopsPlatform = (AbstractIssuePlatform) cons.newInstance(addIssueRequest);
                return azureDevopsPlatform;
            } catch (Throwable e) {
                LogUtil.error(e);
            }
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
