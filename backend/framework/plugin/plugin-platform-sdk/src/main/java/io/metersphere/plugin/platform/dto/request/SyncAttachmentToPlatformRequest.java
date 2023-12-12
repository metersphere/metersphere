package io.metersphere.plugin.platform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
