package io.metersphere.plan.service.rerun;

import io.metersphere.plan.service.TestPlanApiCaseBatchRunService;
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
public class TestPlanApiCaseBatchRerunService implements TaskRerunService {
    @Resource
    private TestPlanApiCaseBatchRunService testPlanApiCaseBatchRunService;


    public TestPlanApiCaseBatchRerunService() {
        TaskRerunServiceInvoker.register(ExecTaskType.TEST_PLAN_API_CASE_BATCH, this);
    }

    @Override
    public void rerun(ExecTask execTask, String userId) {
        testPlanApiCaseBatchRunService.rerun(execTask, userId);
    }
}
