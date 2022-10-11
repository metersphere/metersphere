package io.metersphere.service.issue.platform;

import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.issue.IssuesPlatform;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueFactory {
    public static IssuesPlatform createPlatform(String platform, IssuesRequest addIssueRequest) {
        if (StringUtils.equals(IssuesManagePlatform.Tapd.toString(), platform)) {
            return new TapdPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Jira.toString(), platform)) {
            return new JiraPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.Zentao.toString(), platform)) {
            return new ZentaoPlatform(addIssueRequest);
        } else if (StringUtils.equals(IssuesManagePlatform.AzureDevops.toString(), platform)) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Class clazz = loader.loadClass("io.metersphere.xpack.track.issue.platform.AzureDevopsPlatform");
                Constructor cons = clazz.getDeclaredConstructor(new Class[] { IssuesRequest.class });
                IssuesPlatform azureDevopsPlatform = (IssuesPlatform) cons.newInstance(addIssueRequest);
                return azureDevopsPlatform;
            } catch (Throwable e) {
                LogUtil.error(e);
            }
        } else if (StringUtils.equalsIgnoreCase(IssuesManagePlatform.Local.toString(), platform)) {
            return new LocalPlatform(addIssueRequest);
        }
        return null;
    }

    public static List<IssuesPlatform> createPlatforms(List<String> types, IssuesRequest addIssueRequest) {
        List<IssuesPlatform> platforms = new ArrayList<>();
        types.forEach(type -> {
            IssuesPlatform abstractIssuePlatform = createPlatform(type, addIssueRequest);
            if (abstractIssuePlatform != null) {
                platforms.add(abstractIssuePlatform);
            }
        });
        return platforms;
    }

    public static Map<String, IssuesPlatform> createPlatformsForMap(List<String> types, IssuesRequest addIssueRequest) {
        Map<String, IssuesPlatform> platformMap = new HashMap<>();
        types.forEach(type -> {
            IssuesPlatform abstractIssuePlatform = createPlatform(type, addIssueRequest);
            if (abstractIssuePlatform != null) {
                platformMap.put(type, abstractIssuePlatform);
            }
        });
        return platformMap;
    }
}
