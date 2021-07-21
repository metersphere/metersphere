/**
 *
 */
package io.metersphere.track.service.task;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

public class ParallelApiExecTask<T> implements Callable<T> {
    private RunModeConfig config;
    private JMeterService jMeterService;
    private RunModeDataDTO runModeDataDTO;
    private String runMode;
    private ApiDefinitionExecResultMapper mapper;

    public ParallelApiExecTask(JMeterService jMeterService, ApiDefinitionExecResultMapper mapper, RunModeDataDTO runModeDataDTO, RunModeConfig config, String runMode) {
        this.jMeterService = jMeterService;
        this.config = config;
        this.runModeDataDTO = runModeDataDTO;
        this.runMode = runMode;
        this.mapper = mapper;
    }

    @Override
    public T call() {
        try {
            if (config != null && StringUtils.isNotBlank(config.getResourcePoolId())) {
                jMeterService.runTest(runModeDataDTO.getTestId(), runModeDataDTO.getApiCaseId(), runMode, null, config);
            } else {
                jMeterService.runLocal(runModeDataDTO.getApiCaseId(), runModeDataDTO.getHashTree(), null, runMode);
            }
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
