/**
 *
 */
package io.metersphere.track.service.task;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

public class SerialApiExecTask<T> implements Callable<T> {
    private RunModeConfig config;
    private JMeterService jMeterService;
    private RunModeDataDTO runModeDataDTO;
    private String runMode;
    private ApiDefinitionExecResultMapper mapper;

    public SerialApiExecTask(JMeterService jMeterService, ApiDefinitionExecResultMapper mapper, RunModeDataDTO runModeDataDTO, RunModeConfig config, String runMode) {
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
            // 轮询查看报告状态，最多200次，防止死循环
            ApiDefinitionExecResult report = null;
            int index = 1;
            while (index < 200) {
                Thread.sleep(3000);
                index++;
                report = mapper.selectByPrimaryKey(runModeDataDTO.getApiCaseId());
                if (report != null && !report.getStatus().equals(APITestStatus.Running.name())) {
                    break;
                }
            }
            // 执行失败了，恢复报告状态
            if (index == 200 && report != null && report.getStatus().equals(APITestStatus.Running.name())) {
                report.setStatus(APITestStatus.Error.name());
                mapper.updateByPrimaryKey(report);
            }
            return (T) report;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}