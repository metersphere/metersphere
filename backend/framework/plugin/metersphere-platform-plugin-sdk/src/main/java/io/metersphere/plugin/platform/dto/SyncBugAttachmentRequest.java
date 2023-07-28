package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class SyncBugAttachmentRequest {
    /**
     * 平台 ID
     */
    private String platformId;
    /**
     * 需要同步的附件
     */
    private File file;
    /**
     * 操作类型是更新还是删除
     * 参考 AttachmentSyncType
     */
    private String syncType;
}
