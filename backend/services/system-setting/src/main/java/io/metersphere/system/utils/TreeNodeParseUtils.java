package io.metersphere.system.utils;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.system.domain.Organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNodeParseUtils {
    public static List<BaseTreeNode> parseOrgProjectMap(Map<Organization, List<Project>> orgProjectMap) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        for (Map.Entry<Organization, List<Project>> entry : orgProjectMap.entrySet()) {
            Organization organization = entry.getKey();
            List<Project> projects = entry.getValue();

            BaseTreeNode orgNode = new BaseTreeNode(organization.getId(), organization.getName(), Organization.class.getName());
            returnList.add(orgNode);

            for (Project project : projects) {
                BaseTreeNode projectNode = new BaseTreeNode(project.getId(), project.getName(), Project.class.getName());
                orgNode.addChild(projectNode);
            }
        }
        return returnList;
    }
}
