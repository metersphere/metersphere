package io.metersphere.plan.service.rerun;

import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
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
public class ApiScenarioBatchRerunService implements TaskRerunService {
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;


    public ApiScenarioBatchRerunService() {
        TaskRerunServiceInvoker.register(ExecTaskType.API_SCENARIO_BATCH, this);
    }

    @Override
    public void rerun(ExecTask execTask, String userId) {
        apiScenarioBatchRunService.rerun(execTask, userId);
    }
}
