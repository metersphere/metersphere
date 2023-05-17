package io.metersphere.api.service;

import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.jmeter.FixedCapacityUtils;
import io.metersphere.cache.JMeterEngineCache;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class RemakeReportService {
    @Resource
    @Lazy
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    @Lazy
    private TestResultService testResultService;

    public void testEnded(JmeterRunRequestDTO request, String errorMsg) {
        try {
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, request);
            dto.setQueueId(request.getQueueId());
            dto.setTestId(request.getTestId());
            if (StringUtils.equals(errorMsg, APITestStatus.TIMEOUT.name())) {
                dto.setArbitraryData(new HashMap<>() {{
                    this.put(APITestStatus.TIMEOUT.name(), true);
                }});
                errorMsg = "执行报告超时，请检查环境和网络是否正常";
            }
            if (JMeterEngineCache.runningEngine.containsKey(dto.getReportId())) {
                JMeterEngineCache.runningEngine.remove(dto.getReportId());
            }
            LoggerUtil.info("进入异常结果处理：" + dto.getRunMode() + " 整体处理完成", dto.getReportId());
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            String consoleMsg = FixedCapacityUtils.getJmeterLogger(dto.getReportId(), true);
            dto.setConsole(consoleMsg + "\n" + errorMsg);
            // 整体执行结束更新资源状态
            testResultService.testEnded(dto);

            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                apiExecutionQueueService.queueNext(dto);
            }
            // 更新测试计划报告
            if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                LoggerUtil.info("Check Processing Test Plan report status：" + dto.getQueueId() + "，" + dto.getTestId(), dto.getReportId());
                apiExecutionQueueService.testPlanReportTestEnded(dto.getTestPlanReportId());
            }
        } catch (Exception e) {
            LoggerUtil.error("回归报告异常", request.getReportId(), e);
        }
    }
}
