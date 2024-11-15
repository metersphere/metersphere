package io.metersphere.plan.service.rerun;

import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.invoker.TaskRerunServiceInvoker;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.service.TaskRerunService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseRerunService implements TaskRerunService {
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;

    public TestPlanApiCaseRerunService() {
        TaskRerunServiceInvoker.register(ExecTaskType.TEST_PLAN_API_CASE, this);
    }

    @Override
    public void rerun(ExecTask execTask, List<String> taskItemIds,  String userId) {
        ExecTaskItem execTaskItem = execTaskItemMapper.selectByPrimaryKey(taskItemIds.getFirst());
        testPlanApiCaseService.runRun(execTask, execTaskItem, userId);
    }
}
