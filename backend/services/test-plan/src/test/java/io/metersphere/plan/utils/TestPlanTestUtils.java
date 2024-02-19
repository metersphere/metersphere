package io.metersphere.plan.utils;

import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TestPlanTestUtils {
    public static BaseTreeNode getNodeByName(List<BaseTreeNode> preliminaryTreeNodes, String nodeName) {
        for (BaseTreeNode firstLevelNode : preliminaryTreeNodes) {
            if (StringUtils.equals(firstLevelNode.getName(), nodeName)) {
                return firstLevelNode;
            }
            if (CollectionUtils.isNotEmpty(firstLevelNode.getChildren())) {
                for (BaseTreeNode secondLevelNode : firstLevelNode.getChildren()) {
                    if (StringUtils.equals(secondLevelNode.getName(), nodeName)) {
                        return secondLevelNode;
                    }
                    if (CollectionUtils.isNotEmpty(secondLevelNode.getChildren())) {
                        for (BaseTreeNode thirdLevelNode : secondLevelNode.getChildren()) {
                            if (StringUtils.equals(thirdLevelNode.getName(), nodeName)) {
                                return thirdLevelNode;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
