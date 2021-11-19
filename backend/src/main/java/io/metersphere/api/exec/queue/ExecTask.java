package io.metersphere.api.exec.queue;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;

public class ExecTask implements Runnable {
    private JmeterRunRequestDTO request;

    public ExecTask(JmeterRunRequestDTO request) {
        this.request = request;
    }

    public JmeterRunRequestDTO getRequest() {
        return this.request;
    }

    @Override
    public void run() {
        PoolExecBlockingQueueUtil.init(request.getReportId(), 1);
        LoggerUtil.info("开始执行报告ID：【 " + request.getReportId() + " 】,资源ID【 " + request.getTestId() + " 】");
        JMeterService jMeterService = CommonBeanFactory.getBean(JMeterService.class);
        jMeterService.addQueue(request);
        PoolExecBlockingQueueUtil.take(request.getReportId());
        LoggerUtil.info("任务：【 " + request.getReportId() + " 】执行完成");
    }
}
