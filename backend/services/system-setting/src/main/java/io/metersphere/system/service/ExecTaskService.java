package io.metersphere.system.service;

import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: jianxing
 * @CreateTime: 2024-11-18  14:17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExecTaskService {

    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateTaskRerunStatus(ExecTask execTask, String userId) {
        // 更新任务状态
        execTask.setStatus(ExecStatus.RERUNNING.name());
        execTask.setCreateUser(userId);
        execTask.setEndTime(null);
        execTask.setResult(ExecStatus.PENDING.name());
        execTaskMapper.updateByPrimaryKey(execTask);

        // 新建需要重跑的任务项
        extExecTaskItemMapper.insertRerunTaskItem(execTask.getId(), userId);
        // 假删除原有任务项，执行历史需要能查询到
        extExecTaskItemMapper.deleteRerunTaskItem(execTask.getId(), userId);
    }
}
