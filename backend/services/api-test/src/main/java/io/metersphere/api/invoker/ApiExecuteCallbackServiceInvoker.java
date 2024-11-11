package io.metersphere.api.invoker;

import io.metersphere.api.service.ApiExecuteCallbackService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.EnumValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:48
 */
public class ApiExecuteCallbackServiceInvoker {

    private static final Map<ApiExecuteResourceType, ApiExecuteCallbackService> apiResourceExecuteCallbackMap = new HashMap<>();

    public static void register(ApiExecuteResourceType apiExecuteResourceType, ApiExecuteCallbackService callbackService) {
        apiResourceExecuteCallbackMap.put(apiExecuteResourceType, callbackService);
    }

    private static ApiExecuteCallbackService getCallbackService(ApiExecuteResourceType apiExecuteResourceType) {
        return apiResourceExecuteCallbackMap.get(apiExecuteResourceType);
    }

    public static String initReport(String resourceType, GetRunScriptRequest request) {
        return getCallbackService(getApiExecuteResourceType(resourceType)).initReport(request);
    }

    public static GetRunScriptResult getRunScript(String resourceType, GetRunScriptRequest request) {
        return getCallbackService(getApiExecuteResourceType(resourceType)).getRunScript(request);
    }

    public static void executeNextTask(String resourceType, ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        getCallbackService(getApiExecuteResourceType(resourceType)).executeNextTask(queue, queueDetail);
    }

    public static void executeNextCollection(ApiNoticeDTO apiNoticeDTO, boolean isStopOnFailure) {
        getCallbackService(getApiExecuteResourceType(apiNoticeDTO.getResourceType())).executeNextCollection(apiNoticeDTO, isStopOnFailure);
    }

    public static void stopCollectionOnFailure(String resourceType, String collectionQueueId) {
        getCallbackService(getApiExecuteResourceType(resourceType)).stopCollectionOnFailure(collectionQueueId);
    }

    public static ApiExecuteResourceType getApiExecuteResourceType(String resourceType) {
        return EnumValidator.validateEnum(ApiExecuteResourceType.class, resourceType);
    }
}
