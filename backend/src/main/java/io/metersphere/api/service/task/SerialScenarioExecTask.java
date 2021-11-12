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
            String reportId;
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                String testPlanScenarioId = "";
                if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(runModeDataDTO.getTestId())) {
                    testPlanScenarioId = runModeDataDTO.getTestId();
                    String scenarioId = request.getScenarioTestPlanIdMap().get(runModeDataDTO.getTestId());
                    runModeDataDTO.setTestId(scenarioId);
                } else {
                    testPlanScenarioId = request.getPlanScenarioId();
                }
                if(runModeDataDTO.getReport()!= null){
                    reportId = runModeDataDTO.getReport().getId();
                }else {
                    reportId = runModeDataDTO.getTestId();
                }
                jMeterService.runTest(runModeDataDTO.getTestId(), reportId, request.getRunMode(), testPlanScenarioId, request.getConfig());
            } else {
                reportId = runModeDataDTO.getReport().getId();
                jMeterService.runLocal(runModeDataDTO.getReport().getId(),request.getConfig(), runModeDataDTO.getHashTree(), TriggerMode.BATCH.name().equals(request.getTriggerMode()) ? TriggerMode.BATCH.name() : request.getReportId(), request.getRunMode());
            }
            while (MessageCache.executionQueue.containsKey(reportId)) {
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
                if (MessageCache.terminationOrderDeque.contains(reportId)) {
                    MessageCache.terminationOrderDeque.remove(reportId);
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
