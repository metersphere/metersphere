package io.metersphere.sdk.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 默认的缺陷状态选项
 * 方便初始化项目和组织的状态项
 */
@Getter
public enum DefaultBugStatusItem {

    /**
     * 新建
     */
    NEW(DefaultBugStatusItemName.NEW, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS, DefaultBugStatusItemName.REJECTED),
            Arrays.asList(BugStatusDefinitionType.START)),
    /**
     * 处理中
     */
    IN_PROCESS(DefaultBugStatusItemName.IN_PROCESS, null,
            Arrays.asList(DefaultBugStatusItemName.REJECTED, DefaultBugStatusItemName.RESOLVED, DefaultBugStatusItemName.CLOSED),
            List.of()),
    /**
     * 已关闭
     */
    CLOSED(DefaultBugStatusItemName.CLOSED, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS),
            Arrays.asList(BugStatusDefinitionType.END)),
    /**
     * 已解决
     */
    RESOLVED(DefaultBugStatusItemName.RESOLVED, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS, DefaultBugStatusItemName.CLOSED),
            List.of()),
    /**
     * 已拒绝
     */
    REJECTED(DefaultBugStatusItemName.REJECTED, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS),
            Arrays.asList(BugStatusDefinitionType.END));

    private String name;
    private String remark;
    /**
     * 状态流流转状态
     */
    private List<String> statusFlowTargets;
    /**
     * 状态定义
     */
    private List<BugStatusDefinitionType> definitionTypes;

    DefaultBugStatusItem(String name, String remark, List<String> statusFlowTargets, List<BugStatusDefinitionType> definitionTypes) {
        this.name = name;
        this.remark = remark;
        this.statusFlowTargets = statusFlowTargets;
        this.definitionTypes = definitionTypes;
    }
}
