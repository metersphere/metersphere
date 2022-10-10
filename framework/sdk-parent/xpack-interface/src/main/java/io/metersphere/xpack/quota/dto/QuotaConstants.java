package io.metersphere.xpack.quota.dto;

public class QuotaConstants {

    public enum DefaultType {
        workspace
    }

    // 工作空间下项目的默认配额 prefix + workspace_id
    public static final String prefix = "ws-project";
}
