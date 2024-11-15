package io.metersphere.api.service.rerun;

import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.invoker.TaskRerunServiceInvoker;
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
public class ApiScenarioRerunService implements TaskRerunService {
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private ApiCommonService apiCommonService;

    public ApiScenarioRerunService() {
        TaskRerunServiceInvoker.register(ExecTaskType.API_SCENARIO, this);
    }

    @Override
    public void rerun(ExecTask execTask, String userId) {
        ExecTaskItem execTaskItem = apiCommonService.getRerunTaskItem(execTask.getId());
        apiScenarioRunService.runRun(execTask, execTaskItem, userId);
    }
}
