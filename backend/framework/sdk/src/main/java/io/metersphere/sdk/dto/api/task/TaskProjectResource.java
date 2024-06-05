package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 任务项
 */
@Data
public class TaskProjectResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 插件文件列表
     * id 为插件的 id + 更新时间戳
     * 任务中心批量执行时会有并发操作，使用 CopyOnWriteArrayList
     */
    private CopyOnWriteArrayList<ApiExecuteFileInfo> pluginFiles;

    /**
     * 接口测试函数包
     */
    private CopyOnWriteArrayList<ApiExecuteFileInfo> funcJars;
}
