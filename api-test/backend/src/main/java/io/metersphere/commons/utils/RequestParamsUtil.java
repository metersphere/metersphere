package io.metersphere.commons.utils;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.service.SystemParameterService;

import java.util.Objects;

public class RequestParamsUtil {

    public static JmeterRunRequestDTO init(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue, String reportId) {
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(queue.getTestId(), reportId, executionQueue.getRunMode());
        runRequest.setRetryEnable(queue.getRetryEnable() != null && queue.getRetryEnable());
        runRequest.setRetryNum(queue.getRetryNumber());
        runRequest.setReportType(executionQueue.getReportType());
        runRequest.setPool(GenerateHashTreeUtil.isResourcePool(executionQueue.getPoolId()));
        runRequest.setTestPlanReportId(executionQueue.getReportId());
        runRequest.setRunType(RunModeConstants.SERIAL.toString());
        runRequest.setQueueId(executionQueue.getId());
        runRequest.setPoolId(executionQueue.getPoolId());
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = Objects.requireNonNull(CommonBeanFactory.getBean(SystemParameterService.class)).getBaseInfo();
        runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, queue.getId()));
        return runRequest;
    }
}
