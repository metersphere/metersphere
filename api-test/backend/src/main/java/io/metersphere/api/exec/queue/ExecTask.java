package io.metersphere.api.exec.queue;

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
        LoggerUtil.info("任务执行超时", request.getReportId());
    }
}
