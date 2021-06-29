/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.concurrent.Callable;

public class ParallelScenarioExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private HashTree hashTree;
    private String id;

    public ParallelScenarioExecTask(JMeterService jMeterService, String id, HashTree hashTree, RunScenarioRequest request) {
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
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
