package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-06-03  16:06
 */
@Data
public class TaskResourceFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 点击调试时，尚未保存的本地上传的文件列表
     */
    private List<ApiExecuteFileInfo> localTempFiles;
    /**
     * 通过本地上传的文件ID列表
     */
    private List<ApiExecuteFileInfo> localFiles;
    /**
     * 关联文件管理的文件列表
     * 这里记录文件名，mino存的文件名是id
     * 执行时下载文件后，按原文件命名
     */
    private List<ApiExecuteFileInfo> refFiles;
}
