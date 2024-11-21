package io.metersphere.system.service;

import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateTaskRerunStatus(ExecTask execTask, String userId) {
        // 更新任务状态
        execTask.setStatus(ExecStatus.RERUNNING.name());
        execTask.setCreateUser(userId);
        execTask.setEndTime(null);
        execTask.setResult(ExecStatus.PENDING.name());
        execTaskMapper.updateByPrimaryKey(execTask);

        if (BooleanUtils.isFalse(execTask.getIntegrated()) && !StringUtils.equalsAny(execTask.getTaskType(), ExecTaskType.TEST_PLAN.name(), ExecTaskType.TEST_PLAN_GROUP.name())) {
            // 非集合报告和测试计划执行，则删除任务和报告的关联关系
            ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
            example.createCriteria().andTaskResourceIdEqualTo(execTask.getId());
            apiReportRelateTaskMapper.deleteByExample(example);
        }

        // 新建需要重跑的任务项
        extExecTaskItemMapper.insertRerunTaskItem(execTask.getId(), userId);
        // 假删除原有任务项，执行历史需要能查询到
        extExecTaskItemMapper.deleteRerunTaskItem(execTask.getId(), userId);
    }
}
