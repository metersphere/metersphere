package io.metersphere.sdk.constants;

public class ModuleConstants {
    //未规划节点的ID
    public static final String DEFAULT_NODE_ID = "root";
    //没有父类的节点，parent_id为NONE
    public static final String ROOT_NODE_PARENT_ID = "NONE";
    //默认节点类型
    public static final String NODE_TYPE_DEFAULT = "MODULE";
    //Git节点类型
    public static final String NODE_TYPE_GIT = "GIT";
    //Gitea节点类型
    public static final String NODE_TYPE_GITEA = "Gitea";
    //GitHub节点类型
    public static final String NODE_TYPE_GITHUB = "Github";
    //Gitee节点类型
    public static final String NODE_TYPE_GITEE = "Gitee";
    //GitLab节点类型
    public static final String NODE_TYPE_GITLAB = "Gitlab";

    public static final String NODE_PROTOCOL_HTTP = "HTTP";

    /* 模块数量统计相关key */
    public static final String MODULE_COUNT_ALL = "all";
    public static final String MODULE_COUNT_MY = "my";
    public static final String MODULE_COUNT_GIT = "git";
    public static final String MODULE_COUNT_MINIO = "minio";
    /* 模块数量统计相关key end*/
}
