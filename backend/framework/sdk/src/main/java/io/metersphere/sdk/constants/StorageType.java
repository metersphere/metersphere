package io.metersphere.sdk.constants;

import org.apache.commons.lang3.StringUtils;

public enum StorageType {

    MINIO, GIT, LOCAL;

    public static boolean isGit(String storage) {
        return StringUtils.equalsIgnoreCase(GIT.name(), storage);
    }
}
