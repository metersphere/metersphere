package io.metersphere.api.service;

import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
public interface ApiExecuteCallbackService {
    /**
     * 解析并返回执行脚本
     */
    GetRunScriptResult getRunScript(GetRunScriptRequest request);

    /**
     * 解析并返回执行脚本
     */
    default String initReport(GetRunScriptRequest request) {
        return request.getTaskItem().getReportId();
    }

    /**
     * 串行时，执行下一个任务
     * @param queue
     * @param queueDetail
     */
    void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail);

    /**
     * 批量串行的测试集执行时
     * 测试集下用例执行完成时回调
     * @param apiNoticeDTO
     */
    default void executeNextCollection(ApiNoticeDTO apiNoticeDTO, boolean isStopOnFailure) {
    }

    /**
     * 失败停止时，处理 parentQueue
     * @param parentQueueId
     */
    default void stopCollectionOnFailure(String parentQueueId) {}
}
