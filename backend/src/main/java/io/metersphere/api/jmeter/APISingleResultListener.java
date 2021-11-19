package io.metersphere.api.jmeter;


import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.exec.queue.SerialBlockingQueueUtil;
import io.metersphere.api.service.MsResultService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ResultDTO;
import io.metersphere.jmeter.MsExecListener;
import io.metersphere.utils.LoggerUtil;

import java.util.Map;

public class APISingleResultListener extends MsExecListener {
    @Override
    public void handleTeardownTest(ResultDTO dto, Map<String, Object> kafkaConfig) {
        LoggerUtil.info("开始处理单条执行结果报告【" + dto.getReportId() + " 】,资源【 " + dto.getTestId() + " 】");

        dto.setConsole(CommonBeanFactory.getBean(MsResultService.class).getJmeterLogger(dto.getReportId()));
        // 存储结果
        CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
    }

    @Override
    public void testEnded(ResultDTO dto, Map<String, Object> kafkaConfig) {
        try {
            LoggerUtil.info("进入TEST-END处理报告【" + dto.getReportId() + " 】整体执行完成；" + dto.getRunMode());
            // 串行队列
            SerialBlockingQueueUtil.offer(dto, "testEnd");
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            dto.setConsole(CommonBeanFactory.getBean(MsResultService.class).getJmeterLogger(dto.getReportId()));
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
        // 存储结果
        CommonBeanFactory.getBean(TestResultService.class).testEnded(dto);
    }
}
