/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

public class SerialScenarioExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private ApiScenarioReportMapper apiScenarioReportMapper;
    private RunModeDataDTO runModeDataDTO;
    ApiScenarioReport report = null;

    public SerialScenarioExecTask(JMeterService jMeterService, ApiScenarioReportMapper apiScenarioReportMapper, RunModeDataDTO runModeDataDTO, RunScenarioRequest request) {
        this.jMeterService = jMeterService;
        this.apiScenarioReportMapper = apiScenarioReportMapper;
        this.request = request;
        this.runModeDataDTO = runModeDataDTO;
    }

    @Override
    public T call() {
        try {
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                jMeterService.runTest(runModeDataDTO.getTestId(), runModeDataDTO.getReport().getId(), request.getRunMode(), request.getPlanScenarioId(), request.getConfig());
            } else {
                jMeterService.runLocal(runModeDataDTO.getReport().getId(), runModeDataDTO.getHashTree(), request.getReportId(), request.getRunMode());
            }
            // 轮询查看报告状态，最多200次，防止死循环
            int index = 1;
            while (index < 200) {
                Thread.sleep(3000);
                index++;
                report = apiScenarioReportMapper.selectByPrimaryKey(runModeDataDTO.getReport().getId());
                if (report != null && !report.getStatus().equals(APITestStatus.Running.name())) {
                    break;
                }
            }
            // 执行失败了，恢复报告状态
            if (index == 200 && report != null && report.getStatus().equals(APITestStatus.Running.name())) {
                report.setStatus(APITestStatus.Error.name());
                apiScenarioReportMapper.updateByPrimaryKey(report);
            }
            return (T) report;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}