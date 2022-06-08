package io.metersphere.commons.constants;

import java.util.Objects;

public enum ZentaoIssuePlatformStatus {
    created("新建"), active("激活"), closed("已关闭"), delete("删除"), resolved("已解决"),;

    private final String name;

    ZentaoIssuePlatformStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String getNameByKey(String key) {
        for (ZentaoIssuePlatformStatus status : ZentaoIssuePlatformStatus.values()) {
            if (Objects.equals(status.name(), key)) {
                return status.getName();
            }
        }
        return key;
    }

}
