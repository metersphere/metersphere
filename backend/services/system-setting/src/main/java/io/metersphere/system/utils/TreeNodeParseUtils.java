package io.metersphere.system.utils;

import io.metersphere.project.domain.Project;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.lang3.StringUtils;

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


    // nodePath需要以"/"开头
    public static List<BaseTreeNode> getInsertNodeByPath(Map<String, BaseTreeNode> modulePathMap, String nodePath) {
        //解析modulePath  格式为/a/b/c
        String[] split = nodePath.split("/");
        //一层一层的创建
        List<BaseTreeNode> insertList = new ArrayList<>();

        //因为nodePath是以/开头的，所以split[0]为空，从1开始
        for (int i = 1; i < split.length; i++) {
            String modulePath = StringUtils.join(split, "/", 1, i + 1);
            String path = StringUtils.join("/", modulePath);
            BaseTreeNode baseTreeNode = modulePathMap.get(path);
            if (baseTreeNode == null) {
                //创建模块
                BaseTreeNode module = new BaseTreeNode();
                module.setId(IDGenerator.nextStr());
                module.setName(split[i]);
                if (i != 1) {
                    String parentPath = path.substring(0, path.lastIndexOf("/" + split[i]));
                    module.setParentId(modulePathMap.get(parentPath).getId());
                }
                module.setPath(path);
                insertList.add(module);
                modulePathMap.put(path, module);
            }
        }
        return insertList;
    }

    public static String genFullModulePath(String selectModulePath, String modulePath) {
        String firstPath = selectModulePath;
        String lathPath = modulePath;
        if (!StringUtils.startsWith(firstPath, "/")) {
            firstPath = "/" + firstPath;
        }
        if (StringUtils.endsWith(firstPath, "/")) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
        }

        if (!StringUtils.startsWith(lathPath, "/")) {
            lathPath = "/" + lathPath;
        }
        if (StringUtils.endsWith(lathPath, "/")) {
            lathPath = lathPath.substring(0, firstPath.length() - 1);
        }
        return firstPath + lathPath;
    }
}
