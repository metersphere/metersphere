/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.jorphan.collections.HashTree;

import java.util.concurrent.Callable;

public class ParallelExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private HashTree hashTree;
    private String id;

    public ParallelExecTask(JMeterService jMeterService, String id, HashTree hashTree, RunScenarioRequest request) {
        this.jMeterService = jMeterService;
        this.request = request;
        this.hashTree = hashTree;
        this.id = id;
    }

    @Override
    public T call() {
        try {
            jMeterService.runDefinition(id, hashTree, request.getReportId(), request.getRunMode());
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
