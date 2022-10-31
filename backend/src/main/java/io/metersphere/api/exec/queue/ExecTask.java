package io.metersphere.api.exec.queue;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.JMeterThreadUtils;
import io.metersphere.cache.JMeterEngineCache;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JmeterThreadUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;

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
        if (request.getRunMode().startsWith(ParamConstants.MODEL.RUN_MODEL_UI.getValue()) &&
                StringUtils.equals(request.getRunType(), RunModeConstants.PARALLEL.toString()) &&
                StringUtils.equals(request.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            request.setReportId(JmeterThreadUtils.getThreadName(request));
        }
        CommonBeanFactory.getBean(JMeterService.class).addQueue(request);
        Object res = PoolExecBlockingQueueUtil.take(request.getReportId());
        if (res == null && !JMeterThreadUtils.isRunning(request.getReportId(), request.getTestId())) {
            LoggerUtil.info("任务执行超时", request.getReportId());
            if (JMeterEngineCache.runningEngine.containsKey(request.getReportId())) {
                JMeterEngineCache.runningEngine.remove(request.getReportId());
            }
        }
    }
}
