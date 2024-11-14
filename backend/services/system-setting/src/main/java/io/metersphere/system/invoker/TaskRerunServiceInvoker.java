package io.metersphere.system.invoker;

import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.service.TaskRerunService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:48
 */
public class TaskRerunServiceInvoker {

    private static final Map<ExecTaskType, TaskRerunService> taskRerunServiceMap = new HashMap<>();

    public static void register(ExecTaskType execTaskType, TaskRerunService taskRerunService) {
        taskRerunServiceMap.put(execTaskType, taskRerunService);
    }

    private static TaskRerunService getTaskRerunService(ExecTaskType execTaskType) {
        return taskRerunServiceMap.get(execTaskType);
    }

    public static ExecTaskType getExecTaskType(String execTaskType) {
        return EnumValidator.validateEnum(ExecTaskType.class, execTaskType);
    }

    public static void rerun(ExecTask execTask, List<String> taskItemIds, String userId) {
        getTaskRerunService(getExecTaskType(execTask.getTaskType())).rerun(execTask, taskItemIds, userId);
    }
}
