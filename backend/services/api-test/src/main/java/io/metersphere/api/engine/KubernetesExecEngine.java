package io.metersphere.api.engine;

import io.metersphere.engine.ApiEngine;
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.exception.TaskRunnerResultCode;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.metersphere.api.controller.result.ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR;

public class KubernetesExecEngine implements ApiEngine {
    /**
     * 任务请求参数 @LINK TaskRequestDTO or TaskBatchRequestDTO or List<String>
     */
    private final Object request;
    private final TestResourceDTO resource;

    /**
     * 单调执行构造函数
     *
     * @param request
     * @param resource
     */
    public KubernetesExecEngine(TaskRequestDTO request, TestResourceDTO resource) {
        this.request = request;
        this.resource = resource;
    }

    /**
     * 批量执行构造函数
     *
     * @param batchRequestDTO
     * @param resource
     */
    public KubernetesExecEngine(TaskBatchRequestDTO batchRequestDTO, TestResourceDTO resource) {
        this.resource = resource;
        this.request = batchRequestDTO;
    }

    /**
     * 停止执行构造函数
     *
     * @param reportIds
     * @param resource
     */
    public KubernetesExecEngine(List<String> reportIds, TestResourceDTO resource) {
        this.resource = resource;
        this.request = reportIds;
    }

    @Override
    public void execute(String path) {
        // 初始化任务
        LogUtils.info("CURL 执行方法：【 " + path + " 】");
        this.runApi(path, request);
    }

    private void runApi(String command, Object request) {
        try {
            KubernetesProvider.exec(resource, request, command);
        } catch (HttpServerErrorException e) {
            handleHttpServerError(e);
        } catch (Exception e) {
            handleGeneralError(e);
        }
    }

    private void handleHttpServerError(HttpServerErrorException e) {
        LogUtils.error("K8S 执行异常：", e);

        // 获取错误代码并处理
        int errorCode = Optional.ofNullable(e.getResponseBodyAs(ResultHolder.class))
                .map(ResultHolder::getCode)
                .orElseThrow(() -> new MSException(RESOURCE_POOL_EXECUTE_ERROR, "Unknown error code"));

        // 匹配资源池的错误代码并抛出相应异常
        TaskRunnerResultCode resultCode = Arrays.stream(TaskRunnerResultCode.values())
                .filter(code -> code.getCode() == errorCode)
                .findFirst()
                .orElseThrow(() -> new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage()));

        throw new MSException(resultCode, e.getMessage());
    }

    private void handleGeneralError(Exception e) {
        LogUtils.error("K8S 执行异常：", e);
        throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
    }
}
