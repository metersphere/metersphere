/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.TriggerMode;
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
            if (runModeDataDTO.getReport() != null && MessageCache.terminationOrderDeque.contains(runModeDataDTO.getReport().getId())) {
                MessageCache.terminationOrderDeque.remove(runModeDataDTO.getReport().getId());
                return null;
            }
            String testId;
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                String testPlanScenarioId = "";
                if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(runModeDataDTO.getTestId())) {
                    testPlanScenarioId = request.getScenarioTestPlanIdMap().get(runModeDataDTO.getTestId());
                } else {
                    testPlanScenarioId = request.getPlanScenarioId();
                }
                testId = runModeDataDTO.getTestId();
                jMeterService.runTest(runModeDataDTO.getTestId(), runModeDataDTO.getReport().getId(), request.getRunMode(), testPlanScenarioId, request.getConfig());
            } else {
                testId = runModeDataDTO.getReport().getId();
                jMeterService.runLocal(runModeDataDTO.getReport().getId(), runModeDataDTO.getHashTree(), TriggerMode.BATCH.name().equals(request.getTriggerMode()) ? TriggerMode.BATCH.name() : request.getReportId(), request.getRunMode());
            }
            while (MessageCache.executionQueue.containsKey(testId)) {
                long time = MessageCache.executionQueue.get(runModeDataDTO.getReport().getId());
                long currentSecond = (System.currentTimeMillis() - time) / 1000 / 60;
                // 设置五分钟超时
                if (currentSecond > 5) {
                    // 执行失败了，恢复报告状态
                    report = apiScenarioReportMapper.selectByPrimaryKey(runModeDataDTO.getReport().getId());
                    if (report != null) {
                        report.setStatus(APITestStatus.Error.name());
                        apiScenarioReportMapper.updateByPrimaryKey(report);
                    }
                    break;
                }
                if (runModeDataDTO.getReport() != null && MessageCache.terminationOrderDeque.contains(runModeDataDTO.getReport().getId())) {
                    MessageCache.terminationOrderDeque.remove(runModeDataDTO.getReport().getId());
                    break;
                }
                Thread.sleep(1000);
            }
            return null;
        } catch (Exception ex) {
            LogUtil.error(ex);
            return null;
        }
    }
}
