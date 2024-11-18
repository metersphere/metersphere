package io.metersphere.plan.service.rerun;

import io.metersphere.plan.service.TestPlanExecuteService;
import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.invoker.TaskRerunServiceInvoker;
import io.metersphere.system.service.TaskRerunService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanGroupRerunService implements TaskRerunService {
    @Resource
    private TestPlanExecuteService testPlanExecuteService;

    public TestPlanGroupRerunService() {
        TaskRerunServiceInvoker.register(ExecTaskType.TEST_PLAN_GROUP, this);
    }

    @Override
    public void rerun(ExecTask execTask,  String userId) {
        testPlanExecuteService.testPlanOrGroupRerun(execTask, userId);
    }
}
