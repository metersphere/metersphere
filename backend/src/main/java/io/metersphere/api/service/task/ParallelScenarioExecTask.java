/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

public class ParallelScenarioExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private RunModeDataDTO runModeDataDTO;

    public ParallelScenarioExecTask(JMeterService jMeterService, RunModeDataDTO runModeDataDTO, RunScenarioRequest request) {
        this.jMeterService = jMeterService;
        this.request = request;
        this.runModeDataDTO = runModeDataDTO;
    }

    @Override
    public T call() {
        try {
            if (request.getConfig() != null && StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
                jMeterService.runTest(runModeDataDTO.getTestId(), runModeDataDTO.getReport().getId(), request.getRunMode(), request.getPlanScenarioId(), request.getConfig());
            } else {
                jMeterService.runLocal(runModeDataDTO.getReport().getId(), runModeDataDTO.getHashTree(), TriggerMode.BATCH.name().equals(request.getTriggerMode()) ? TriggerMode.BATCH.name() : request.getReportId(), request.getRunMode());
            }
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex);
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
