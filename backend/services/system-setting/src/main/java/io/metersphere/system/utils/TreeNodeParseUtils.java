package io.metersphere.system.utils;

import io.metersphere.project.domain.Project;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.response.user.UserTreeSelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNodeParseUtils {
    public static List<UserTreeSelectOption> parseOrgProjectMap(Map<Organization, List<Project>> orgProjectMap) {
        List<UserTreeSelectOption> userTreeSelectOptions = new ArrayList<>();
        for (Map.Entry<Organization, List<Project>> entry : orgProjectMap.entrySet()) {
            Organization organization = entry.getKey();
            List<Project> projects = entry.getValue();

            UserTreeSelectOption orgNode = new UserTreeSelectOption(organization.getId(), organization.getName(), null);
            userTreeSelectOptions.add(orgNode);

            for (Project project : projects) {
                UserTreeSelectOption projectNode = new UserTreeSelectOption(project.getId(), project.getName(), organization.getId());
                userTreeSelectOptions.add(projectNode);
            }
        }
        return userTreeSelectOptions;
    }
}
