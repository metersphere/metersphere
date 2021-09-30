/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.concurrent.Callable;

public class SerialScenarioExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private HashTree hashTree;
    private String id;

    public SerialScenarioExecTask(JMeterService jMeterService, String id, HashTree hashTree, RunScenarioRequest request) {
        this.jMeterService = jMeterService;
        this.request = request;
        this.hashTree = hashTree;
        this.id = id;
    }

    @Override
    public T call() {
        try {
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                jMeterService.runTest(id, hashTree, request.getRunMode(), false, request.getUserId(), request.getConfig());
            } else {
                jMeterService.runSerial(id, hashTree, request.getReportId(), request.getRunMode(), request.getConfig());
            }
            while (MessageCache.executionQueue.containsKey(id)) {
                Thread.sleep(1000);
                long time = MessageCache.executionQueue.get(id);
                long currentSecond = (System.currentTimeMillis() - time) / 1000 / 60;
                // 设置五分钟超时
                if (currentSecond > 5) {
                    // 执行失败了，恢复报告状态
                    break;
                }
            }
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}