package io.metersphere.api.exec.queue;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.JmeterThreadUtils;
import io.metersphere.cache.JMeterEngineCache;
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
        CommonBeanFactory.getBean(JMeterService.class).addQueue(request);
        Object res = PoolExecBlockingQueueUtil.take(request.getReportId());
        if (res == null && !JmeterThreadUtils.isRunning(request.getReportId(), request.getTestId())) {
            LoggerUtil.info("执行报告：【 " + request.getReportId() + " 】,资源ID【 " + request.getTestId() + " 】执行超时");
            if (JMeterEngineCache.runningEngine.containsKey(request.getReportId())) {
                JMeterEngineCache.runningEngine.remove(request.getReportId());
            }
        }
    }
}
