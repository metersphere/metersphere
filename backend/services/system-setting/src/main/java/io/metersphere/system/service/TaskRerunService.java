package io.metersphere.system.service;

import io.metersphere.system.domain.ExecTask;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
public interface TaskRerunService {
    /**
     * 任务重跑
     */
    void rerun(ExecTask execTask);
}
