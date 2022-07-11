package io.metersphere.api.exec.utils;

import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;

public class RequestParamsUtil {

    public static JmeterRunRequestDTO init(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue, String reportId) {
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(queue.getTestId(), reportId, executionQueue.getRunMode(), null);
        runRequest.setRetryEnable(queue.getRetryEnable() == null ? false : queue.getRetryEnable());
        runRequest.setRetryNum(queue.getRetryNumber());
        runRequest.setReportType(executionQueue.getReportType());
        runRequest.setPool(GenerateHashTreeUtil.isResourcePool(executionQueue.getPoolId()));
        runRequest.setTestPlanReportId(executionQueue.getReportId());
        runRequest.setRunType(RunModeConstants.SERIAL.toString());
        runRequest.setQueueId(executionQueue.getId());
        runRequest.setPoolId(executionQueue.getPoolId());
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, queue.getId()));
        return runRequest;
    }

    public static void rollback(JmeterRunRequestDTO runRequest, Exception e) {
        RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
        remakeReportService.remake(runRequest);
        ResultDTO dto = new ResultDTO();
        BeanUtils.copyBean(dto, runRequest);
        CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
        LoggerUtil.error("执行队列[" + runRequest.getQueueId() + "]入队列失败：", runRequest.getReportId(), e);
    }
}
