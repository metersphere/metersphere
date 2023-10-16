package io.metersphere.plugin.platform.dto;

import lombok.Data;

import java.io.File;

@Data
public class SyncAttachmentToPlatformRequest {
    /**
     * 平台资源Key(需求或缺陷ID)
     */
    private String platformKey;

    /**
     * 需要同步的附件
     */
    private File file;

    /**
     * 同步类型是上传还是删除
     * 参考 SyncAttachmentType
     */
    private String syncType;
}
