package io.metersphere.request;

import io.metersphere.commons.constants.StorageConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class BodyFile {
    private String id;
    private String name;
    // LOCAL 和 引用(FILE_REF) / GIT
    private String storage;
    private String fileId;
    private String projectId;
    private String fileType;
    // 正常/已删除
    private String status;
    // 调试附件处理
    private String refResourceId;

    public boolean isRef() {
        return StringUtils.equals(storage, StorageConstants.FILE_REF.name()) && StringUtils.isNotEmpty(fileId);
    }
}
