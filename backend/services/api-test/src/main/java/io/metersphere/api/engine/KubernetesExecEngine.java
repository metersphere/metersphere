package io.metersphere.api.engine;

import io.metersphere.engine.ApiEngine;
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceDTO;

import java.util.List;

public class KubernetesExecEngine implements ApiEngine {
    /**
     * 任务请求参数 @LINK TaskRequestDTO or TaskBatchRequestDTO or List<String>
     */
    private final Object request;
    private final TestResourceDTO resource;

    public KubernetesExecEngine(TaskRequestDTO request, TestResourceDTO resource) {
        this.request = request;
        this.resource = resource;
    }

    public KubernetesExecEngine(TaskBatchRequestDTO batchRequestDTO, TestResourceDTO resource) {
        this.resource = resource;
        this.request = batchRequestDTO;
    }

    public KubernetesExecEngine(List<String> reportIds, TestResourceDTO resource) {
        this.resource = resource;
        this.request = reportIds;
    }

    @Override
    public void execute(String command) {
        // 初始化任务
        LogUtils.info("CURL 命令：【 " + command + " 】");
        this.runApi(command);
    }

    private void runApi(String command) {
        try {
            KubernetesProvider.exec(resource, request, command);
        } catch (Exception e) {
            LogUtils.error("K8S 执行异常：", e);
            rollbackOnFailure();  // 错误处理逻辑
        }
    }


    // 错误回滚处理
    private void rollbackOnFailure() {
        // TODO: 实现回滚处理逻辑
        LogUtils.info("执行失败，回滚操作启动。");
    }
}
