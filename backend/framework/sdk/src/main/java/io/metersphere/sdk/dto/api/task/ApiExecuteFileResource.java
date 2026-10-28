package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 执行节点可下载的文件资源
 * 文件的真实存储位置由服务端生成并缓存，执行节点只能通过 fileId 获取
 */
@Data
public class ApiExecuteFileResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件所在目录
     */
    private String folder;
    /**
     * 存储中的文件名称
     * 注意：文件管理中的文件，存储名称是文件ID
     */
    private String fileName;
    /**
     * 存储方式
     * {@link io.metersphere.sdk.constants.StorageType}
     */
    private String storage;
}
